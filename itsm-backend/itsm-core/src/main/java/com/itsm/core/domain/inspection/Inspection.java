package com.itsm.core.domain.inspection;

import com.itsm.core.domain.BaseEntity;
import com.itsm.core.domain.company.Company;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "tb_inspection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inspection extends BaseEntity {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "SCHEDULED", Set.of("IN_PROGRESS", "ON_HOLD"),
            "IN_PROGRESS", Set.of("COMPLETED", "ON_HOLD"),
            "ON_HOLD", Set.of("SCHEDULED", "IN_PROGRESS"),
            "COMPLETED", Set.of("CLOSED"),
            "CLOSED", Set.of()
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_id")
    private Long inspectionId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "inspection_type_cd", nullable = false, length = 50)
    private String inspectionTypeCd;

    @Column(name = "status_cd", nullable = false, length = 20)
    private String statusCd;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDate scheduledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder
    public Inspection(String title, String inspectionTypeCd, LocalDate scheduledAt,
                      Company company, Long managerId, String description) {
        this.title = title;
        this.inspectionTypeCd = inspectionTypeCd;
        this.statusCd = "SCHEDULED";
        this.scheduledAt = scheduledAt;
        this.company = company;
        this.managerId = managerId;
        this.description = description;
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
        }
        if ("CLOSED".equals(newStatus)) {
            this.closedAt = LocalDateTime.now();
        }
    }

    public void update(String title, String inspectionTypeCd, LocalDate scheduledAt, String description) {
        this.title = title;
        this.inspectionTypeCd = inspectionTypeCd;
        this.scheduledAt = scheduledAt;
        this.description = description;
    }

    public void updateManager(Long managerId) {
        this.managerId = managerId;
    }
}
