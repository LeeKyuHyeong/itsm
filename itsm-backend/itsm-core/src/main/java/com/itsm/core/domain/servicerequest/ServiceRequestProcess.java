package com.itsm.core.domain.servicerequest;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_service_request_process")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ServiceRequestProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "process_id")
    private Long processId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "process_content", nullable = false, columnDefinition = "TEXT")
    private String processContent;

    @Column(name = "is_completed", nullable = false, columnDefinition = "char(1)")
    private String isCompleted;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ServiceRequestProcess(Long requestId, Long userId, String processContent) {
        this.requestId = requestId;
        this.userId = userId;
        this.processContent = processContent;
        this.isCompleted = "N";
    }

    public void updateContent(String processContent) {
        this.processContent = processContent;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.isCompleted = "Y";
        this.completedAt = LocalDateTime.now();
    }
}
