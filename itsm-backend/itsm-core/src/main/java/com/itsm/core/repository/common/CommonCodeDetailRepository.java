package com.itsm.core.repository.common;

import com.itsm.core.domain.common.CommonCodeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeDetailRepository extends JpaRepository<CommonCodeDetail, Long> {

    List<CommonCodeDetail> findByCommonCode_GroupIdAndIsActiveOrderBySortOrder(Long groupId, String isActive);

    List<CommonCodeDetail> findByCommonCode_GroupIdOrderBySortOrder(Long groupId);

    boolean existsByCommonCode_GroupIdAndCodeVal(Long groupId, String codeVal);
}
