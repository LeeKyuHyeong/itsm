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
@Table(name = "tb_asset_hw")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetHw extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_hw_id")
    private Long assetHwId;

    @Column(name = "asset_nm", nullable = false, length = 100)
    private String assetNm;

    @Column(name = "asset_type_cd", nullable = false, length = 50)
    private String assetTypeCd;

    @Column(name = "asset_category", length = 20)
    private String assetCategory;

    @Column(name = "asset_sub_category", length = 30)
    private String assetSubCategory;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "model_nm", length = 100)
    private String modelNm;

    @Column(name = "serial_no", length = 100)
    private String serialNo;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "mac_address", length = 50)
    private String macAddress;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "introduced_at")
    private LocalDate introducedAt;

    @Column(name = "warranty_end_at")
    private LocalDate warrantyEndAt;

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
    public AssetHw(String assetNm, String assetTypeCd, String assetCategory, String assetSubCategory,
                   String manufacturer, String modelNm,
                   String serialNo, String ipAddress, String macAddress, String location,
                   LocalDate introducedAt, LocalDate warrantyEndAt, Company company,
                   User manager, String status, String description) {
        this.assetNm = assetNm;
        this.assetTypeCd = assetTypeCd;
        this.assetCategory = assetCategory != null ? assetCategory : "INFRA_HW";
        this.assetSubCategory = assetSubCategory;
        this.manufacturer = manufacturer;
        this.modelNm = modelNm;
        this.serialNo = serialNo;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.location = location;
        this.introducedAt = introducedAt;
        this.warrantyEndAt = warrantyEndAt;
        this.company = company;
        this.manager = manager;
        this.status = status != null ? status : "ACTIVE";
        this.description = description;
    }

    public void update(String assetNm, String assetTypeCd, String manufacturer, String modelNm,
                       String serialNo, String ipAddress, String macAddress, String location,
                       LocalDate introducedAt, LocalDate warrantyEndAt,
                       User manager, String description, String assetCategory, String assetSubCategory) {
        this.assetNm = assetNm;
        this.assetTypeCd = assetTypeCd;
        this.manufacturer = manufacturer;
        this.modelNm = modelNm;
        this.serialNo = serialNo;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.location = location;
        this.introducedAt = introducedAt;
        this.warrantyEndAt = warrantyEndAt;
        this.manager = manager;
        this.description = description;
        this.assetCategory = assetCategory;
        this.assetSubCategory = assetSubCategory;
    }

    public void changeStatus(String status) {
        this.status = status;
    }

    public void assignManager(User manager) {
        this.manager = manager;
    }
}
