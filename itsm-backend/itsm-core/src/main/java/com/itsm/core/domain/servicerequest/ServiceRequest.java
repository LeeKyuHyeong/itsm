package com.itsm.core.domain.servicerequest;

import com.itsm.core.constant.ServiceRequestStatus;
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
@Table(name = "tb_service_request", indexes = {
        @Index(name = "idx_sr_status_cd", columnList = "status_cd"),
        @Index(name = "idx_sr_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceRequest extends BaseEntity {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            ServiceRequestStatus.RECEIVED, Set.of(ServiceRequestStatus.ASSIGNED, ServiceRequestStatus.CANCELLED, ServiceRequestStatus.REJECTED),
            ServiceRequestStatus.ASSIGNED, Set.of(ServiceRequestStatus.IN_PROGRESS, ServiceRequestStatus.RECEIVED, ServiceRequestStatus.REJECTED),
            ServiceRequestStatus.IN_PROGRESS, Set.of(ServiceRequestStatus.PENDING_COMPLETE, ServiceRequestStatus.REJECTED),
            ServiceRequestStatus.PENDING_COMPLETE, Set.of(ServiceRequestStatus.CLOSED),
            ServiceRequestStatus.CLOSED, Set.of(),
            ServiceRequestStatus.CANCELLED, Set.of(),
            ServiceRequestStatus.REJECTED, Set.of(ServiceRequestStatus.RECEIVED)
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

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "revised_scheduled_at")
    private LocalDateTime revisedScheduledAt;

    @Column(name = "schedule_change_reason", length = 500)
    private String scheduleChangeReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

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

    @Column(name = "satisfaction_score", columnDefinition = "tinyint")
    private Integer satisfactionScore;

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
        this.statusCd = ServiceRequestStatus.RECEIVED;
        this.occurredAt = occurredAt;
        this.company = company;
        this.rejectCnt = 0;
        this.receivedAt = LocalDateTime.now();
    }

    public void changeStatus(String newStatus) {
        Set<String> allowed = VALID_TRANSITIONS.getOrDefault(this.statusCd, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("유효하지 않은 상태 전이: %s → %s", this.statusCd, newStatus));
        }
        if (ServiceRequestStatus.REJECTED.equals(newStatus)) {
            this.rejectCnt++;
        }
        if (ServiceRequestStatus.PENDING_COMPLETE.equals(newStatus)) {
            LocalDateTime now = LocalDateTime.now();
            this.completedAt = now;
            this.processedAt = now;
        }
        if (ServiceRequestStatus.CLOSED.equals(newStatus)) {
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

    public void setSchedule(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public void reviseSchedule(LocalDateTime revisedAt, String reason) {
        if (this.scheduledAt == null) {
            throw new IllegalStateException("처리예정일이 설정되지 않은 상태에서는 변경할 수 없습니다.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("처리예정일 변경 사유는 필수입니다.");
        }
        this.revisedScheduledAt = revisedAt;
        this.scheduleChangeReason = reason;
    }
}
