package com.itsm.core.domain.company;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_nm", nullable = false, length = 100)
    private String companyNm;

    @Column(name = "biz_no", length = 20, unique = true)
    private String bizNo;

    @Column(name = "ceo_nm", length = 50)
    private String ceoNm;

    @Column(name = "tel", length = 20)
    private String tel;

    @Column(name = "default_pm_id")
    private Long defaultPmId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Builder
    public Company(String companyNm, String bizNo, String ceoNm, String tel, Long defaultPmId, String status) {
        this.companyNm = companyNm;
        this.bizNo = bizNo;
        this.ceoNm = ceoNm;
        this.tel = tel;
        this.defaultPmId = defaultPmId;
        this.status = status != null ? status : "ACTIVE";
    }

    public void update(String companyNm, String bizNo, String ceoNm, String tel, Long defaultPmId) {
        this.companyNm = companyNm;
        this.bizNo = bizNo;
        this.ceoNm = ceoNm;
        this.tel = tel;
        this.defaultPmId = defaultPmId;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
