package com.itsm.core.domain.incident;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_incident_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class IncidentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "incident_id", nullable = false, unique = true)
    private Long incidentId;

    @Column(name = "report_form_id", nullable = false)
    private Long reportFormId;

    @Column(name = "report_content", nullable = false, columnDefinition = "JSON")
    private String reportContent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Builder
    public IncidentReport(Long incidentId, Long reportFormId, String reportContent, Long createdBy) {
        this.incidentId = incidentId;
        this.reportFormId = reportFormId;
        this.reportContent = reportContent;
        this.createdBy = createdBy;
    }

    public void update(String reportContent, Long updatedBy) {
        this.reportContent = reportContent;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
