package com.wanli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.dto.WorldStateDTO;
import com.wanli.model.WorldState;
import com.wanli.repository.WorldStateRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WorldStateService {

    private final WorldStateRepository wsRepo;
    private final ObjectMapper objectMapper;

    public WorldStateService(WorldStateRepository wsRepo, ObjectMapper objectMapper) {
        this.wsRepo = wsRepo;
        this.objectMapper = objectMapper;
    }

    public Optional<WorldState> getLatestState(String sessionId) {
        return wsRepo.findTopBySessionIdOrderByRoundNumberDesc(sessionId);
    }

    public WorldStateDTO toDTO(WorldState ws) {
        return new WorldStateDTO(
            ws.getYear(), ws.getEraName(), ws.getTreasury(),
            ws.getPublicSupport(), ws.getMilitaryLoyalty(), ws.getPlayerLocation()
        );
    }

    public void saveState(String sessionId, int round, WorldStateDTO dto) {
        WorldState ws = new WorldState();
        ws.setSessionId(sessionId);
        ws.setRoundNumber(round);
        ws.setYear(dto.getYear());
        ws.setEraName(dto.getEraName());
        ws.setTreasury(dto.getTreasury());
        ws.setPublicSupport(dto.getPublicSupport());
        ws.setMilitaryLoyalty(dto.getMilitaryLoyalty());
        ws.setPlayerLocation(dto.getPlayerLocation());
        try {
            ws.setStateJson(objectMapper.writeValueAsString(dto));
        } catch (Exception e) {
            ws.setStateJson("{}");
        }
        wsRepo.save(ws);
    }

    public String buildStateContext(String sessionId) {
        return getLatestState(sessionId)
            .map(ws -> String.format(
                "当前世界状态：\n- 年份: %d年 (%s)\n- 国库: %d两\n- 民望: %d\n- 军队忠诚度: %d\n- 当前位置: %s\n",
                ws.getYear(), ws.getEraName(), ws.getTreasury(),
                ws.getPublicSupport(), ws.getMilitaryLoyalty(), ws.getPlayerLocation()))
            .orElse("世界状态：游戏中尚未记录\n");
    }
}
