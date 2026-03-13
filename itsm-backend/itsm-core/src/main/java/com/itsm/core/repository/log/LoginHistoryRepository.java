package com.itsm.core.repository.log;

import com.itsm.core.domain.log.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByUserId(Long userId);
    List<LoginHistory> findByLoginAtBetween(LocalDateTime from, LocalDateTime to);
    long countByLoginAtBetween(LocalDateTime from, LocalDateTime to);
    List<LoginHistory> findByUserIdAndLogoutAtIsNull(Long userId);
}
