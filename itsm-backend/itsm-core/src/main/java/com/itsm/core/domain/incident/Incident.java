package com.itsm.core.domain.incident;

import com.itsm.core.domain.BaseEntity;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "tb_incident")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incident extends BaseEntity {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "RECEIVED", Set.of("IN_PROGRESS", "REJECTED"),
            "IN_PROGRESS", Set.of("COMPLETED", "REJECTED"),
            "COMPLETED", Set.of("CLOSED"),
            "REJECTED", Set.of("RECEIVED"),
            "CLOSED", Set.of()
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "incident_id")
    private Long incidentId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "incident_type_cd", nullable = false, length = 50)
    private String incidentTypeCd;

    @Column(name = "priority_cd", nullable = false, length = 20)
    private String priorityCd;

    @Column(name = "status_cd", nullable = false, length = 20)
    private String statusCd;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "sla_deadline_at")
    private LocalDateTime slaDeadlineAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_manager_id")
    private User mainManager;

    @Column(name = "process_content", columnDefinition = "TEXT")
    private String processContent;

    @Builder
    public Incident(String title, String content, String incidentTypeCd, String priorityCd,
                    LocalDateTime occurredAt, Company company, User mainManager) {
        this.title = title;
        this.content = content;
        this.incidentTypeCd = incidentTypeCd;
        this.priorityCd = priorityCd;
        this.statusCd = "RECEIVED";
        this.occurredAt = occurredAt;
        this.company = company;
        this.mainManager = mainManager;
    }

    public void changeStatus(String newStatus) {
        Set<String> allowed = VALID_TRANSITIONS.getOrDefault(this.statusCd, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("유효하지 않은 상태 전이: %s → %s", this.statusCd, newStatus));
        }
        this.statusCd = newStatus;

        if ("COMPLETED".equals(newStatus)) {
            this.completedAt = LocalDateTime.now();
        } else if ("CLOSED".equals(newStatus)) {
            this.closedAt = LocalDateTime.now();
        }
    }

    public void update(String title, String content, String incidentTypeCd,
                       String priorityCd, LocalDateTime occurredAt) {
        this.title = title;
        this.content = content;
        this.incidentTypeCd = incidentTypeCd;
        this.priorityCd = priorityCd;
        this.occurredAt = occurredAt;
    }

    public void assignMainManager(User manager) {
        this.mainManager = manager;
    }

    public void writeProcessContent(String processContent) {
        this.processContent = processContent;
    }

    public void setSlaDeadline(LocalDateTime deadline) {
        this.slaDeadlineAt = deadline;
    }

    public void extendSlaDeadline(int hours) {
        if (this.slaDeadlineAt != null) {
            this.slaDeadlineAt = this.slaDeadlineAt.plusHours(hours);
        }
    }
}
