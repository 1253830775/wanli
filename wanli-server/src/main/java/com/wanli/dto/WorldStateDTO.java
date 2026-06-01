package com.wanli.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldStateDTO {
    private Integer year;
    private String eraName;
    private Long treasury;
    private Integer publicSupport;
    private Integer militaryLoyalty;
    private String playerLocation;
}
