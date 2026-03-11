package com.itsm.core.domain.company;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_department")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "dept_nm", nullable = false, length = 100)
    private String deptNm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Builder
    public Department(String deptNm, Company company, String status) {
        this.deptNm = deptNm;
        this.company = company;
        this.status = status != null ? status : "ACTIVE";
    }

    public void update(String deptNm) {
        this.deptNm = deptNm;
    }

    public void changeStatus(String status) {
        this.status = status;
    }
}
