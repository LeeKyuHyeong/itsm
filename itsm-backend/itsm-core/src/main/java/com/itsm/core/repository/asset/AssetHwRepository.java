package com.itsm.core.repository.asset;

import com.itsm.core.domain.asset.AssetHw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssetHwRepository extends JpaRepository<AssetHw, Long> {

    List<AssetHw> findByWarrantyEndAtBetween(LocalDate from, LocalDate to);

    Page<AssetHw> findByStatus(String status, Pageable pageable);

    Page<AssetHw> findByCompanyCompanyId(Long companyId, Pageable pageable);

    Page<AssetHw> findByCompanyCompanyIdAndStatus(Long companyId, String status, Pageable pageable);

    @Query("SELECT a FROM AssetHw a WHERE " +
            "(:keyword IS NULL OR a.assetNm LIKE %:keyword% OR a.serialNo LIKE %:keyword% OR a.ipAddress LIKE %:keyword%) " +
            "AND (:companyId IS NULL OR a.company.companyId = :companyId) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:assetTypeCd IS NULL OR a.assetTypeCd = :assetTypeCd)")
    Page<AssetHw> search(@Param("keyword") String keyword,
                         @Param("companyId") Long companyId,
                         @Param("status") String status,
                         @Param("assetTypeCd") String assetTypeCd,
                         Pageable pageable);

    Optional<AssetHw> findBySerialNo(String serialNo);

    List<AssetHw> findByManagerUserId(Long managerId);
}
