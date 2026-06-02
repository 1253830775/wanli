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
@Table(name = "world_states")
public class WorldState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "`year`")
    private Integer year;

    @Column(name = "era_name")
    private String eraName;

    @Column(name = "treasury")
    private Long treasury;

    @Column(name = "public_support")
    private Integer publicSupport;

    @Column(name = "military_loyalty")
    private Integer militaryLoyalty;

    @Column(name = "imperial_authority")
    private Integer imperialAuthority;

    @Column(name = "player_location")
    private String playerLocation;

    @Column(name = "state_json", columnDefinition = "TEXT")
    private String stateJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
