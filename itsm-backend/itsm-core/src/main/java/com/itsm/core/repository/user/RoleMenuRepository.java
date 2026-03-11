package com.itsm.core.repository.user;

import com.itsm.core.domain.user.RoleMenu;
import com.itsm.core.domain.user.RoleMenuId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, RoleMenuId> {

    List<RoleMenu> findByRoleId(Long roleId);
}
