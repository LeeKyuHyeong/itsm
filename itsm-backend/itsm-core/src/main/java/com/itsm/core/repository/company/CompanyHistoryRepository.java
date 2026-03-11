package com.itsm.core.repository.company;

import com.itsm.core.domain.company.CompanyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyHistoryRepository extends JpaRepository<CompanyHistory, Long> {

    List<CompanyHistory> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
}
