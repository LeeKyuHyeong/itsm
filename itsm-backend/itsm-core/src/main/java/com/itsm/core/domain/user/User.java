package com.itsm.core.domain.user;

import com.itsm.core.domain.BaseEntity;
import com.itsm.core.domain.company.Department;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", length = 50, unique = true)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "user_nm", nullable = false, length = 50)
    private String userNm;

    @Column(name = "employee_no", length = 30)
    private String employeeNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "tel", length = 20)
    private String tel;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "pwd_changed_at")
    private LocalDateTime pwdChangedAt;

    @Column(name = "login_fail_cnt", nullable = false)
    private int loginFailCnt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles = new ArrayList<>();

    @Builder
    public User(String loginId, String password, String userNm, String employeeNo,
                Department department, String email, String tel, String status) {
        this.loginId = loginId;
        this.password = password;
        this.userNm = userNm;
        this.employeeNo = employeeNo;
        this.department = department;
        this.email = email;
        this.tel = tel;
        this.status = status != null ? status : "ACTIVE";
        this.validFrom = LocalDateTime.now();
        this.loginFailCnt = 0;
    }

    public void update(String userNm, String employeeNo, Department department, String email, String tel) {
        this.userNm = userNm;
        this.employeeNo = employeeNo;
        this.department = department;
        this.email = email;
        this.tel = tel;
    }

    public void changeStatus(String status) {
        this.status = status;
        if ("DELETED".equals(status)) {
            this.loginId = "DELETED_" + this.userId + "_" + this.loginId;
            this.validTo = LocalDateTime.now();
        }
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.pwdChangedAt = LocalDateTime.now();
    }

    public void recordLoginSuccess() {
        this.lastLoginAt = LocalDateTime.now();
        this.loginFailCnt = 0;
    }

    public void recordLoginFailure() {
        this.loginFailCnt++;
    }

    public void resetLoginFailCount() {
        this.loginFailCnt = 0;
    }

    public boolean isLocked() {
        return "LOCKED".equals(this.status);
    }

    public void lock() {
        this.status = "LOCKED";
    }

    public void unlock() {
        this.status = "ACTIVE";
        this.loginFailCnt = 0;
    }
}
