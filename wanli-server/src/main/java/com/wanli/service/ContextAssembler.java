package com.wanli.service;

import com.wanli.model.NpcProfile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContextAssembler {

    private final WorldStateService wsService;
    private final NpcService npcService;
    private final CourtSessionService courtSessionService;
    private final KnowledgeGraphService kgService;

    public ContextAssembler(WorldStateService wsService,
                             NpcService npcService,
                             CourtSessionService courtSessionService,
                             KnowledgeGraphService kgService) {
        this.wsService = wsService;
        this.npcService = npcService;
        this.courtSessionService = courtSessionService;
        this.kgService = kgService;
    }

    public String buildSystemPrompt(String sessionId) {
        String stateContext = wsService.buildStateContext(sessionId);
        List<NpcProfile> npcs = npcService.getSceneNpcs(sessionId, "");
        String npcContext = npcService.buildNpcContext(npcs);
        String courtContext = courtSessionService.buildCourtContext(sessionId);

        return String.format("""
你是大明万历朝的文字冒险游戏 AI 叙事引擎。玩家穿越成了明神宗朱翊钧（十岁登基）。

## 世界设定
- 玩家拥有现代知识，可自由决策改变历史
- 历史事件是素材而非剧本，玩家的决策可以阻止、推迟、改变任何历史事件
- NPC 有各自的立场和利益，同一议题不同大臣态度不同

## 叙事规则
1. 用中文创作沉浸式的历史叙事文本，文风参考历史小说
2. 根据玩家的输入推进剧情，不要替玩家做决定
3. 保持历史人物的性格一致性（参考 NPC 档案）
4. 朝堂冲突保持克制——通过措辞和态度体现暗流涌动，而非当庭争吵
5. 裁决阶段结束后，必须在叙事末尾输出状态变更块
6. 每次朝会结束时，附上"史书对照"

## 状态变更输出格式
在裁决阶段的叙事末尾，必须输出：
【状态变更】{"treasury":数值变化,"publicSupport":数值变化,"imperialAuthority":数值变化,"npcAttitudes":{"npcId":态度变化值}}
- treasury: 国库变化（两），如 -50000 表示花费 5 万两
- publicSupport: 民望变化（-100~100）
- imperialAuthority: 帝威变化（-100~100）
- npcAttitudes: NPC 态度变化（-100~100），key 为 NPC 的英文 ID

%s

%s

%s
""", stateContext, npcContext, courtContext);
    }

    public String buildNarrativePrompt(String playerInput, String history, String npcTarget) {
        StringBuilder sb = new StringBuilder();

        if (history != null && !history.isBlank()) {
            sb.append("## 近期对话记录\n");
            sb.append(history).append("\n\n");
        }

        if (npcTarget != null && !npcTarget.isBlank()) {
            sb.append("玩家正在与 ").append(npcTarget).append(" 对话。请生成该 NPC 的回应和对白。\n");
        }

        sb.append("玩家行动：").append(playerInput).append("\n");
        sb.append("请根据以上所有上下文，生成下一段叙事。");
        return sb.toString();
    }
}
