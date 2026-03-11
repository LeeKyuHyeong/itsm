package com.itsm.core.repository.common;

import com.itsm.core.domain.common.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {

    Optional<SlaPolicy> findByCompanyIdAndPriorityCd(Long companyId, String priorityCd);

    Optional<SlaPolicy> findByCompanyIdIsNullAndPriorityCd(String priorityCd);

    List<SlaPolicy> findByCompanyId(Long companyId);

    List<SlaPolicy> findByCompanyIdIsNull();

    List<SlaPolicy> findByIsActive(String isActive);
}
