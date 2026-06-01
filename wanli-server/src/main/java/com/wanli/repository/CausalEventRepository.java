package com.wanli.repository;

import com.wanli.model.CausalEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CausalEventRepository extends JpaRepository<CausalEvent, Long> {

    List<CausalEvent> findBySessionIdOrderByRoundNumberDesc(String sessionId);

    List<CausalEvent> findBySessionIdAndRoundNumberLessThanEqual(String sessionId, Integer roundNumber);
}
