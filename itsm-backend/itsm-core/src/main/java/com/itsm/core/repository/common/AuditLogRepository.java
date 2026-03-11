package com.itsm.core.repository.common;

import com.itsm.core.domain.common.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTargetTypeAndTargetId(String targetType, Long targetId);

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
}
