package com.itsm.core.domain.incident;

import com.itsm.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_incident_assignee")
@IdClass(IncidentAssigneeId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncidentAssignee {

    @Id
    @Column(name = "incident_id")
    private Long incidentId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Builder
    public IncidentAssignee(Long incidentId, Long userId, Long grantedBy) {
        this.incidentId = incidentId;
        this.userId = userId;
        this.grantedAt = LocalDateTime.now();
        this.grantedBy = grantedBy;
    }
}
