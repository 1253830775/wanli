package com.wanli.service;

import com.wanli.dto.EventNodeDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventNodeService {

    private static final String COURT_EVENT_ID = "morning_court";
    private final Map<String, EventNodeDTO> activeEvents = new ConcurrentHashMap<>();

    public EventNodeDTO initializeSession(String sessionId) {
        EventNodeDTO event = new EventNodeDTO(
            "regency_transition",
            "隆庆托孤",
            "先帝病重，顾命大臣与司礼监都在等待新君的第一道态度。完成托孤问答后，可选择移驾上朝。",
            "active",
            1,
            List.of("听取托孤", "安抚两宫", "确定辅政格局"),
            List.of("询问张居正托孤细节", "安抚两宫皇太后", "上朝，召集群臣奏对"),
            "开局自动触发",
            "输入“上朝”会收束托孤并开启朝会节点"
        );
        activeEvents.put(sessionId, event);
        return event;
    }

    public EventNodeDTO advanceByInput(String sessionId, String playerInput) {
        String normalizedInput = playerInput == null ? "" : playerInput;
        EventNodeDTO current = activeEvents.get(sessionId);

        if (current != null && COURT_EVENT_ID.equals(current.getId())) {
            if (containsAny(normalizedInput, "退朝", "散朝", "结束朝会", "结束上朝", "收束朝会")) {
                EventNodeDTO completed = buildMorningCourt("completed", 3);
                activeEvents.put(sessionId, completed);
                return completed;
            }

            if (containsAny(normalizedInput, "定夺", "裁决", "批示", "下旨", "议题", "奏议", "票拟")) {
                current.setCurrentStep(Math.max(current.getCurrentStep(), 2));
                current.setQuickActions(List.of("定夺今日奏议", "追问张居正改革利弊", "宣布退朝，命内阁整理今日奏议"));
                return current;
            }
        }

        if (containsAny(normalizedInput, "上朝", "早朝", "朝会", "奉天殿")) {
            EventNodeDTO court = buildMorningCourt("active", 1);
            activeEvents.put(sessionId, court);
            return court;
        }

        if (current != null && "regency_transition".equals(current.getId())
            && containsAny(normalizedInput, "托孤", "两宫", "辅政", "顾命")) {
            current.setCurrentStep(Math.max(current.getCurrentStep(), 2));
            current.setQuickActions(List.of("确定内阁辅政秩序", "安抚冯保", "上朝，召集群臣奏对"));
            return current;
        }

        return current;
    }

    public Optional<EventNodeDTO> getActiveEvent(String sessionId) {
        return Optional.ofNullable(activeEvents.get(sessionId));
    }

    public String buildEventContext(String sessionId) {
        return getActiveEvent(sessionId)
            .map(event -> String.format("""
当前事件节点：%s（%s）
- 节点说明：%s
- 当前进度：第 %d/%d 步
- 触发提示：%s
- 完成提示：%s
叙事要求：围绕当前节点推进。若玩家输入包含完成提示（例如“退朝”“结束朝会”），需要明确写出节点收束的结果与后续悬念。
""", event.getTitle(), event.getStatus(), event.getDescription(),
                event.getCurrentStep(), event.getSteps().size(), event.getTriggerHint(), event.getCompletionHint()))
            .orElse("当前事件节点：自由行动。可通过“上朝”“召见某人”等关键词触发新的节点。\n");
    }

    private EventNodeDTO buildMorningCourt(String status, int currentStep) {
        return new EventNodeDTO(
            COURT_EVENT_ID,
            "上朝奏对",
            "群臣齐集，奏疏、党争与新君威仪会在同一场朝会中被检验。",
            status,
            currentStep,
            List.of("宣召入殿", "听取并定夺奏议", "宣布退朝并派发后续任务"),
            status.equals("completed")
                ? List.of("召见张居正复盘朝议", "返回乾清宫休整")
                : List.of("听取今日奏议", "定夺今日奏议", "宣布退朝，命内阁整理今日奏议"),
            "输入“上朝/早朝/朝会”触发",
            "输入“宣布退朝/结束朝会/散朝”完成"
        );
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
