package com.itsm.core.domain.change;

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
@Table(name = "tb_change")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Change extends BaseEntity {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "DRAFT", Set.of("APPROVAL_REQUESTED", "CANCELLED"),
            "APPROVAL_REQUESTED", Set.of("APPROVED", "REJECTED"),
            "APPROVED", Set.of("IN_PROGRESS"),
            "IN_PROGRESS", Set.of("COMPLETED"),
            "COMPLETED", Set.of("CLOSED"),
            "CLOSED", Set.of(),
            "REJECTED", Set.of("DRAFT"),
            "CANCELLED", Set.of()
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_id")
    private Long changeId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "change_type_cd", nullable = false, length = 50)
    private String changeTypeCd;

    @Column(name = "priority_cd", nullable = false, length = 20)
    private String priorityCd;

    @Column(name = "status_cd", nullable = false, length = 20)
    private String statusCd;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "rollback_plan", columnDefinition = "TEXT")
    private String rollbackPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Builder
    public Change(String title, String content, String changeTypeCd, String priorityCd,
                  LocalDateTime scheduledAt, String rollbackPlan, Company company) {
        this.title = title;
        this.content = content;
        this.changeTypeCd = changeTypeCd;
        this.priorityCd = priorityCd;
        this.statusCd = "DRAFT";
        this.scheduledAt = scheduledAt;
        this.rollbackPlan = rollbackPlan;
        this.company = company;
    }

    public void changeStatus(String newStatus) {
        Set<String> allowed = VALID_TRANSITIONS.getOrDefault(this.statusCd, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("유효하지 않은 상태 전이: %s → %s", this.statusCd, newStatus));
        }
        this.statusCd = newStatus;
    }

    public void update(String title, String content, String changeTypeCd,
                       String priorityCd, LocalDateTime scheduledAt, String rollbackPlan) {
        this.title = title;
        this.content = content;
        this.changeTypeCd = changeTypeCd;
        this.priorityCd = priorityCd;
        this.scheduledAt = scheduledAt;
        this.rollbackPlan = rollbackPlan;
    }
}
