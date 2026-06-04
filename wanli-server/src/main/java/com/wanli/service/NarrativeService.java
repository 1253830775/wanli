package com.wanli.service;

import com.wanli.dto.NarrativeEvent;
import com.wanli.dto.StateChangeDTO;
import com.wanli.model.CourtSessionState;
import com.wanli.model.GameSession;
import com.wanli.model.NpcProfile;
import com.wanli.repository.GameSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NarrativeService {

    private static final Logger log = LoggerFactory.getLogger(NarrativeService.class);

    private final GameSessionRepository sessionRepo;
    private final LLMClient llmClient;
    private final ContextAssembler contextAssembler;
    private final WorldStateService wsService;
    private final KnowledgeGraphService kgService;
    private final NpcService npcService;
    private final CourtSessionService courtSessionService;
    private final StateChangeParser stateChangeParser;

    public NarrativeService(GameSessionRepository sessionRepo,
                             LLMClient llmClient,
                             ContextAssembler contextAssembler,
                             WorldStateService wsService,
                             KnowledgeGraphService kgService,
                             NpcService npcService,
                             CourtSessionService courtSessionService,
                             StateChangeParser stateChangeParser) {
        this.sessionRepo = sessionRepo;
        this.llmClient = llmClient;
        this.contextAssembler = contextAssembler;
        this.wsService = wsService;
        this.kgService = kgService;
        this.npcService = npcService;
        this.courtSessionService = courtSessionService;
        this.stateChangeParser = stateChangeParser;
    }

    public Flux<NarrativeEvent> generateNarrative(String sessionId, String playerInput, String targetNpc) {
        return Flux.create(sink -> {
            GameSession session = sessionRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

            String history = session.getNarrativeHistory() != null ? session.getNarrativeHistory() : "";
            int newRound = session.getCurrentRound() + 1;

            handleCourtSessionFlow(sessionId, playerInput);

            var courtState = courtSessionService.getActiveSession(sessionId);

            wsService.getLatestState(sessionId).ifPresent(ws -> {
                NarrativeEvent header = new NarrativeEvent();
                header.setType("state");
                header.setSessionId(sessionId);
                header.setWorldState(wsService.toDTO(ws));

                List<NpcProfile> sceneNpcs = npcService.getSceneNpcs(sessionId, "");
                header.setSceneCharacters(sceneNpcs.stream()
                    .map(n -> "@" + n.getName())
                    .collect(Collectors.toList()));

                courtState.ifPresent(cs -> header.setCourtSession(courtSessionService.toDTO(cs)));

                sink.next(header);
            });

            String systemPrompt = contextAssembler.buildSystemPrompt(sessionId);
            String userPrompt = contextAssembler.buildNarrativePrompt(playerInput, history, targetNpc);
            Map<String, Object> subGraph = kgService.getSubGraph(sessionId, playerInput, 50);
            String kgContext = "--- 相关知识图谱 ---\n" + subGraph.toString();
            String fullSystemPrompt = systemPrompt + "\n" + kgContext;

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
                        String narrative = fullNarrative.toString();

                        StateChangeDTO stateChange = stateChangeParser.parse(narrative);
                        if (stateChange != null) {
                            applyStateChange(sessionId, newRound, stateChange);
                            narrative = stateChangeParser.stripStateChangeBlock(narrative);
                        }

                        String cleanedNarrative = narrative;
                        String updatedHistory = history + "\n[第" + newRound + "轮] " + playerInput + "\n" + cleanedNarrative;
                        session.setNarrativeHistory(updatedHistory);
                        session.setCurrentRound(newRound);
                        sessionRepo.save(session);

                        wsService.getLatestState(sessionId).ifPresent(ws -> {
                            NarrativeEvent stateUpdate = new NarrativeEvent();
                            stateUpdate.setType("state");
                            stateUpdate.setSessionId(sessionId);
                            stateUpdate.setWorldState(wsService.toDTO(ws));

                            List<NpcProfile> sceneNpcs = npcService.getSceneNpcs(sessionId, "");
                            stateUpdate.setSceneCharacters(sceneNpcs.stream()
                                .map(n -> "@" + n.getName())
                                .collect(Collectors.toList()));

                            courtSessionService.getActiveSession(sessionId)
                                .ifPresent(cs -> stateUpdate.setCourtSession(courtSessionService.toDTO(cs)));

                            sink.next(stateUpdate);
                        });

                        NarrativeEvent done = new NarrativeEvent();
                        done.setType("done");
                        done.setSessionId(sessionId);
                        sink.next(done);
                        sink.complete();
                    }
                );
        });
    }

    private void handleCourtSessionFlow(String sessionId, String playerInput) {
        String normalized = playerInput == null ? "" : playerInput;
        var activeSession = courtSessionService.getActiveSession(sessionId);

        if (activeSession.isEmpty()) {
            if (containsAny(normalized, "上朝", "早朝", "朝会", "奉天殿")) {
                courtSessionService.startCourtSession(sessionId);
            }
        } else {
            if (containsAny(normalized, "退朝", "散朝", "结束朝会", "结束上朝")) {
                courtSessionService.endCourtSession(sessionId);
                return;
            }
            courtSessionService.advancePhase(sessionId, playerInput);
        }
    }

    private void applyStateChange(String sessionId, int round, StateChangeDTO change) {
        wsService.getLatestState(sessionId).ifPresent(ws -> {
            if (change.getTreasury() != null) {
                ws.setTreasury(ws.getTreasury() + change.getTreasury());
            }
            if (change.getPublicSupport() != null) {
                ws.setPublicSupport(clamp(ws.getPublicSupport() + change.getPublicSupport(), 0, 100));
            }
            if (change.getImperialAuthority() != null) {
                ws.setImperialAuthority(clamp(ws.getImperialAuthority() + change.getImperialAuthority(), 0, 100));
            }
            wsService.saveState(sessionId, round, ws);
        });

        if (change.getNpcAttitudes() != null) {
            change.getNpcAttitudes().forEach((npcId, delta) -> {
                npcService.updateAttitude(npcId, sessionId, delta, "朝会裁决");
            });
        }

        if (change.getNpcRelations() != null) {
            change.getNpcRelations().forEach((pair, delta) -> {
                String[] ids = pair.split("-");
                if (ids.length == 2) {
                    npcService.updateRelation(ids[0], ids[1], sessionId, delta);
                }
            });
        }

        kgService.addCausalEvent(sessionId,
            "朝会裁决 (第" + round + "轮)",
            change.toString(),
            round);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}
