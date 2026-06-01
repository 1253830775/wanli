package com.wanli.repository;

import com.wanli.model.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GameEntityRepository extends JpaRepository<GameEntity, Long> {

    List<GameEntity> findBySessionId(String sessionId);

    List<GameEntity> findByNameContainingAndSessionId(String name, String sessionId);

    @Query("SELECT e FROM GameEntity e WHERE e.sessionId = :sessionId AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<GameEntity> searchByText(@Param("sessionId") String sessionId, @Param("query") String query);
}
