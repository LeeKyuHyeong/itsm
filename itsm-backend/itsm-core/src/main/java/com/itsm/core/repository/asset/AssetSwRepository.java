package com.itsm.core.repository.asset;

import com.itsm.core.domain.asset.AssetSw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetSwRepository extends JpaRepository<AssetSw, Long> {

    Page<AssetSw> findByStatus(String status, Pageable pageable);

    Page<AssetSw> findByCompanyCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT s FROM AssetSw s WHERE " +
            "(:keyword IS NULL OR s.swNm LIKE %:keyword% OR s.licenseKey LIKE %:keyword%) " +
            "AND (:companyId IS NULL OR s.company.companyId = :companyId) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (:swTypeCd IS NULL OR s.swTypeCd = :swTypeCd) " +
            "AND (:assetCategory IS NULL OR s.assetCategory = :assetCategory) " +
            "AND (:assetSubCategory IS NULL OR s.assetSubCategory = :assetSubCategory)")
    Page<AssetSw> search(@Param("keyword") String keyword,
                         @Param("companyId") Long companyId,
                         @Param("status") String status,
                         @Param("swTypeCd") String swTypeCd,
                         @Param("assetCategory") String assetCategory,
                         @Param("assetSubCategory") String assetSubCategory,
                         Pageable pageable);

    List<AssetSw> findByManagerUserId(Long managerId);

    @Query("SELECT s.assetCategory, COUNT(s) FROM AssetSw s WHERE s.status != 'DISPOSED' GROUP BY s.assetCategory")
    List<Object[]> countByCategory();

    @Query("SELECT s.assetSubCategory, COUNT(s) FROM AssetSw s WHERE s.assetCategory = :category AND s.status != 'DISPOSED' GROUP BY s.assetSubCategory")
    List<Object[]> countBySubCategory(@Param("category") String category);

    @Query("SELECT s.status, COUNT(s) FROM AssetSw s GROUP BY s.status")
    List<Object[]> countByStatus();
}
