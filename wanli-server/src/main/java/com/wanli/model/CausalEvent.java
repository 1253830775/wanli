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
@Table(name = "causal_events")
public class CausalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "trigger_description", columnDefinition = "TEXT", nullable = false)
    private String triggerDescription;

    @Column(name = "effects", columnDefinition = "TEXT", nullable = false)
    private String effects;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
