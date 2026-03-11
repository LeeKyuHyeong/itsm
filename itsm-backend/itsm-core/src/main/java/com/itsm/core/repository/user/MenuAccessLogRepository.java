package com.itsm.core.repository.user;

import com.itsm.core.domain.user.MenuAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuAccessLogRepository extends JpaRepository<MenuAccessLog, Long> {
}
