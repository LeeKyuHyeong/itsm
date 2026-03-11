package com.itsm.core.repository.common;

import com.itsm.core.domain.common.NotificationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationPolicyRepository extends JpaRepository<NotificationPolicy, Long> {

    List<NotificationPolicy> findByNotiTypeCd(String notiTypeCd);

    List<NotificationPolicy> findByIsActive(String isActive);

    List<NotificationPolicy> findByNotiTypeCdAndIsActive(String notiTypeCd, String isActive);
}
