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
        WorldStateDTO dto = new WorldStateDTO();
        dto.setYear(ws.getYear());
        dto.setEraName(ws.getEraName());
        dto.setTreasury(ws.getTreasury());
        dto.setPublicSupport(ws.getPublicSupport());
        dto.setMilitaryLoyalty(ws.getMilitaryLoyalty());
        dto.setImperialAuthority(ws.getImperialAuthority());
        dto.setPlayerLocation(ws.getPlayerLocation());
        return dto;
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
        ws.setImperialAuthority(dto.getImperialAuthority());
        ws.setPlayerLocation(dto.getPlayerLocation());
        try {
            ws.setStateJson(objectMapper.writeValueAsString(dto));
        } catch (Exception e) {
            ws.setStateJson("{}");
        }
        wsRepo.save(ws);
    }

    public void saveState(String sessionId, int round, WorldState ws) {
        WorldState newState = new WorldState();
        newState.setSessionId(sessionId);
        newState.setRoundNumber(round);
        newState.setYear(ws.getYear());
        newState.setEraName(ws.getEraName());
        newState.setTreasury(ws.getTreasury());
        newState.setPublicSupport(ws.getPublicSupport());
        newState.setMilitaryLoyalty(ws.getMilitaryLoyalty());
        newState.setImperialAuthority(ws.getImperialAuthority());
        newState.setPlayerLocation(ws.getPlayerLocation());
        try {
            newState.setStateJson(objectMapper.writeValueAsString(newState));
        } catch (Exception e) {
            // ignore
        }
        wsRepo.save(newState);
    }

    public String buildStateContext(String sessionId) {
        return getLatestState(sessionId)
            .map(ws -> String.format(
                "--- 世界状态 ---\n- 纪年：%d年 · %s\n- 国库：%d 两\n- 民望：%d\n- 帝威：%d\n- 所在：%s\n",
                ws.getYear(), ws.getEraName(), ws.getTreasury(),
                ws.getPublicSupport(), ws.getImperialAuthority(), ws.getPlayerLocation()))
            .orElse("世界状态：游戏中尚未记录\n");
    }
}
