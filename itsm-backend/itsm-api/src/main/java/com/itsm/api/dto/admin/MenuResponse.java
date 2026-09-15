package com.itsm.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {
    private Long menuId;
    private String menuNm;
    private String menuNmEn;
    private String menuUrl;
    private String icon;
    private int sortOrder;
    /** 프론트 MenuManageView 가 노출 여부 배지에 읽는 값 — 과거 응답에 없어 항상 "사용" 으로 보였다 (2026-09-16 P3) */
    private String isVisible;
    @Builder.Default
    private List<MenuResponse> children = new ArrayList<>();
}
