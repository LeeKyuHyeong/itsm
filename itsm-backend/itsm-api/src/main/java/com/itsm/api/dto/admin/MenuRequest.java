package com.itsm.api.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메뉴 생성/수정 요청. 필드명은 프론트 MenuManageView 가 보내는 payload(name/nameEn/path/icon/sortOrder/parentId)와 같다.
 * 2026-09-16 전수조사 P3: 화면은 있었지만 이 요청을 받는 엔드포인트가 없었다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {

    @NotBlank(message = "메뉴명은 필수입니다.")
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String nameEn;

    @Size(max = 200)
    private String path;

    @Size(max = 50)
    private String icon;

    private Integer sortOrder;

    private Long parentId;
}
