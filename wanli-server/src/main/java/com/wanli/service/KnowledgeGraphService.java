package com.wanli.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.model.CausalEvent;
import com.wanli.model.GameEntity;
import com.wanli.model.GameRelation;
import com.wanli.repository.CausalEventRepository;
import com.wanli.repository.GameEntityRepository;
import com.wanli.repository.GameRelationRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeGraphService {

    private final GameEntityRepository entityRepo;
    private final GameRelationRepository relationRepo;
    private final CausalEventRepository causalRepo;
    private final ObjectMapper objectMapper;

    public KnowledgeGraphService(GameEntityRepository entityRepo,
                                  GameRelationRepository relationRepo,
                                  CausalEventRepository causalRepo,
                                  ObjectMapper objectMapper) {
        this.entityRepo = entityRepo;
        this.relationRepo = relationRepo;
        this.causalRepo = causalRepo;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getSubGraph(String sessionId, String context, int topK) {
        List<GameEntity> matchedEntities = entityRepo.searchByText(sessionId, context);
        if (matchedEntities.size() > topK) {
            matchedEntities = matchedEntities.subList(0, topK);
        }

        List<Long> entityIds = matchedEntities.stream()
                .map(GameEntity::getId)
                .collect(Collectors.toList());

        List<GameRelation> relations;
        if (entityIds.isEmpty()) {
            relations = relationRepo.findBySessionId(sessionId);
            if (relations.size() > topK * 2) {
                relations = relations.subList(0, topK * 2);
            }
        } else {
            relations = relationRepo.findByEntityIds(sessionId, entityIds);
        }

        List<CausalEvent> recentCausal = causalRepo.findBySessionIdOrderByRoundNumberDesc(sessionId);
        if (recentCausal.size() > 20) {
            recentCausal = recentCausal.subList(0, 20);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entities", matchedEntities.stream().map(e ->
            Map.of("id", e.getId(), "name", e.getName(), "type", e.getType().name(),
                   "description", e.getDescription())
        ).collect(Collectors.toList()));
        result.put("relations", relations.stream().map(r ->
            Map.of("source", r.getSourceId(), "target", r.getTargetId(), "type", r.getType())
        ).collect(Collectors.toList()));
        result.put("causalEvents", recentCausal.stream().map(c ->
            Map.of("eventId", c.getEventId(), "trigger", c.getTriggerDescription(),
                   "effects", c.getEffects(), "round", c.getRoundNumber())
        ).collect(Collectors.toList()));

        return result;
    }

    public void addCausalEvent(String sessionId, String trigger, String effects, int round) {
        CausalEvent event = new CausalEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setSessionId(sessionId);
        event.setTriggerDescription(trigger);
        event.setEffects(effects);
        event.setRoundNumber(round);
        causalRepo.save(event);
    }
}
