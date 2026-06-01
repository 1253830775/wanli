package com.wanli.controller;

import com.wanli.dto.*;
import com.wanli.model.GameSession;
import com.wanli.model.NpcProfile;
import com.wanli.model.WorldState;
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

    public GameController(GameService gameService,
                           NarrativeService narrativeService,
                           WorldStateService wsService,
                           NpcService npcService) {
        this.gameService = gameService;
        this.narrativeService = narrativeService;
        this.wsService = wsService;
        this.npcService = npcService;
    }

    @PostMapping("/session")
    public CreateSessionResponse createSession(@RequestBody CreateSessionRequest request) {
        GameSession session = gameService.createSession(request.getPlayerName());
        return new CreateSessionResponse(session.getSessionId(),
            "欢迎来到大明！你已穿越成明神宗朱翊钧。\n" +
            "隆庆六年（1572 年），先帝病重，召你与顾命大臣于乾清宫……\n" +
            "输入你的第一个行动开始冒险。");
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
}
