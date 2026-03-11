package com.itsm.core.repository.user;

import com.itsm.core.domain.user.UserRole;
import com.itsm.core.domain.user.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUserId(Long userId);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.userId = :userId")
    List<UserRole> findByUserIdWithRole(@Param("userId") Long userId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
