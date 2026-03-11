package com.itsm.core.repository.user;

import com.itsm.core.domain.user.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}
