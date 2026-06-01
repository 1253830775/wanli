package com.wanli.repository;

import com.wanli.model.GameRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GameRelationRepository extends JpaRepository<GameRelation, Long> {

    List<GameRelation> findBySessionId(String sessionId);

    List<GameRelation> findBySourceIdAndSessionId(Long sourceId, String sessionId);

    List<GameRelation> findByTargetIdAndSessionId(Long targetId, String sessionId);

    @Query(value = "SELECT * FROM game_relations r WHERE r.session_id = :sessionId AND " +
                   "(r.source_id IN :entityIds OR r.target_id IN :entityIds)", nativeQuery = true)
    List<GameRelation> findByEntityIds(@Param("sessionId") String sessionId,
                                        @Param("entityIds") List<Long> entityIds);
}
