package com.itsm.api.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeDetailResponse {

    private Long detailId;
    private String codeVal;
    private String codeNm;
    private Integer sortOrder;
    private String isActive;
    private LocalDateTime createdAt;
}
