package com.wanli.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.dto.CourtSessionDTO;
import com.wanli.dto.CourtSessionDTO.TopicDTO;
import com.wanli.model.CourtSessionState;
import com.wanli.repository.CourtSessionStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourtSessionService {

    private static final Logger log = LoggerFactory.getLogger(CourtSessionService.class);

    public static final String PHASE_IDLE = "idle";
    public static final String PHASE_REPORT = "report";
    public static final String PHASE_INQUIRY = "inquiry";
    public static final String PHASE_RULING = "ruling";
    public static final String PHASE_REACTION = "reaction";
    public static final String PHASE_ENDED = "ended";

    private final CourtSessionStateRepository repo;
    private final ObjectMapper objectMapper;

    public CourtSessionService(CourtSessionStateRepository repo) {
        this.repo = repo;
        this.objectMapper = new ObjectMapper();
    }

    public CourtSessionState startCourtSession(String sessionId) {
        int nextNumber = repo.findTopBySessionIdOrderByCourtNumberDesc(sessionId)
            .map(s -> s.getCourtNumber() + 1)
            .orElse(1);

        repo.findBySessionIdAndIsActive(sessionId, true).ifPresent(existing -> {
            existing.setIsActive(false);
            repo.save(existing);
        });

        CourtSessionState state = new CourtSessionState();
        state.setSessionId(sessionId);
        state.setCourtNumber(nextNumber);
        state.setPhase(PHASE_REPORT);
        state.setCurrentTopicIndex(0);
        state.setTopicsJson("[]");
        state.setInquiryCount(0);
        state.setIsActive(true);
        return repo.save(state);
    }

    public CourtSessionState advancePhase(String sessionId, String playerInput) {
        Optional<CourtSessionState> opt = repo.findBySessionIdAndIsActive(sessionId, true);
        if (opt.isEmpty()) return null;

        CourtSessionState state = opt.get();
        String phase = state.getPhase();
        String normalized = playerInput == null ? "" : playerInput;

        switch (phase) {
            case PHASE_REPORT -> {
                state.setPhase(PHASE_INQUIRY);
                state.setInquiryCount(0);
            }
            case PHASE_INQUIRY -> {
                state.setInquiryCount(state.getInquiryCount() + 1);
                if (containsAny(normalized, "裁决", "定夺", "下旨", "批示", "朕意已决", "做出决定")) {
                    state.setPhase(PHASE_RULING);
                }
            }
            case PHASE_RULING -> {
                state.setPhase(PHASE_REACTION);
            }
            case PHASE_REACTION -> {
                List<TopicDTO> topics = parseTopics(state.getTopicsJson());
                int nextIndex = state.getCurrentTopicIndex() + 1;
                if (nextIndex < topics.size() && !containsAny(normalized, "退朝", "散朝", "结束朝会")) {
                    state.setCurrentTopicIndex(nextIndex);
                    state.setPhase(PHASE_REPORT);
                    state.setInquiryCount(0);
                } else {
                    state.setPhase(PHASE_ENDED);
                    state.setIsActive(false);
                }
            }
        }

        return repo.save(state);
    }

    public void endCourtSession(String sessionId) {
        repo.findBySessionIdAndIsActive(sessionId, true).ifPresent(state -> {
            state.setPhase(PHASE_ENDED);
            state.setIsActive(false);
            repo.save(state);
        });
    }

    public Optional<CourtSessionState> getActiveSession(String sessionId) {
        return repo.findBySessionIdAndIsActive(sessionId, true);
    }

    public CourtSessionDTO toDTO(CourtSessionState state) {
        CourtSessionDTO dto = new CourtSessionDTO();
        dto.setCourtNumber(state.getCourtNumber());
        dto.setPhase(state.getPhase());
        dto.setCurrentTopicIndex(state.getCurrentTopicIndex());
        dto.setInquiryCount(state.getInquiryCount());
        dto.setIsActive(state.getIsActive());
        dto.setTopics(parseTopics(state.getTopicsJson()));
        return dto;
    }

    public String buildCourtContext(String sessionId) {
        Optional<CourtSessionState> opt = getActiveSession(sessionId);
        if (opt.isEmpty()) {
            return "当前状态：非朝会时间。玩家可输入“上朝”开启朝会。\n";
        }

        CourtSessionState state = opt.get();
        List<TopicDTO> topics = parseTopics(state.getTopicsJson());
        TopicDTO currentTopic = topics.isEmpty() ? null :
            (state.getCurrentTopicIndex() < topics.size() ? topics.get(state.getCurrentTopicIndex()) : null);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("## 朝会状态\n"));
        sb.append(String.format("- 第 %d 次朝会\n", state.getCourtNumber()));
        sb.append(String.format("- 当前阶段：%s\n", phaseLabel(state.getPhase())));

        if (currentTopic != null) {
            sb.append(String.format("- 当前议题：%s\n", currentTopic.getTitle()));
            sb.append(String.format("  - 奏报人：%s\n", currentTopic.getReporter()));
            if (currentTopic.getDescription() != null) {
                sb.append(String.format("  - 议题概要：%s\n", currentTopic.getDescription()));
            }
            if (currentTopic.getHistoricalNote() != null) {
                sb.append(String.format("  - 历史参考：%s\n", currentTopic.getHistoricalNote()));
            }
        }

        sb.append(String.format("- 已问对轮数：%d\n", state.getInquiryCount()));
        sb.append(String.format("- 议题进度：%d/%d\n",
            state.getCurrentTopicIndex() + 1, Math.max(topics.size(), 1)));

        sb.append("\n### 阶段叙事要求\n");
        switch (state.getPhase()) {
            case PHASE_REPORT -> sb.append("""
                当前是【奏报阶段】。请生成大臣上奏的场景：
                1. 描述朝堂氛围和环境
                2. 由奏报大臣陈述议题内容
                3. 其他在场大臣的初步反应
                4. 叙事结束后，AI 应自然过渡到问对阶段
                """);
            case PHASE_INQUIRY -> sb.append("""
                当前是【问对阶段】。玩家正在向大臣追问细节：
                1. 根据玩家的问题，由被 @的大臣回答
                2. 如果玩家 @了某位大臣，该大臣必须给出有立场的回答
                3. 其他大臣可以在适当时候插话
                4. 保持克制，暗流涌动而非当庭争吵
                """);
            case PHASE_RULING -> sb.append("""
                当前是【裁决阶段】。玩家需要对当前议题做出决策：
                1. 描述朝堂等待皇帝裁决的紧张氛围
                2. 各大臣的目光和期待
                3. 在叙事末尾提供 2-3 个决策选项供玩家选择
                4. 玩家做出决策后，在叙事末尾输出状态变更：
                   【状态变更】{"treasury":数值变化,"publicSupport":数值变化,"imperialAuthority":数值变化,"npcAttitudes":{"npcId":态度变化值}}
                """);
            case PHASE_REACTION -> sb.append("""
                当前是【反应阶段】。皇帝已做出裁决：
                1. 描述各大臣对裁决的反应（赞同/沉默/不满）
                2. 根据 NPC 关系矩阵，盟友和对手的反应不同
                3. 暗示此决策可能带来的后续影响
                4. 如果还有下一个议题，自然过渡到下一个奏报
                5. 如果是最后一个议题，描述朝会即将结束的氛围
                """);
            case PHASE_ENDED -> sb.append("""
                朝会已结束。请生成退朝场景：
                1. 宣布退朝
                2. 简要总结本次朝会的关键决策
                3. 暗示下一步可能的走向
                4. 在末尾附上"史书对照"：据《明史》记载，此事本应……然而在你的干预下……
                """);
        }

        return sb.toString();
    }

    public void updateTopics(String sessionId, List<TopicDTO> topics) {
        repo.findBySessionIdAndIsActive(sessionId, true).ifPresent(state -> {
            try {
                state.setTopicsJson(objectMapper.writeValueAsString(topics));
                repo.save(state);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize topics", e);
            }
        });
    }

    private List<TopicDTO> parseTopics(String topicsJson) {
        if (topicsJson == null || topicsJson.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(topicsJson, new TypeReference<List<TopicDTO>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse topics JSON", e);
            return new ArrayList<>();
        }
    }

    private String phaseLabel(String phase) {
        return switch (phase) {
            case PHASE_REPORT -> "奏报";
            case PHASE_INQUIRY -> "问对";
            case PHASE_RULING -> "裁决";
            case PHASE_REACTION -> "反应";
            case PHASE_ENDED -> "已结束";
            default -> phase;
        };
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}
