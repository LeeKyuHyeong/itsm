package com.itsm.core.repository.change;

import com.itsm.core.domain.change.ChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeHistoryRepository extends JpaRepository<ChangeHistory, Long> {
    List<ChangeHistory> findByChangeIdOrderByCreatedAtDesc(Long changeId);
}
