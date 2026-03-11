package com.itsm.core.repository.user;

import com.itsm.core.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    @Query("SELECT u FROM User u WHERE u.status <> 'DELETED'")
    Page<User> findAllActive(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.status <> 'DELETED' AND " +
            "(u.userNm LIKE %:keyword% OR u.loginId LIKE %:keyword% OR u.email LIKE %:keyword%)")
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.department.deptId = :deptId AND u.status <> 'DELETED'")
    Page<User> findByDepartment(@Param("deptId") Long deptId, Pageable pageable);
}
