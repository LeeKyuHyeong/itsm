package com.itsm.core.repository.asset;

import com.itsm.core.domain.asset.AssetHwHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetHwHistoryRepository extends JpaRepository<AssetHwHistory, Long> {

    List<AssetHwHistory> findByAssetHwIdOrderByCreatedAtDesc(Long assetHwId);
}
