package com.itsm.core.repository.asset;

import com.itsm.core.domain.asset.AssetSwHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetSwHistoryRepository extends JpaRepository<AssetSwHistory, Long> {

    List<AssetSwHistory> findByAssetSwIdOrderByCreatedAtDesc(Long assetSwId);
}
