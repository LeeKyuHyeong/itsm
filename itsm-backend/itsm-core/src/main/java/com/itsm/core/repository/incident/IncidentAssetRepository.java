package com.itsm.core.repository.incident;

import com.itsm.core.domain.incident.IncidentAsset;
import com.itsm.core.domain.incident.IncidentAssetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentAssetRepository extends JpaRepository<IncidentAsset, IncidentAssetId> {

    List<IncidentAsset> findByIncidentId(Long incidentId);

    List<IncidentAsset> findByAssetTypeAndAssetId(String assetType, Long assetId);

    @Query("SELECT ia.assetType, ia.assetId, COUNT(ia) FROM IncidentAsset ia " +
            "WHERE ia.createdAt >= :since " +
            "GROUP BY ia.assetType, ia.assetId " +
            "HAVING COUNT(ia) >= :minCount")
    List<Object[]> findRepeatIncidentAssets(@Param("since") LocalDateTime since,
                                            @Param("minCount") long minCount);
}
