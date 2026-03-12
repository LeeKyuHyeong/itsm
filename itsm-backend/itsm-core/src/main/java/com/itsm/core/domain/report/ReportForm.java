package com.itsm.core.domain.report;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_report_form")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportForm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "form_id")
    private Long formId;

    @Column(name = "form_nm", nullable = false, length = 100)
    private String formNm;

    @Column(name = "form_type_cd", nullable = false, length = 50)
    private String formTypeCd;

    @Column(name = "form_schema", nullable = false, columnDefinition = "JSON")
    private String formSchema;

    @Column(name = "is_active", nullable = false, columnDefinition = "char(1)")
    private String isActive;

    @Builder
    public ReportForm(String formNm, String formTypeCd, String formSchema, String isActive) {
        this.formNm = formNm;
        this.formTypeCd = formTypeCd;
        this.formSchema = formSchema;
        this.isActive = isActive != null ? isActive : "Y";
    }

    public void update(String formNm, String formTypeCd, String formSchema, String isActive) {
        this.formNm = formNm;
        this.formTypeCd = formTypeCd;
        this.formSchema = formSchema;
        this.isActive = isActive;
    }
}
