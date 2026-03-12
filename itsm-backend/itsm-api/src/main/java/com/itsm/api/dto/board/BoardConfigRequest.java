package com.itsm.api.dto.board;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardConfigRequest {

    @NotBlank(message = "게시판명은 필수입니다.")
    private String boardNm;

    @NotBlank(message = "게시판유형코드는 필수입니다.")
    private String boardTypeCd;

    private String allowExt;

    private Integer maxFileSize;

    private String allowComment;

    private String rolePermission;

    private String isActive;

    private Integer sortOrder;
}
