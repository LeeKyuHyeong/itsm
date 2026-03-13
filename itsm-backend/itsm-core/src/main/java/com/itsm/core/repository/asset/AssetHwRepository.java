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
            "AND (:assetTypeCd IS NULL OR a.assetTypeCd = :assetTypeCd) " +
            "AND (:assetCategory IS NULL OR a.assetCategory = :assetCategory) " +
            "AND (:assetSubCategory IS NULL OR a.assetSubCategory = :assetSubCategory)")
    Page<AssetHw> search(@Param("keyword") String keyword,
                         @Param("companyId") Long companyId,
                         @Param("status") String status,
                         @Param("assetTypeCd") String assetTypeCd,
                         @Param("assetCategory") String assetCategory,
                         @Param("assetSubCategory") String assetSubCategory,
                         Pageable pageable);

    Optional<AssetHw> findBySerialNo(String serialNo);

    List<AssetHw> findByManagerUserId(Long managerId);

    @Query("SELECT a.assetCategory, COUNT(a) FROM AssetHw a WHERE a.status != 'DISPOSED' GROUP BY a.assetCategory")
    List<Object[]> countByCategory();

    @Query("SELECT a.assetSubCategory, COUNT(a) FROM AssetHw a WHERE a.assetCategory = :category AND a.status != 'DISPOSED' GROUP BY a.assetSubCategory")
    List<Object[]> countBySubCategory(@Param("category") String category);

    @Query("SELECT a.status, COUNT(a) FROM AssetHw a GROUP BY a.status")
    List<Object[]> countByStatus();
}
