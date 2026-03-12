package com.itsm.core.domain.change;

import com.itsm.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_change_approver")
@IdClass(ChangeApproverId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeApprover {

    @Id
    @Column(name = "change_id")
    private Long changeId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "approve_order", nullable = false)
    private int approveOrder;

    @Column(name = "approve_status", nullable = false, length = 20)
    private String approveStatus;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "comment", length = 500)
    private String comment;

    @Builder
    public ChangeApprover(Long changeId, Long userId, int approveOrder) {
        this.changeId = changeId;
        this.userId = userId;
        this.approveOrder = approveOrder;
        this.approveStatus = "PENDING";
    }

    public void approve(String comment) {
        this.approveStatus = "APPROVED";
        this.approvedAt = LocalDateTime.now();
        this.comment = comment;
    }

    public void reject(String comment) {
        this.approveStatus = "REJECTED";
        this.approvedAt = LocalDateTime.now();
        this.comment = comment;
    }
}
