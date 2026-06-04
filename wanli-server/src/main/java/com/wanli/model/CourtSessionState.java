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
