package com.itsm.core.repository.common;

import com.itsm.core.domain.common.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

    Optional<CommonCode> findByGroupCd(String groupCd);

    boolean existsByGroupCd(String groupCd);

    List<CommonCode> findAllByIsActive(String isActive);

    List<CommonCode> findByGroupNmContaining(String keyword);
}
