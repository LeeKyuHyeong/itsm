package com.itsm.core.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_role")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_nm", nullable = false, length = 50)
    private String roleNm;

    @Column(name = "role_cd", nullable = false, length = 50, unique = true)
    private String roleCd;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder
    public Role(String roleNm, String roleCd, String description, String status, Long createdBy) {
        this.roleNm = roleNm;
        this.roleCd = roleCd;
        this.description = description;
        this.status = status != null ? status : "ACTIVE";
        this.createdBy = createdBy;
    }
}
