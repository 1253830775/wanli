package com.wanli.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionResponse {
    private String sessionId;
    private String message;
    private EventNodeDTO activeEvent;
    private WorldStateDTO worldState;
}
