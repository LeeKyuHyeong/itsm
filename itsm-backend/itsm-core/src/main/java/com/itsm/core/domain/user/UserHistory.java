package com.itsm.core.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_user_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(name = "user_nm", nullable = false, length = 50)
    private String userNm;

    @Column(name = "employee_no", length = 30)
    private String employeeNo;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "tel", length = 20)
    private String tel;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "changed_field", length = 100)
    private String changedField;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "batch_job_id", length = 50)
    private String batchJobId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder
    public UserHistory(Long userId, String loginId, String userNm, String employeeNo,
                       Long deptId, String email, String tel, String status,
                       String changedField, String beforeValue, String afterValue,
                       LocalDateTime validFrom, LocalDateTime validTo,
                       String batchJobId, Long createdBy) {
        this.userId = userId;
        this.loginId = loginId;
        this.userNm = userNm;
        this.employeeNo = employeeNo;
        this.deptId = deptId;
        this.email = email;
        this.tel = tel;
        this.status = status;
        this.changedField = changedField;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.batchJobId = batchJobId;
        this.createdBy = createdBy;
    }
}
