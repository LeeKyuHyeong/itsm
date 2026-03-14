package com.itsm.core.domain.board;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_board_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "board_nm", nullable = false, length = 100)
    private String boardNm;

    @Column(name = "board_nm_en", length = 100)
    private String boardNmEn;

    @Column(name = "board_type_cd", nullable = false, length = 20)
    private String boardTypeCd;

    @Column(name = "allow_ext", length = 200)
    private String allowExt;

    @Column(name = "max_file_size")
    private Integer maxFileSize;

    @Column(name = "allow_comment", nullable = false, columnDefinition = "char(1)")
    private String allowComment;

    @Column(name = "role_permission", nullable = false, columnDefinition = "JSON")
    private String rolePermission;

    @Column(name = "is_active", nullable = false, columnDefinition = "char(1)")
    private String isActive;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Builder
    public BoardConfig(String boardNm, String boardNmEn, String boardTypeCd, String allowExt, Integer maxFileSize,
                       String allowComment, String rolePermission, String isActive, Integer sortOrder) {
        this.boardNm = boardNm;
        this.boardNmEn = boardNmEn;
        this.boardTypeCd = boardTypeCd;
        this.allowExt = allowExt;
        this.maxFileSize = maxFileSize;
        this.allowComment = allowComment;
        this.rolePermission = rolePermission;
        this.isActive = isActive;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public void update(String boardNm, String boardNmEn, String boardTypeCd, String allowExt, Integer maxFileSize,
                       String allowComment, String rolePermission, String isActive, Integer sortOrder) {
        this.boardNm = boardNm;
        this.boardNmEn = boardNmEn;
        this.boardTypeCd = boardTypeCd;
        this.allowExt = allowExt;
        this.maxFileSize = maxFileSize;
        this.allowComment = allowComment;
        this.rolePermission = rolePermission;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
    }

    public void activate() {
        this.isActive = "Y";
    }

    public void deactivate() {
        this.isActive = "N";
    }
}
