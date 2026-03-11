package com.itsm.core.domain.common;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_common_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CommonCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_nm", nullable = false, length = 100)
    private String groupNm;

    @Column(name = "group_cd", nullable = false, length = 50, unique = true)
    private String groupCd;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "is_active", nullable = false, length = 1)
    private String isActive;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @OneToMany(mappedBy = "commonCode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommonCodeDetail> details = new ArrayList<>();

    @Builder
    public CommonCode(String groupNm, String groupCd, String description, String isActive, Long createdBy) {
        this.groupNm = groupNm;
        this.groupCd = groupCd;
        this.description = description;
        this.isActive = isActive != null ? isActive : "Y";
        this.createdBy = createdBy;
    }

    public void update(String groupNm, String description) {
        this.groupNm = groupNm;
        this.description = description;
    }

    public void activate() {
        this.isActive = "Y";
    }

    public void deactivate() {
        this.isActive = "N";
    }
}
