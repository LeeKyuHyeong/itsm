package com.itsm.core.domain.common;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_id")
    private Long notiId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "noti_type_cd", nullable = false, length = 50)
    private String notiTypeCd;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "ref_type", length = 20)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(Long userId, String notiTypeCd, String title,
                        String content, String refType, Long refId) {
        this.userId = userId;
        this.notiTypeCd = notiTypeCd;
        this.title = title;
        this.content = content;
        this.refType = refType;
        this.refId = refId;
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }
}
