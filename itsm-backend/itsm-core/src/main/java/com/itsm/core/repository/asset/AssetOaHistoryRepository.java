package com.itsm.core.repository.asset;

import com.itsm.core.domain.asset.AssetOaHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetOaHistoryRepository extends JpaRepository<AssetOaHistory, Long> {

    List<AssetOaHistory> findByAssetOaIdOrderByCreatedAtDesc(Long assetOaId);
}
