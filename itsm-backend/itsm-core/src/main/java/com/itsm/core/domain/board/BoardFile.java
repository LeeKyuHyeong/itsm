package com.itsm.core.domain.board;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_board_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BoardFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "original_nm", nullable = false, length = 255)
    private String originalNm;

    @Column(name = "saved_nm", nullable = false, length = 255)
    private String savedNm;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "extension", nullable = false, length = 20)
    private String extension;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder
    public BoardFile(Long postId, String originalNm, String savedNm, String filePath,
                     Long fileSize, String extension, Long createdBy) {
        this.postId = postId;
        this.originalNm = originalNm;
        this.savedNm = savedNm;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.extension = extension;
        this.createdBy = createdBy;
    }
}
