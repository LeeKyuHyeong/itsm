package com.itsm.core.domain.servicerequest;

import com.itsm.core.domain.BaseEntity;
import com.itsm.core.domain.company.Company;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "tb_service_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceRequest extends BaseEntity {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "RECEIVED", Set.of("ASSIGNED", "CANCELLED", "REJECTED"),
            "ASSIGNED", Set.of("IN_PROGRESS", "RECEIVED", "REJECTED"),
            "IN_PROGRESS", Set.of("PENDING_COMPLETE", "REJECTED"),
            "PENDING_COMPLETE", Set.of("CLOSED"),
            "CLOSED", Set.of(),
            "CANCELLED", Set.of(),
            "REJECTED", Set.of("RECEIVED")
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "request_type_cd", nullable = false, length = 50)
    private String requestTypeCd;

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

    @Column(name = "reject_cnt", nullable = false)
    private int rejectCnt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "satisfaction_score")
    private int satisfactionScore;

    @Column(name = "satisfaction_comment", length = 500)
    private String satisfactionComment;

    @Column(name = "satisfaction_submitted_at")
    private LocalDateTime satisfactionSubmittedAt;

    @Builder
    public ServiceRequest(String title, String content, String requestTypeCd, String priorityCd,
                          LocalDateTime occurredAt, Company company) {
        this.title = title;
        this.content = content;
        this.requestTypeCd = requestTypeCd;
        this.priorityCd = priorityCd;
        this.statusCd = "RECEIVED";
        this.occurredAt = occurredAt;
        this.company = company;
        this.rejectCnt = 0;
    }

    public void changeStatus(String newStatus) {
        Set<String> allowed = VALID_TRANSITIONS.getOrDefault(this.statusCd, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("유효하지 않은 상태 전이: %s → %s", this.statusCd, newStatus));
        }
        if ("REJECTED".equals(newStatus)) {
            this.rejectCnt++;
        }
        if ("PENDING_COMPLETE".equals(newStatus)) {
            this.completedAt = LocalDateTime.now();
        }
        if ("CLOSED".equals(newStatus)) {
            this.closedAt = LocalDateTime.now();
        }
        this.statusCd = newStatus;
    }

    public void update(String title, String content, String requestTypeCd,
                       String priorityCd, LocalDateTime occurredAt) {
        this.title = title;
        this.content = content;
        this.requestTypeCd = requestTypeCd;
        this.priorityCd = priorityCd;
        this.occurredAt = occurredAt;
    }

    public void submitSatisfaction(int score, String comment) {
        this.satisfactionScore = score;
        this.satisfactionComment = comment;
        this.satisfactionSubmittedAt = LocalDateTime.now();
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
