package com.itsm.core.repository.company;

import com.itsm.core.domain.company.DepartmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentHistoryRepository extends JpaRepository<DepartmentHistory, Long> {

    List<DepartmentHistory> findByDeptIdOrderByCreatedAtDesc(Long deptId);
}
