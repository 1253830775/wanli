package com.wanli.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NarrativeEvent {
    private String type;
    private String sessionId;
    private String content;
    private String token;
    private WorldStateDTO worldState;
    private List<String> sceneCharacters;
    private EventNodeDTO activeEvent;
    private CourtSessionDTO courtSession;
    private String error;
}
