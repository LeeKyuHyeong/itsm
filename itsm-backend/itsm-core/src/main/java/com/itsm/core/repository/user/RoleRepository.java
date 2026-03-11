package com.itsm.core.repository.user;

import com.itsm.core.domain.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleCd(String roleCd);

    List<Role> findByStatus(String status);
}
