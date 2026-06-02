package com.wanli.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventNodeDTO {
    private String id;
    private String title;
    private String description;
    private String status;
    private Integer currentStep;
    private List<String> steps;
    private List<String> quickActions;
    private String triggerHint;
    private String completionHint;
}
