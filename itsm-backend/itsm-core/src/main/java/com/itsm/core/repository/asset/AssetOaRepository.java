package com.itsm.core.repository.asset;

import com.itsm.core.domain.asset.AssetOa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssetOaRepository extends JpaRepository<AssetOa, Long> {

    List<AssetOa> findByWarrantyEndAtBetween(LocalDate from, LocalDate to);

    @Query("SELECT a FROM AssetOa a WHERE " +
            "(:keyword IS NULL OR a.assetNm LIKE %:keyword% OR a.serialNo LIKE %:keyword% OR a.ipAddress LIKE %:keyword%) " +
            "AND (:companyId IS NULL OR a.company.companyId = :companyId) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:assetTypeCd IS NULL OR a.assetTypeCd = :assetTypeCd)")
    Page<AssetOa> search(@Param("keyword") String keyword,
                         @Param("companyId") Long companyId,
                         @Param("status") String status,
                         @Param("assetTypeCd") String assetTypeCd,
                         Pageable pageable);

    Optional<AssetOa> findBySerialNo(String serialNo);

    List<AssetOa> findByManagerUserId(Long managerId);

    @Query("SELECT a.status, COUNT(a) FROM AssetOa a GROUP BY a.status")
    List<Object[]> countByStatus();

    @Query("SELECT a.assetTypeCd, COUNT(a) FROM AssetOa a WHERE a.status != 'DISPOSED' GROUP BY a.assetTypeCd")
    List<Object[]> countByTypeCd();
}
