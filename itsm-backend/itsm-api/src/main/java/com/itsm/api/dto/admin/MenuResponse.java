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
    private String menuUrl;
    private String icon;
    private int sortOrder;

    @Builder.Default
    private List<MenuResponse> children = new ArrayList<>();
}
