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
