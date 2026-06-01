package com.wanli.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.model.NpcProfile;
import com.wanli.repository.NpcProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NpcService {

    private final NpcProfileRepository npcRepo;
    private final ObjectMapper objectMapper;

    public NpcService(NpcProfileRepository npcRepo, ObjectMapper objectMapper) {
        this.npcRepo = npcRepo;
        this.objectMapper = objectMapper;
    }

    public Optional<NpcProfile> getProfile(String npcId, String sessionId) {
        return npcRepo.findByNpcIdAndSessionId(npcId, sessionId);
    }

    public List<NpcProfile> getSceneNpcs(String sessionId, String location) {
        return npcRepo.findBySessionId(sessionId);
    }

    public void updateAttitude(String npcId, String sessionId, int delta, String reason) {
        npcRepo.findByNpcIdAndSessionId(npcId, sessionId).ifPresent(profile -> {
            try {
                Map<String, Object> attitude = objectMapper.readValue(
                    profile.getAttitudeJson(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                );
                int currentValue = (int) attitude.getOrDefault("value", 50);
                attitude.put("value", Math.max(0, Math.min(100, currentValue + delta)));
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> history = (List<Map<String, Object>>) attitude.get("history");
                history.add(Map.of("reason", reason, "delta", delta));
                profile.setAttitudeJson(objectMapper.writeValueAsString(attitude));
                npcRepo.save(profile);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to update attitude", e);
            }
        });
    }

    public String buildNpcContext(List<NpcProfile> npcs) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- NPC 状态 ---\n");
        for (NpcProfile npc : npcs) {
            sb.append(String.format("- %s: %s, 态度: %s, 知识边界: %s\n",
                npc.getName(), npc.getTraits(),
                npc.getAttitudeJson(), npc.getKnowledgeBoundary()));
        }
        return sb.toString();
    }
}
