package com.wanli.repository;

import com.wanli.model.NpcProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NpcProfileRepository extends JpaRepository<NpcProfile, Long> {

    Optional<NpcProfile> findByNpcIdAndSessionId(String npcId, String sessionId);

    List<NpcProfile> findBySessionId(String sessionId);

    List<NpcProfile> findBySessionIdAndStatus(String sessionId, String status);
}
