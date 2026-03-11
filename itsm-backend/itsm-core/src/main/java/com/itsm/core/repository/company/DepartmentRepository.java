package com.itsm.core.repository.company;

import com.itsm.core.domain.company.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByCompany_CompanyIdAndStatus(Long companyId, String status);

    List<Department> findByCompany_CompanyId(Long companyId);
}
