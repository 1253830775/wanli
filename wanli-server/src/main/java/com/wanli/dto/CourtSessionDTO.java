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
