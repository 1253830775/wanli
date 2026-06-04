package com.wanli.service;

import com.wanli.dto.WorldStateDTO;
import com.wanli.model.GameSession;
import com.wanli.model.NpcProfile;
import com.wanli.repository.GameSessionRepository;
import com.wanli.repository.NpcProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GameService {

    private final GameSessionRepository sessionRepo;
    private final NpcService npcService;
    private final WorldStateService wsService;
    private final KnowledgeGraphService kgService;
    private final NpcProfileRepository npcProfileRepo;
    private final EventNodeService eventNodeService;

    public GameService(GameSessionRepository sessionRepo,
                        NpcService npcService,
                        WorldStateService wsService,
                        KnowledgeGraphService kgService,
                        NpcProfileRepository npcProfileRepo,
                        EventNodeService eventNodeService) {
        this.sessionRepo = sessionRepo;
        this.npcService = npcService;
        this.wsService = wsService;
        this.kgService = kgService;
        this.npcProfileRepo = npcProfileRepo;
        this.eventNodeService = eventNodeService;
    }

    @Transactional
    public GameSession createSession(String playerName) {
        GameSession session = new GameSession();
        String sessionId = UUID.randomUUID().toString();
        session.setSessionId(sessionId);
        session.setPlayerName(playerName);
        session.setStatus("active");
        session.setCurrentRound(0);
        session = sessionRepo.save(session);

        // Initialize default NPCs
        initNpcs(sessionId);

        // Initialize starting world state
        WorldStateDTO startingState = new WorldStateDTO();
        startingState.setYear(1572);
        startingState.setEraName("隆庆六年");
        startingState.setTreasury(2000000L);
        startingState.setPublicSupport(65);
        startingState.setMilitaryLoyalty(70);
        startingState.setImperialAuthority(50);
        startingState.setPlayerLocation("乾清宫");
        wsService.saveState(sessionId, 0, startingState);
        eventNodeService.initializeSession(sessionId);

        return session;
    }

    private void initNpcs(String sessionId) {
        saveNpc("zhang_juzheng", "张居正", sessionId,
            "[\"严厉\", \"忠诚\", \"务实\", \"雄才大略\"]",
            "[\"不知后世之事\", \"不知玩家穿越身份\"]",
            75, "顾命大臣 · 首辅", "改革派",
            "{\"gao_gong\":-20,\"feng_bao\":30,\"li_shi\":10}", 1582);

        saveNpc("gao_gong", "高拱", sessionId,
            "[\"刚直\", \"老练\", \"善谋\"]",
            "[\"不知后世之事\", \"不知玩家穿越身份\"]",
            60, "顾命大臣 · 前首辅", "保守派",
            "{\"zhang_juzheng\":-20,\"feng_bao\":-40}", 1578);

        saveNpc("feng_bao", "冯保", sessionId,
            "[\"精明\", \"机变\", \"有权谋\"]",
            "[\"不知后世之事\", \"不知玩家穿越身份\"]",
            50, "司礼监掌印太监", "内廷派",
            "{\"zhang_juzheng\":30,\"gao_gong\":-40,\"li_shi\":15}", 1583);

        saveNpc("li_shi", "李氏", sessionId,
            "[\"慈爱\", \"谨慎\", \"传统\"]",
            "[\"不知后世之事\", \"不知玩家穿越身份\"]",
            80, "生母 · 皇太后", "后宫",
            "{\"zhang_juzheng\":10,\"feng_bao\":15}", null);

        saveNpc("chen_shi", "陈氏", sessionId,
            "[\"温和\", \"守礼\", \"淡泊\"]",
            "[\"不知后世之事\", \"不知玩家穿越身份\"]",
            70, "嫡母 · 皇太后", "后宫",
            "{}", null);
    }

    private void saveNpc(String npcId, String name, String sessionId,
                          String traits, String knowledgeBoundary, int attitude,
                          String status, String faction, String relations,
                          Integer historicalDeathYear) {
        NpcProfile profile = new NpcProfile();
        profile.setNpcId(npcId);
        profile.setName(name);
        profile.setSessionId(sessionId);
        profile.setTraits(traits);
        profile.setKnowledgeBoundary(knowledgeBoundary);
        profile.setAttitudeJson("{\"value\":" + attitude + ",\"history\":[]}");
        profile.setStatus(status);
        profile.setFaction(faction);
        profile.setRelations(relations);
        profile.setAlive(true);
        profile.setHistoricalDeathYear(historicalDeathYear);
        if (npcService.getProfile(npcId, sessionId).isEmpty()) {
            npcProfileRepo.save(profile);
        }
    }
}
