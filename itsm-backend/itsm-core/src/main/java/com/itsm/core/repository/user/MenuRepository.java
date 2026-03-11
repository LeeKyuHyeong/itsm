package com.itsm.core.repository.user;

import com.itsm.core.domain.user.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("SELECT m FROM Menu m WHERE m.parent IS NULL AND m.status = 'ACTIVE' ORDER BY m.sortOrder")
    List<Menu> findRootMenus();

    @Query("SELECT DISTINCT rm.menuId FROM RoleMenu rm WHERE rm.roleId IN :roleIds AND rm.canRead = 'Y'")
    List<Long> findAccessibleMenuIds(@Param("roleIds") List<Long> roleIds);

    @Query("SELECT m FROM Menu m WHERE m.menuId IN :menuIds AND m.status = 'ACTIVE' ORDER BY m.sortOrder")
    List<Menu> findByMenuIds(@Param("menuIds") List<Long> menuIds);
}
