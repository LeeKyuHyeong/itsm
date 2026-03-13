package com.itsm.core.repository.log;

import com.itsm.core.domain.log.MenuAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SimMenuAccessLogRepository extends JpaRepository<MenuAccessLog, Long> {
    List<MenuAccessLog> findByUserId(Long userId);
    List<MenuAccessLog> findByAccessedAtBetween(LocalDateTime from, LocalDateTime to);
    long countByAccessedAtBetween(LocalDateTime from, LocalDateTime to);
}
