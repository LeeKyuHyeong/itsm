package com.itsm.core.domain.asset;

import com.itsm.core.domain.BaseEntity;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tb_asset_sw")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetSw extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_sw_id")
    private Long assetSwId;

    @Column(name = "sw_nm", nullable = false, length = 100)
    private String swNm;

    @Column(name = "sw_type_cd", nullable = false, length = 50)
    private String swTypeCd;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "license_key", length = 200)
    private String licenseKey;

    @Column(name = "license_cnt")
    private Integer licenseCnt;

    @Column(name = "installed_at")
    private LocalDate installedAt;

    @Column(name = "expired_at")
    private LocalDate expiredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder
    public AssetSw(String swNm, String swTypeCd, String version, String licenseKey,
                   Integer licenseCnt, LocalDate installedAt, LocalDate expiredAt,
                   Company company, User manager, String status, String description) {
        this.swNm = swNm;
        this.swTypeCd = swTypeCd;
        this.version = version;
        this.licenseKey = licenseKey;
        this.licenseCnt = licenseCnt;
        this.installedAt = installedAt;
        this.expiredAt = expiredAt;
        this.company = company;
        this.manager = manager;
        this.status = status != null ? status : "ACTIVE";
        this.description = description;
    }

    public void update(String swNm, String swTypeCd, String version, String licenseKey,
                       Integer licenseCnt, LocalDate installedAt, LocalDate expiredAt,
                       User manager, String description) {
        this.swNm = swNm;
        this.swTypeCd = swTypeCd;
        this.version = version;
        this.licenseKey = licenseKey;
        this.licenseCnt = licenseCnt;
        this.installedAt = installedAt;
        this.expiredAt = expiredAt;
        this.manager = manager;
        this.description = description;
    }

    public void changeStatus(String status) {
        this.status = status;
    }

    public void assignManager(User manager) {
        this.manager = manager;
    }
}
