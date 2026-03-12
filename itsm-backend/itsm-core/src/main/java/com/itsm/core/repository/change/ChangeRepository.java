package com.itsm.core.repository.change;

import com.itsm.core.domain.change.Change;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChangeRepository extends JpaRepository<Change, Long> {

    @Query("SELECT c FROM Change c WHERE " +
            "(:keyword IS NULL OR c.title LIKE %:keyword% OR c.content LIKE %:keyword%) " +
            "AND (:companyId IS NULL OR c.company.companyId = :companyId) " +
            "AND (:statusCd IS NULL OR c.statusCd = :statusCd) " +
            "AND (:priorityCd IS NULL OR c.priorityCd = :priorityCd) " +
            "AND (:changeTypeCd IS NULL OR c.changeTypeCd = :changeTypeCd)")
    Page<Change> search(@Param("keyword") String keyword,
                        @Param("companyId") Long companyId,
                        @Param("statusCd") String statusCd,
                        @Param("priorityCd") String priorityCd,
                        @Param("changeTypeCd") String changeTypeCd,
                        Pageable pageable);
}
