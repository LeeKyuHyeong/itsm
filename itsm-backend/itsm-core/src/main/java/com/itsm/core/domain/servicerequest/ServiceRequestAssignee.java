package com.itsm.core.domain.servicerequest;

import com.itsm.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_service_request_assignee")
@IdClass(ServiceRequestAssigneeId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceRequestAssignee {

    @Id
    @Column(name = "request_id")
    private Long requestId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "process_status", nullable = false, length = 20)
    private String processStatus;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Builder
    public ServiceRequestAssignee(Long requestId, Long userId, Long grantedBy) {
        this.requestId = requestId;
        this.userId = userId;
        this.processStatus = "PENDING";
        this.grantedAt = LocalDateTime.now();
        this.grantedBy = grantedBy;
    }

    public void changeProcessStatus(String status) {
        this.processStatus = status;
    }
}
