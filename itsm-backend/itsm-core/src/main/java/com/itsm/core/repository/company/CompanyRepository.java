package com.itsm.core.repository.company;

import com.itsm.core.domain.company.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByBizNo(String bizNo);

    boolean existsByBizNo(String bizNo);

    @Query("SELECT c FROM Company c WHERE c.status = :status")
    Page<Company> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.companyNm LIKE %:keyword% OR c.bizNo LIKE %:keyword%")
    Page<Company> search(@Param("keyword") String keyword, Pageable pageable);
}
