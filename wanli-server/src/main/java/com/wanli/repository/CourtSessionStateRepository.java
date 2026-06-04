package com.wanli.repository;

import com.wanli.model.CourtSessionState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CourtSessionStateRepository extends JpaRepository<CourtSessionState, Long> {

    Optional<CourtSessionState> findBySessionIdAndIsActive(String sessionId, Boolean isActive);

    Optional<CourtSessionState> findTopBySessionIdOrderByCourtNumberDesc(String sessionId);
}
