package com.itsm.core.domain.log;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "SimMenuAccessLog")
@Table(name = "tb_sim_menu_access_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuAccessLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_log_id")
    private Long accessLogId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "menu_path", nullable = false, length = 200)
    private String menuPath;

    @Column(name = "menu_nm", length = 100)
    private String menuNm;

    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;

    @Builder
    public MenuAccessLog(Long userId, String menuPath, String menuNm, LocalDateTime accessedAt) {
        this.userId = userId;
        this.menuPath = menuPath;
        this.menuNm = menuNm;
        this.accessedAt = accessedAt;
    }
}
