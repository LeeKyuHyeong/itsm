package com.itsm.core.repository.change;

import com.itsm.core.domain.change.ChangeAsset;
import com.itsm.core.domain.change.ChangeAssetId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeAssetRepository extends JpaRepository<ChangeAsset, ChangeAssetId> {
    List<ChangeAsset> findByChangeId(Long changeId);
}
