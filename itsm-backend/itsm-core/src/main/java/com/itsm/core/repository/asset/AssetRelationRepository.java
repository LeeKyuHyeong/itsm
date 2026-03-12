package com.itsm.core.repository.asset;

import com.itsm.core.domain.asset.AssetRelation;
import com.itsm.core.domain.asset.AssetRelationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRelationRepository extends JpaRepository<AssetRelation, AssetRelationId> {

    List<AssetRelation> findByAssetHwIdAndRemovedAtIsNull(Long assetHwId);

    List<AssetRelation> findByAssetSwIdAndRemovedAtIsNull(Long assetSwId);

    List<AssetRelation> findByAssetHwId(Long assetHwId);

    List<AssetRelation> findByAssetSwId(Long assetSwId);
}
