package com.itsm.api.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardConfigResponse {

    private Long boardId;
    private String boardNm;
    private String boardNmEn;
    private String boardTypeCd;
    private String allowExt;
    private Integer maxFileSize;
    private String allowComment;
    private String rolePermission;
    private String isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
