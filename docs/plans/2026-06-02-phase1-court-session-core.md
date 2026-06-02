# Phase 1: Court Session Core Loop

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make a complete court session playable end-to-end: topics are presented, player can inquire and rule, state changes are applied, and NPC attitudes evolve.

**Architecture:** Add a `CourtSessionService` that manages the court session state machine (report → inquiry → ruling → reaction). Rewrite `ContextAssembler` to inject court-specific prompts. Add a `StateChangeParser` to extract structured state changes from AI output. Wire everything into `NarrativeService`.

**Tech Stack:** Java 17, Spring Boot 3.2, React 18, TypeScript 5.6

---

## File Structure

### Backend (wanli-server/src/main/java/com/wanli/)

| Action | File | Responsibility |
|--------|------|----------------|
| Modify | `model/WorldState.java` | Add `imperialAuthority` field |
| Modify | `model/NpcProfile.java` | Add `faction` and `relations` fields |
| Create | `model/CourtSessionState.java` | JPA entity for court session state persistence |
| Create | `dto/CourtSessionDTO.java` | DTO for court session state |
| Create | `dto/StateChangeDTO.java` | DTO for parsed state changes |
| Modify | `dto/WorldStateDTO.java` | Add `imperialAuthority` field |
| Modify | `dto/NarrativeEvent.java` | Add `courtSession` field |
| Create | `repository/CourtSessionStateRepository.java` | Repository for court session state |
| Create | `service/StateChangeParser.java` | Parse structured state changes from AI output |
| Create | `service/CourtSessionService.java` | Court session state machine and topic management |
| Modify | `service/ContextAssembler.java` | Rewrite prompt building for court sessions |
| Modify | `service/NarrativeService.java` | Integrate court session flow + state change application |
| Modify | `service/NpcService.java` | Add relation matrix methods |
| Modify | `service/GameService.java` | Initialize new fields, set imperialAuthority |
| Modify | `service/WorldStateService.java` | Handle imperialAuthority in state context |
| Modify | `controller/GameController.java` | Add court session state endpoint |

### Frontend (wanli-client/src/)

| Action | File | Responsibility |
|--------|------|----------------|
| Modify | `types/index.ts` | Add CourtSessionState, update WorldState |
| Modify | `components/StatusPanel.tsx` | Replace militaryLoyalty with imperialAuthority |
| Create | `components/CourtSessionPanel.tsx` | Display court phase, topic, progress |
| Modify | `App.tsx` | Integrate court session state, update header actions |
| Modify | `App.css` | Add court session panel styles |
| Modify | `services/api.ts` | Handle court session state in SSE events |

---

### Task 1: Data Model Updates

**Files:**
- Modify: `wanli-server/src/main/java/com/wanli/model/WorldState.java`
- Modify: `wanli-server/src/main/java/com/wanli/model/NpcProfile.java`
- Modify: `wanli-server/src/main/java/com/wanli/dto/WorldStateDTO.java`
- Modify: `wanli-server/src/main/java/com/wanli/dto/NarrativeEvent.java`
- Create: `wanli-server/src/main/java/com/wanli/model/CourtSessionState.java`
- Create: `wanli-server/src/main/java/com/wanli/dto/CourtSessionDTO.java`
- Create: `wanli-server/src/main/java/com/wanli/dto/StateChangeDTO.java`
- Create: `wanli-server/src/main/java/com/wanli/repository/CourtSessionStateRepository.java`

- [ ] **Step 1: Update WorldState model — add imperialAuthority**

In `WorldState.java`, add a new field after `militaryLoyalty`:

```java
@Column(name = "imperial_authority")
private Integer imperialAuthority;
```

- [ ] **Step 2: Update WorldStateDTO — add imperialAuthority**

In `WorldStateDTO.java`, add:

```java
private Integer imperialAuthority;
```

- [ ] **Step 3: Update NpcProfile model — add faction and relations**

In `NpcProfile.java`, add two new fields after `status`:

```java
@Column(name = "faction")
private String faction;

@Column(name = "relations", columnDefinition = "TEXT")
private String relations;

@Column(name = "alive")
private Boolean alive;

@Column(name = "historical_death_year")
private Integer historicalDeathYear;
```

- [ ] **Step 4: Create CourtSessionState JPA entity**

Create `CourtSessionState.java`:

```java
package com.wanli.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "court_session_states")
public class CourtSessionState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "court_number", nullable = false)
    private Integer courtNumber;

    @Column(name = "phase", nullable = false)
    private String phase;

    @Column(name = "current_topic_index", nullable = false)
    private Integer currentTopicIndex;

    @Column(name = "topics_json", columnDefinition = "TEXT")
    private String topicsJson;

    @Column(name = "inquiry_count", nullable = false)
    private Integer inquiryCount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 5: Create CourtSessionDTO**

Create `CourtSessionDTO.java`:

```java
package com.wanli.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtSessionDTO {
    private Integer courtNumber;
    private String phase;
    private Integer currentTopicIndex;
    private List<TopicDTO> topics;
    private Integer inquiryCount;
    private Boolean isActive;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicDTO {
        private String title;
        private String reporter;
        private String description;
        private String historicalNote;
        private String rulingStatus;
    }
}
```

- [ ] **Step 6: Create StateChangeDTO**

Create `StateChangeDTO.java`:

```java
package com.wanli.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateChangeDTO {
    private Long treasury;
    private Integer publicSupport;
    private Integer imperialAuthority;
    private Map<String, Integer> npcAttitudes;
    private Map<String, Integer> npcRelations;
}
```

- [ ] **Step 7: Create CourtSessionStateRepository**

Create `CourtSessionStateRepository.java`:

```java
package com.wanli.repository;

import com.wanli.model.CourtSessionState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CourtSessionStateRepository extends JpaRepository<CourtSessionState, Long> {

    Optional<CourtSessionState> findBySessionIdAndIsActive(String sessionId, Boolean isActive);

    Optional<CourtSessionState> findTopBySessionIdOrderByCourtNumberDesc(String sessionId);
}
```

- [ ] **Step 8: Update NarrativeEvent DTO — add courtSession field**

In `NarrativeEvent.java`, add:

```java
private CourtSessionDTO courtSession;
```

- [ ] **Step 9: Verify compilation**

Run: `mvn compile` in `wanli-server/`
Expected: BUILD SUCCESS

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat: add data model for court session system"
```

---

### Task 2: State Change Parser

**Files:**
- Create: `wanli-server/src/main/java/com/wanli/service/StateChangeParser.java`
- Create: `wanli-server/src/test/java/com/wanli/service/StateChangeParserTest.java`

- [ ] **Step 1: Write the failing test**

Create `StateChangeParserTest.java`:

```java
package com.wanli.service;

import com.wanli.dto.StateChangeDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StateChangeParserTest {

    private final StateChangeParser parser = new StateChangeParser();

    @Test
    void parsesStateChangeBlock() {
        String narrative = """
            你步入乾清宫，张居正正在等候。
            【状态变更】{"treasury":-50000,"publicSupport":3,"imperialAuthority":5,"npcAttitudes":{"zhang_juzheng":10,"gao_gong":-15}}
            朝会就此结束。""";
        StateChangeDTO result = parser.parse(narrative);
        assertNotNull(result);
        assertEquals(-50000L, result.getTreasury());
        assertEquals(3, result.getPublicSupport());
        assertEquals(5, result.getImperialAuthority());
        assertEquals(10, result.getNpcAttitudes().get("zhang_juzheng"));
        assertEquals(-15, result.getNpcAttitudes().get("gao_gong"));
    }

    @Test
    void returnsNullWhenNoBlock() {
        String narrative = "你步入乾清宫，一切如常。";
        StateChangeDTO result = parser.parse(narrative);
        assertNull(result);
    }

    @Test
    void stripsStateChangeBlockFromNarrative() {
        String narrative = "前文。\n【状态变更】{\"treasury\":-50000}\n后文。";
        String cleaned = parser.stripStateChangeBlock(narrative);
        assertFalse(cleaned.contains("【状态变更】"));
        assertTrue(cleaned.contains("前文。"));
        assertTrue(cleaned.contains("后文。"));
    }

    @Test
    void handlesMalformedJson() {
        String narrative = "【状态变更】{invalid json}";
        StateChangeDTO result = parser.parse(narrative);
        assertNull(result);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl . -Dtest=StateChangeParserTest`
Expected: FAIL — `StateChangeParser` class not found

- [ ] **Step 3: Implement StateChangeParser**

Create `StateChangeParser.java`:

```java
package com.wanli.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.dto.StateChangeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StateChangeParser {

    private static final Logger log = LoggerFactory.getLogger(StateChangeParser.class);
    private static final Pattern STATE_CHANGE_PATTERN =
        Pattern.compile("【状态变更】\\s*(\\{[^】]*\\})");

    private final ObjectMapper objectMapper;

    public StateChangeParser() {
        this.objectMapper = new ObjectMapper();
    }

    public StateChangeDTO parse(String narrative) {
        if (narrative == null || narrative.isBlank()) return null;
        Matcher matcher = STATE_CHANGE_PATTERN.matcher(narrative);
        if (!matcher.find()) return null;
        String json = matcher.group(1);
        try {
            return objectMapper.readValue(json, StateChangeDTO.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse state change JSON: {}", json, e);
            return null;
        }
    }

    public String stripStateChangeBlock(String narrative) {
        if (narrative == null) return null;
        return STATE_CHANGE_PATTERN.matcher(narrative).replaceAll("").trim();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -pl . -Dtest=StateChangeParserTest`
Expected: All 4 tests PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add StateChangeParser for extracting structured changes from AI output"
```

---

### Task 3: Court Session Service

**Files:**
- Create: `wanli-server/src/main/java/com/wanli/service/CourtSessionService.java`

- [ ] **Step 1: Implement CourtSessionService**

Create `CourtSessionService.java`:

```java
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
            return "当前状态：非朝会时间。玩家可输入"上朝"开启朝会。\n";
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
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile` in `wanli-server/`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: add CourtSessionService for managing court session state machine"
```

---

### Task 4: Rewrite ContextAssembler

**Files:**
- Modify: `wanli-server/src/main/java/com/wanli/service/ContextAssembler.java`

- [ ] **Step 1: Rewrite ContextAssembler**

Replace the entire content of `ContextAssembler.java` with:

```java
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
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile` in `wanli-server/`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: rewrite ContextAssembler with court session prompt structure"
```

---

### Task 5: Wire State Changes into NarrativeService

**Files:**
- Modify: `wanli-server/src/main/java/com/wanli/service/NarrativeService.java`
- Modify: `wanli-server/src/main/java/com/wanli/service/NpcService.java`
- Modify: `wanli-server/src/main/java/com/wanli/service/WorldStateService.java`

- [ ] **Step 1: Update WorldStateService — add imperialAuthority support**

In `WorldStateService.java`, update the `buildStateContext` method to include imperialAuthority:

Find the line that formats the state context string and add imperial authority. The method should output:

```
--- 世界状态 ---
- 纪年：{year}年 · {eraName}
- 国库：{treasury} 两
- 民望：{publicSupport}
- 帝威：{imperialAuthority}
- 所在：{playerLocation}
```

Also update the `toDTO` method to include `imperialAuthority`:

```java
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
```

- [ ] **Step 2: Update NpcService — add relation matrix support**

In `NpcService.java`, add a method to update NPC relations:

```java
public void updateRelation(String npc1Id, String npc2Id, String sessionId, int delta) {
    updateRelationForNpc(npc1Id, npc2Id, sessionId, delta);
    updateRelationForNpc(npc2Id, npc1Id, sessionId, delta);
}

private void updateRelationForNpc(String npcId, String targetId, String sessionId, int delta) {
    npcRepo.findByNpcIdAndSessionId(npcId, sessionId).ifPresent(profile -> {
        try {
            Map<String, Integer> relations;
            if (profile.getRelations() != null && !profile.getRelations().isBlank()) {
                relations = objectMapper.readValue(profile.getRelations(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Integer.class));
            } else {
                relations = new HashMap<>();
            }
            int current = relations.getOrDefault(targetId, 0);
            relations.put(targetId, Math.max(-100, Math.min(100, current + delta)));
            profile.setRelations(objectMapper.writeValueAsString(relations));
            npcRepo.save(profile);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to update relation", e);
        }
    });
}
```

Also add `import java.util.HashMap;` at the top.

Update `buildNpcContext` to include faction and relations:

```java
public String buildNpcContext(List<NpcProfile> npcs) {
    StringBuilder sb = new StringBuilder();
    sb.append("\n--- NPC 状态 ---\n");
    for (NpcProfile npc : npcs) {
        sb.append(String.format("- %s（%s）: 性格%s, 态度: %s, 派系: %s",
            npc.getName(), npc.getStatus(), npc.getTraits(),
            npc.getAttitudeJson(),
            npc.getFaction() != null ? npc.getFaction() : "无"));
        if (npc.getRelations() != null && !npc.getRelations().isBlank()) {
            sb.append(String.format(", 关系: %s", npc.getRelations()));
        }
        sb.append("\n");
    }
    return sb.toString();
}
```

- [ ] **Step 3: Rewrite NarrativeService — integrate court session and state changes**

Replace the entire content of `NarrativeService.java` with:

```java
package com.wanli.service;

import com.wanli.dto.CourtSessionDTO;
import com.wanli.dto.NarrativeEvent;
import com.wanli.dto.StateChangeDTO;
import com.wanli.model.CourtSessionState;
import com.wanli.model.GameSession;
import com.wanli.model.NpcProfile;
import com.wanli.repository.GameSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NarrativeService {

    private static final Logger log = LoggerFactory.getLogger(NarrativeService.class);

    private final GameSessionRepository sessionRepo;
    private final LLMClient llmClient;
    private final ContextAssembler contextAssembler;
    private final WorldStateService wsService;
    private final KnowledgeGraphService kgService;
    private final NpcService npcService;
    private final CourtSessionService courtSessionService;
    private final StateChangeParser stateChangeParser;

    public NarrativeService(GameSessionRepository sessionRepo,
                             LLMClient llmClient,
                             ContextAssembler contextAssembler,
                             WorldStateService wsService,
                             KnowledgeGraphService kgService,
                             NpcService npcService,
                             CourtSessionService courtSessionService,
                             StateChangeParser stateChangeParser) {
        this.sessionRepo = sessionRepo;
        this.llmClient = llmClient;
        this.contextAssembler = contextAssembler;
        this.wsService = wsService;
        this.kgService = kgService;
        this.npcService = npcService;
        this.courtSessionService = courtSessionService;
        this.stateChangeParser = stateChangeParser;
    }

    public Flux<NarrativeEvent> generateNarrative(String sessionId, String playerInput, String targetNpc) {
        return Flux.create(sink -> {
            GameSession session = sessionRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

            String history = session.getNarrativeHistory() != null ? session.getNarrativeHistory() : "";
            int newRound = session.getCurrentRound() + 1;

            handleCourtSessionFlow(sessionId, playerInput);

            var courtState = courtSessionService.getActiveSession(sessionId);

            wsService.getLatestState(sessionId).ifPresent(ws -> {
                NarrativeEvent header = new NarrativeEvent();
                header.setType("state");
                header.setSessionId(sessionId);
                header.setWorldState(wsService.toDTO(ws));

                List<NpcProfile> sceneNpcs = npcService.getSceneNpcs(sessionId, "");
                header.setSceneCharacters(sceneNpcs.stream()
                    .map(n -> "@" + n.getName())
                    .collect(Collectors.toList()));

                courtState.ifPresent(cs -> header.setCourtSession(courtSessionService.toDTO(cs)));

                sink.next(header);
            });

            String systemPrompt = contextAssembler.buildSystemPrompt(sessionId);
            String userPrompt = contextAssembler.buildNarrativePrompt(playerInput, history, targetNpc);
            Map<String, Object> subGraph = kgService.getSubGraph(sessionId, playerInput, 50);
            String kgContext = "--- 相关知识图谱 ---\n" + subGraph.toString();
            String fullSystemPrompt = systemPrompt + "\n" + kgContext;

            StringBuilder fullNarrative = new StringBuilder();

            llmClient.streamChat(fullSystemPrompt, userPrompt)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                    token -> {
                        fullNarrative.append(token);
                        NarrativeEvent event = new NarrativeEvent();
                        event.setType("token");
                        event.setToken(token);
                        event.setSessionId(sessionId);
                        sink.next(event);
                    },
                    error -> {
                        NarrativeEvent err = new NarrativeEvent();
                        err.setType("error");
                        err.setError(error.getMessage());
                        sink.next(err);
                        sink.complete();
                    },
                    () -> {
                        String narrative = fullNarrative.toString();

                        StateChangeDTO stateChange = stateChangeParser.parse(narrative);
                        if (stateChange != null) {
                            applyStateChange(sessionId, newRound, stateChange);
                            narrative = stateChangeParser.stripStateChangeBlock(narrative);
                        }

                        String cleanedNarrative = narrative;
                        String updatedHistory = history + "\n[第" + newRound + "轮] " + playerInput + "\n" + cleanedNarrative;
                        session.setNarrativeHistory(updatedHistory);
                        session.setCurrentRound(newRound);
                        sessionRepo.save(session);

                        wsService.getLatestState(sessionId).ifPresent(ws -> {
                            NarrativeEvent stateUpdate = new NarrativeEvent();
                            stateUpdate.setType("state");
                            stateUpdate.setSessionId(sessionId);
                            stateUpdate.setWorldState(wsService.toDTO(ws));

                            List<NpcProfile> sceneNpcs = npcService.getSceneNpcs(sessionId, "");
                            stateUpdate.setSceneCharacters(sceneNpcs.stream()
                                .map(n -> "@" + n.getName())
                                .collect(Collectors.toList()));

                            courtSessionService.getActiveSession(sessionId)
                                .ifPresent(cs -> stateUpdate.setCourtSession(courtSessionService.toDTO(cs)));

                            sink.next(stateUpdate);
                        });

                        NarrativeEvent done = new NarrativeEvent();
                        done.setType("done");
                        done.setSessionId(sessionId);
                        sink.next(done);
                        sink.complete();
                    }
                );
        });
    }

    private void handleCourtSessionFlow(String sessionId, String playerInput) {
        String normalized = playerInput == null ? "" : playerInput;
        var activeSession = courtSessionService.getActiveSession(sessionId);

        if (activeSession.isEmpty()) {
            if (containsAny(normalized, "上朝", "早朝", "朝会", "奉天殿")) {
                courtSessionService.startCourtSession(sessionId);
            }
        } else {
            CourtSessionState state = activeSession.get();
            String phase = state.getPhase();

            if (containsAny(normalized, "退朝", "散朝", "结束朝会", "结束上朝")) {
                courtSessionService.endCourtSession(sessionId);
                return;
            }

            courtSessionService.advancePhase(sessionId, playerInput);
        }
    }

    private void applyStateChange(String sessionId, int round, StateChangeDTO change) {
        wsService.getLatestState(sessionId).ifPresent(ws -> {
            if (change.getTreasury() != null) {
                ws.setTreasury(ws.getTreasury() + change.getTreasury());
            }
            if (change.getPublicSupport() != null) {
                ws.setPublicSupport(clamp(ws.getPublicSupport() + change.getPublicSupport(), 0, 100));
            }
            if (change.getImperialAuthority() != null) {
                ws.setImperialAuthority(clamp(ws.getImperialAuthority() + change.getImperialAuthority(), 0, 100));
            }
            wsService.saveState(sessionId, round, wsService.toDTO(ws));
        });

        if (change.getNpcAttitudes() != null) {
            change.getNpcAttitudes().forEach((npcId, delta) -> {
                npcService.updateAttitude(npcId, sessionId, delta, "朝会裁决");
            });
        }

        if (change.getNpcRelations() != null) {
            change.getNpcRelations().forEach((pair, delta) -> {
                String[] ids = pair.split("-");
                if (ids.length == 2) {
                    npcService.updateRelation(ids[0], ids[1], sessionId, delta);
                }
            });
        }

        kgService.addCausalEvent(sessionId,
            "朝会裁决 (第" + round + "轮)",
            change.toString(),
            round);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}
```

- [ ] **Step 4: Verify compilation**

Run: `mvn compile` in `wanli-server/`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: integrate court session flow and state change application into NarrativeService"
```

---

### Task 6: Update GameService Initialization

**Files:**
- Modify: `wanli-server/src/main/java/com/wanli/service/GameService.java`

- [ ] **Step 1: Update GameService — set imperialAuthority and NPC factions**

In `GameService.java`, update the `createSession` method:

1. In the `WorldStateDTO` initialization, add:
```java
startingState.setImperialAuthority(50);
```

2. Update the `initNpcs` method to include faction and relations data. Replace the `saveNpc` calls:

```java
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
```

3. Update the `saveNpc` method signature to accept new parameters:

```java
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
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile` in `wanli-server/`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: initialize NPC factions, relations, and imperial authority"
```

---

### Task 7: Update GameController

**Files:**
- Modify: `wanli-server/src/main/java/com/wanli/controller/GameController.java`

- [ ] **Step 1: Add CourtSessionService dependency and court session endpoint**

Update `GameController.java`:

1. Add `CourtSessionService` to constructor injection
2. Add a new endpoint:

```java
@GetMapping("/{sessionId}/court")
public CourtSessionDTO getCourtSession(@PathVariable String sessionId) {
    return courtSessionService.getActiveSession(sessionId)
        .map(courtSessionService::toDTO)
        .orElse(null);
}
```

3. Update the `createSession` response message to reflect the new court session system:

```java
return new CreateSessionResponse(session.getSessionId(),
    "你以十岁天子的身份醒在乾清宫。\n" +
    "隆庆六年（1572 年），先帝病重托孤，顾命大臣与司礼监都在等待新君的态度。\n" +
    "输入"上朝"开启第一次朝会，在高拱、张居正、冯保之间做出你的第一个决策。",
    null,
    wsService.getLatestState(session.getSessionId()).map(wsService::toDTO).orElse(new WorldStateDTO()));
```

Note: Remove the `eventNodeService` field and `activeEvent` from the response since we're replacing event nodes with court sessions.

- [ ] **Step 2: Verify compilation**

Run: `mvn compile` in `wanli-server/`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: update GameController with court session endpoint"
```

---

### Task 8: Frontend — Types and Status Panel

**Files:**
- Modify: `wanli-client/src/types/index.ts`
- Modify: `wanli-client/src/components/StatusPanel.tsx`

- [ ] **Step 1: Update TypeScript types**

In `types/index.ts`, update `WorldState`:

```typescript
export interface WorldState {
  year: number;
  eraName: string;
  treasury: number;
  publicSupport: number;
  imperialAuthority: number;
  playerLocation: string;
}
```

Add `CourtSessionState` type:

```typescript
export interface CourtSessionState {
  courtNumber: number;
  phase: string;
  currentTopicIndex: number;
  topics: Topic[];
  inquiryCount: number;
  isActive: boolean;
}

export interface Topic {
  title: string;
  reporter: string;
  description: string;
  historicalNote: string;
  rulingStatus: string;
}
```

Update `NarrativeEvent`:

```typescript
export interface NarrativeEvent {
  type: 'token' | 'state' | 'done' | 'error';
  sessionId: string;
  content?: string;
  token?: string;
  worldState?: WorldState;
  sceneCharacters?: string[];
  courtSession?: CourtSessionState;
  error?: string;
}
```

Update `CreateSessionResponse`:

```typescript
export interface CreateSessionResponse {
  sessionId: string;
  message: string;
  courtSession?: CourtSessionState;
  worldState?: WorldState;
}
```

- [ ] **Step 2: Update StatusPanel — replace militaryLoyalty with imperialAuthority**

In `StatusPanel.tsx`, replace the `军心` meter with `帝威`:

```tsx
<div className="stat-row">
  <span className="stat-label">帝威</span>
  <Meter value={state.imperialAuthority} color="#c07728" />
</div>
```

- [ ] **Step 3: Verify TypeScript compilation**

Run: `npx tsc --noEmit` in `wanli-client/`
Expected: No errors

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: update frontend types and StatusPanel for imperial authority"
```

---

### Task 9: Frontend — Court Session Panel

**Files:**
- Create: `wanli-client/src/components/CourtSessionPanel.tsx`
- Modify: `wanli-client/src/App.css`

- [ ] **Step 1: Create CourtSessionPanel component**

Create `CourtSessionPanel.tsx`:

```tsx
import React from 'react';
import { CourtSessionState } from '../types';

interface CourtSessionPanelProps {
  courtSession: CourtSessionState | null;
  onSend: (text: string) => void;
  disabled: boolean;
}

const phaseLabels: Record<string, string> = {
  report: '奏报',
  inquiry: '问对',
  ruling: '裁决',
  reaction: '反应',
  ended: '已结束'
};

const phaseDescriptions: Record<string, string> = {
  report: '大臣正在上奏议题',
  inquiry: '向大臣追问细节',
  ruling: '等待皇帝裁决',
  reaction: '大臣们对裁决的反应',
  ended: '本次朝会已结束'
};

export const CourtSessionPanel: React.FC<CourtSessionPanelProps> = ({
  courtSession, onSend, disabled
}) => {
  if (!courtSession || !courtSession.isActive) {
    return (
      <section className="side-card court-card">
        <div className="panel-title">朝会</div>
        <div className="court-idle">当前非朝会时间</div>
        <div className="court-actions">
          <button
            className="quick-chip"
            type="button"
            disabled={disabled}
            onClick={() => onSend('上朝，召集群臣奏对')}
          >
            开启朝会
          </button>
        </div>
      </section>
    );
  }

  const currentTopic = courtSession.topics[courtSession.currentTopicIndex];
  const phases = ['report', 'inquiry', 'ruling', 'reaction'];
  const currentPhaseIndex = phases.indexOf(courtSession.phase);

  return (
    <section className="side-card court-card">
      <div className="panel-title">第 {courtSession.courtNumber} 次朝会</div>

      <div className="court-phases">
        {phases.map((phase, index) => (
          <div
            key={phase}
            className={`court-phase ${index <= currentPhaseIndex ? 'active' : ''} ${index === currentPhaseIndex ? 'current' : ''}`}
          >
            <span className="phase-dot">{index + 1}</span>
            <span className="phase-label">{phaseLabels[phase]}</span>
          </div>
        ))}
      </div>

      <div className="court-phase-desc">
        {phaseDescriptions[courtSession.phase]}
      </div>

      {currentTopic && (
        <div className="court-topic">
          <div className="topic-title">{currentTopic.title}</div>
          <div className="topic-reporter">奏报：{currentTopic.reporter}</div>
          {currentTopic.description && (
            <div className="topic-desc">{currentTopic.description}</div>
          )}
        </div>
      )}

      <div className="court-info">
        <span>问对轮数：{courtSession.inquiryCount}</span>
        <span>议题：{courtSession.currentTopicIndex + 1}/{Math.max(courtSession.topics.length, 1)}</span>
      </div>

      <div className="court-actions">
        {courtSession.phase === 'inquiry' && (
          <button
            className="quick-chip"
            type="button"
            disabled={disabled}
            onClick={() => onSend('朕意已决，请诸位静候裁决')}
          >
            进入裁决
          </button>
        )}
        {courtSession.phase !== 'ended' && (
          <button
            className="quick-chip"
            type="button"
            disabled={disabled}
            onClick={() => onSend('宣布退朝，今日朝会到此结束')}
          >
            退朝
          </button>
        )}
      </div>
    </section>
  );
};
```

- [ ] **Step 2: Add court session panel CSS**

Add to `App.css`:

```css
.court-card {
  background: linear-gradient(145deg, #1a2a1a, #2a4a2a);
  color: #e8f4e8;
}

.court-card .panel-title {
  color: #7cb87c;
}

.court-idle {
  color: #8aaa8a;
  margin-bottom: 12px;
}

.court-phases {
  display: flex;
  gap: 4px;
  margin-bottom: 12px;
}

.court-phase {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0.4;
  font-size: 13px;
}

.court-phase.active {
  opacity: 0.7;
}

.court-phase.current {
  opacity: 1;
  font-weight: 700;
}

.phase-dot {
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(255,255,255,0.12);
  font-size: 11px;
  font-weight: 900;
}

.court-phase.active .phase-dot {
  background: #7cb87c;
  color: #1a2a1a;
}

.phase-label {
  font-size: 12px;
}

.court-phase-desc {
  color: #a8cca8;
  font-size: 13px;
  margin-bottom: 12px;
}

.court-topic {
  border: 1px solid rgba(124, 184, 124, 0.2);
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
}

.topic-title {
  font-size: 16px;
  font-weight: 800;
  margin-bottom: 4px;
}

.topic-reporter {
  color: #7cb87c;
  font-size: 12px;
  margin-bottom: 4px;
}

.topic-desc {
  color: #c8e8c8;
  font-size: 13px;
  line-height: 1.6;
}

.court-info {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #8aaa8a;
  margin-bottom: 12px;
}

.court-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.court-card .quick-chip {
  border-color: rgba(124, 184, 124, 0.3);
  color: #7cb87c;
  background: rgba(124, 184, 124, 0.1);
}
```

- [ ] **Step 3: Verify TypeScript compilation**

Run: `npx tsc --noEmit` in `wanli-client/`
Expected: No errors

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add CourtSessionPanel component with phase progress display"
```

---

### Task 10: Frontend Integration

**Files:**
- Modify: `wanli-client/src/App.tsx`
- Modify: `wanli-client/src/services/api.ts`

- [ ] **Step 1: Update api.ts — handle courtSession in SSE events**

In `api.ts`, update the `streamNarrative` function's `case 'state'` handler:

```typescript
case 'state':
  if (event.worldState) {
    onState(
      event.worldState,
      event.sceneCharacters || [],
      event.courtSession
    );
  }
  break;
```

Update the `onState` callback signature in the function parameters:

```typescript
export function streamNarrative(
  input: PlayerInput,
  onToken: (token: string) => void,
  onState: (state: WorldState, characters: string[], courtSession?: CourtSessionState) => void,
  onDone: () => void,
  onError: (err: string) => void
): AbortController {
```

Add `CourtSessionState` to the imports from `../types`.

- [ ] **Step 2: Update App.tsx — integrate court session state**

In `App.tsx`:

1. Add import for `CourtSessionPanel` and `CourtSessionState` type:

```tsx
import { CourtSessionPanel } from './components/CourtSessionPanel';
import { CourtSessionState, EventNode, WorldState } from './types';
```

2. Add state for court session:

```tsx
const [courtSession, setCourtSession] = React.useState<CourtSessionState | null>(null);
```

3. Update `handleStart` to remove `activeEvent` references:

```tsx
const handleStart = async (playerName: string) => {
  try {
    const res = await createSession(playerName);
    setSessionId(res.sessionId);
    setNarrative(res.message);
    setWorldState(res.worldState || null);
    setCourtSession(res.courtSession || null);
    setStarted(true);
  } catch (err: any) {
    setError('无法连接服务器，请确保后端已启动。');
  }
};
```

4. Update `handleSend` to handle court session state:

```tsx
const handleSend = (text: string) => {
  if (!sessionId || isStreaming) return;

  setIsStreaming(true);
  setError('');
  setNarrative(prev => `${prev}\n\n【旨意】${text}\n`);

  streamNarrative(
    { sessionId, text },
    (token) => {
      setNarrative(prev => prev + token);
    },
    (state, characters, court) => {
      setWorldState(state);
      setSceneCharacters(characters);
      setCourtSession(court || null);
    },
    () => {
      setIsStreaming(false);
    },
    (err) => {
      setError(err);
      setIsStreaming(false);
    }
  );
};
```

5. Replace `EventNodePanel` with `CourtSessionPanel` in the sidebar:

```tsx
<aside className="side-column">
  <CourtSessionPanel courtSession={courtSession} onSend={handleSend} disabled={isStreaming} />
  <StatusPanel state={worldState} />
  <CharacterList characters={sceneCharacters} onSelect={handleSelectNpc} />
</aside>
```

6. Remove the `activeEvent` state and `EventNodePanel` import since we're replacing it.

7. Update the header quick actions:

```tsx
<div className="hero-actions" aria-label="快捷行动">
  <button className="quick-chip" type="button" disabled={isStreaming} onClick={() => handleSend('上朝，召集群臣奏对')}>
    上朝
  </button>
  <button className="quick-chip" type="button" disabled={isStreaming} onClick={() => handleSend('罢朝，今日不上朝')}>
    罢朝
  </button>
  <button className="quick-chip" type="button" disabled={isStreaming} onClick={() => handleSend('宣布退朝，今日朝会到此结束')}>
    退朝
  </button>
</div>
```

8. Update the header kicker to show dynamic year from worldState:

```tsx
<div className="app-kicker">
  {worldState ? `${worldState.eraName} · ${worldState.year}年` : '隆庆六年 · 乾清宫'}
</div>
```

- [ ] **Step 3: Verify TypeScript compilation**

Run: `npx tsc --noEmit` in `wanli-client/`
Expected: No errors

- [ ] **Step 4: Verify frontend build**

Run: `npm run build` in `wanli-client/`
Expected: Build successful

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: integrate court session panel into main game UI"
```

---

### Task 11: Integration Testing

- [ ] **Step 1: Start the backend**

Run: `mvn spring-boot:run` in `wanli-server/`
Expected: Application starts on port 8080

- [ ] **Step 2: Start the frontend**

Run: `npm run dev` in `wanli-client/`
Expected: Dev server starts, proxies to backend

- [ ] **Step 3: Test session creation**

Open browser to `http://localhost:5173`, enter a name, click "踏入大明".
Expected: Opening narrative appears, world state panel shows imperialAuthority = 50.

- [ ] **Step 4: Test court session start**

Click "上朝" button.
Expected: Court session panel shows phase "奏报", AI generates a court scene with a minister presenting a topic.

- [ ] **Step 5: Test inquiry phase**

Type `@张居正 此事先生有何看法？` and submit.
Expected: AI generates Zhang Juzheng's response. Court session panel advances to "问对" phase.

- [ ] **Step 6: Test ruling phase**

Type `朕意已决，准奏推行` and submit.
Expected: AI generates ruling scene. State changes are applied (world state panel updates). Court session panel advances to "裁决" then "反应".

- [ ] **Step 7: Test court session end**

Type `宣布退朝` and submit.
Expected: AI generates end-of-court narrative with historical comparison. Court session panel returns to idle state.

- [ ] **Step 8: Verify state persistence**

Check that world state values (treasury, publicSupport, imperialAuthority) have changed from initial values after rulings.

- [ ] **Step 9: Commit any fixes**

```bash
git add -A
git commit -m "fix: integration test fixes for court session system"
```
