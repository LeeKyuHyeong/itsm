package com.itsm.core.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_role_menu")
@IdClass(RoleMenuId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RoleMenu {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "menu_id")
    private Long menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", insertable = false, updatable = false)
    private Menu menu;

    @Column(name = "can_read", nullable = false, length = 1)
    private String canRead;

    @Column(name = "can_write", nullable = false, length = 1)
    private String canWrite;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    public RoleMenu(Long roleId, Long menuId, String canRead, String canWrite, Long createdBy) {
        this.roleId = roleId;
        this.menuId = menuId;
        this.canRead = canRead != null ? canRead : "Y";
        this.canWrite = canWrite != null ? canWrite : "N";
        this.createdBy = createdBy;
    }
}
