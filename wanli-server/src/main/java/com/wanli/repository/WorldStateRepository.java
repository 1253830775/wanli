package com.wanli.repository;

import com.wanli.model.WorldState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorldStateRepository extends JpaRepository<WorldState, Long> {

    Optional<WorldState> findTopBySessionIdOrderByRoundNumberDesc(String sessionId);

    Optional<WorldState> findBySessionIdAndRoundNumber(String sessionId, Integer roundNumber);
}
