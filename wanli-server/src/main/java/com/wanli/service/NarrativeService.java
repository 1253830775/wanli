package com.wanli.service;

import com.wanli.dto.NarrativeEvent;
import com.wanli.model.GameSession;
import com.wanli.model.NpcProfile;
import com.wanli.repository.GameSessionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NarrativeService {

    private final GameSessionRepository sessionRepo;
    private final LLMClient llmClient;
    private final ContextAssembler contextAssembler;
    private final WorldStateService wsService;
    private final KnowledgeGraphService kgService;
    private final NpcService npcService;
    private final EventNodeService eventNodeService;

    public NarrativeService(GameSessionRepository sessionRepo,
                             LLMClient llmClient,
                             ContextAssembler contextAssembler,
                             WorldStateService wsService,
                             KnowledgeGraphService kgService,
                             NpcService npcService,
                             EventNodeService eventNodeService) {
        this.sessionRepo = sessionRepo;
        this.llmClient = llmClient;
        this.contextAssembler = contextAssembler;
        this.wsService = wsService;
        this.kgService = kgService;
        this.npcService = npcService;
        this.eventNodeService = eventNodeService;
    }

    public Flux<NarrativeEvent> generateNarrative(String sessionId, String playerInput, String targetNpc) {
        return Flux.create(sink -> {
            GameSession session = sessionRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

            String history = session.getNarrativeHistory() != null ? session.getNarrativeHistory() : "";
            int newRound = session.getCurrentRound() + 1;
            var eventNode = eventNodeService.advanceByInput(sessionId, playerInput);

            // Emit world state header
            wsService.getLatestState(sessionId).ifPresent(ws -> {
                NarrativeEvent header = new NarrativeEvent();
                header.setType("state");
                header.setSessionId(sessionId);
                header.setWorldState(wsService.toDTO(ws));

                List<NpcProfile> sceneNpcs = npcService.getSceneNpcs(sessionId, "");
                header.setSceneCharacters(sceneNpcs.stream()
                    .map(n -> "@" + n.getName())
                    .collect(Collectors.toList()));
                header.setActiveEvent(eventNode);

                sink.next(header);
            });

            // Build context (blocking, use boundedElastic)
            String systemPrompt = contextAssembler.buildSystemPrompt(sessionId);
            String userPrompt = contextAssembler.buildNarrativePrompt(playerInput, history, targetNpc);
            Map<String, Object> subGraph = kgService.getSubGraph(sessionId, playerInput, 50);
            String kgContext = "--- 相关知识图谱 ---\n" + subGraph.toString();
            String fullSystemPrompt = systemPrompt + "\n" + kgContext;

            // Stream from LLM
            StringBuilder fullNarrative = new StringBuilder();

            llmClient.streamChat(fullSystemPrompt, userPrompt)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                    token -> {
                        fullNarrative.append(token);
                        NarrativeEvent event = new NarrativeEvent();
                        event.setType("token");
                        event.setToken(token);
                        event.setSessionId(sessionId);
                        sink.next(event);
                    },
                    error -> {
                        NarrativeEvent err = new NarrativeEvent();
                        err.setType("error");
                        err.setError(error.getMessage());
                        sink.next(err);
                        sink.complete();
                    },
                    () -> {
                        // Save narrative and update state
                        String updatedHistory = history + "\n[第" + newRound + "轮] " + playerInput + "\n" + fullNarrative;
                        session.setNarrativeHistory(updatedHistory);
                        session.setCurrentRound(newRound);
                        sessionRepo.save(session);

                        NarrativeEvent done = new NarrativeEvent();
                        done.setType("done");
                        done.setSessionId(sessionId);
                        sink.next(done);
                        sink.complete();
                    }
                );
        });
    }
}
