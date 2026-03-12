package com.itsm.core.domain.report;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private ReportForm reportForm;

    @Column(name = "ref_type", nullable = false, length = 20)
    private String refType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "report_content", nullable = false, columnDefinition = "JSON")
    private String reportContent;

    @Builder
    public Report(ReportForm reportForm, String refType, Long refId, String reportContent) {
        this.reportForm = reportForm;
        this.refType = refType;
        this.refId = refId;
        this.reportContent = reportContent;
    }

    public void update(String reportContent) {
        this.reportContent = reportContent;
    }
}
