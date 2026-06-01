package com.wanli.service;

import com.wanli.model.GameSession;
import com.wanli.model.NpcProfile;
import com.wanli.repository.GameSessionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class ContextAssembler {

    private final GameSessionRepository sessionRepo;
    private final KnowledgeGraphService kgService;
    private final WorldStateService wsService;
    private final NpcService npcService;

    public ContextAssembler(GameSessionRepository sessionRepo,
                             KnowledgeGraphService kgService,
                             WorldStateService wsService,
                             NpcService npcService) {
        this.sessionRepo = sessionRepo;
        this.kgService = kgService;
        this.wsService = wsService;
        this.npcService = npcService;
    }

    public String buildSystemPrompt(String sessionId) {
        GameSession session = sessionRepo.findBySessionId(sessionId).orElseThrow();
        String stateContext = wsService.buildStateContext(sessionId);
        List<NpcProfile> npcs = npcService.getSceneNpcs(sessionId, "");
        String npcContext = npcService.buildNpcContext(npcs);

        return String.format(""" 
你是大明万历朝的文字冒险游戏 AI 叙事引擎。玩家穿越成了明神宗朱翊钧（十岁登基）。

## 世界设定
- 时间：隆庆六年（1572 年），明穆宗朱载坖病重托孤
- 顾命大臣：高拱、张居正、高仪
- 司礼监掌印太监：冯保
- 玩家拥有现代知识，可自由决策改变历史

## 叙事规则
1. 用中文创作沉浸式的历史叙事文本
2. 根据玩家的输入推进剧情，不要替玩家做决定
3. 保持历史人物的性格一致性（参考 NPC 档案）
4. 玩家的决策会引发因果连锁反应，记录在世界状态中
5. 每轮生成结束后，输出当前场景可交互的 NPC 列表（以 @NPC名 格式）
6. 如果有关键决策点，在叙事结束后提供 2-4 个选项

%s

%s

## 短期记忆（最近对话）
""", stateContext, npcContext);
    }

    public String buildNarrativePrompt(String playerInput, String history, String npcTarget) {
        StringBuilder sb = new StringBuilder();
        if (npcTarget != null && !npcTarget.isBlank()) {
            sb.append("玩家正在与 ").append(npcTarget).append(" 对话。请生成该 NPC 的回应和对白。\n");
        }
        sb.append("玩家行动：").append(playerInput).append("\n");
        sb.append("请根据以上所有上下文，生成下一段叙事。");
        return sb.toString();
    }
}
