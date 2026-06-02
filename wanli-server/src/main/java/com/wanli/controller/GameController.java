package com.wanli.controller;

import com.wanli.dto.*;
import com.wanli.model.GameSession;
import com.wanli.service.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;
    private final NarrativeService narrativeService;
    private final WorldStateService wsService;
    private final NpcService npcService;
    private final CourtSessionService courtSessionService;

    public GameController(GameService gameService,
                           NarrativeService narrativeService,
                           WorldStateService wsService,
                           NpcService npcService,
                           CourtSessionService courtSessionService) {
        this.gameService = gameService;
        this.narrativeService = narrativeService;
        this.wsService = wsService;
        this.npcService = npcService;
        this.courtSessionService = courtSessionService;
    }

    @PostMapping("/session")
    public CreateSessionResponse createSession(@RequestBody CreateSessionRequest request) {
        GameSession session = gameService.createSession(request.getPlayerName());
        return new CreateSessionResponse(session.getSessionId(),
            "你以十岁天子的身份醒在乾清宫。\n" +
            "隆庆六年（1572 年），先帝病重托孤，顾命大臣与司礼监都在等待新君的态度。\n" +
            "输入"上朝"开启第一次朝会，在高拱、张居正、冯保之间做出你的第一个决策。",
            null,
            wsService.getLatestState(session.getSessionId()).map(wsService::toDTO).orElse(new WorldStateDTO()));
    }

    @PostMapping(value = "/narrate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NarrativeEvent> narrate(@RequestBody PlayerInput input) {
        return narrativeService.generateNarrative(
            input.getSessionId(), input.getText(), input.getTargetNpc());
    }

    @GetMapping("/{sessionId}/state")
    public WorldStateDTO getState(@PathVariable String sessionId) {
        return wsService.getLatestState(sessionId)
            .map(wsService::toDTO)
            .orElse(new WorldStateDTO());
    }

    @GetMapping("/{sessionId}/npcs")
    public List<Map<String, String>> getSceneNpcs(@PathVariable String sessionId) {
        return npcService.getSceneNpcs(sessionId, "").stream()
            .map(n -> Map.of("id", n.getNpcId(), "name", n.getName(),
                             "status", n.getStatus(),
                             "attitude", n.getAttitudeJson()))
            .collect(Collectors.toList());
    }

    @GetMapping("/{sessionId}/court")
    public CourtSessionDTO getCourtSession(@PathVariable String sessionId) {
        return courtSessionService.getActiveSession(sessionId)
            .map(courtSessionService::toDTO)
            .orElse(null);
    }
}
