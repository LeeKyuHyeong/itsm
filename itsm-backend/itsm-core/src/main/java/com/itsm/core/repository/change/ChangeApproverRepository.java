package com.itsm.core.repository.change;

import com.itsm.core.domain.change.ChangeApprover;
import com.itsm.core.domain.change.ChangeApproverId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeApproverRepository extends JpaRepository<ChangeApprover, ChangeApproverId> {
    List<ChangeApprover> findByChangeIdOrderByApproveOrderAsc(Long changeId);
}
