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
@Table(name = "npc_profiles")
public class NpcProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "npc_id", nullable = false)
    private String npcId;

    @Column(nullable = false)
    private String name;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(columnDefinition = "TEXT")
    private String traits;

    @Column(name = "knowledge_boundary", columnDefinition = "TEXT")
    private String knowledgeBoundary;

    @Column(name = "attitude_json", columnDefinition = "TEXT")
    private String attitudeJson;

    @Column(name = "status", nullable = false)
    private String status;

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
