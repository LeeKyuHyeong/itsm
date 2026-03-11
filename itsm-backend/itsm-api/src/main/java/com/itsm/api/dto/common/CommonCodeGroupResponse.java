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
public class CommonCodeGroupResponse {

    private Long groupId;
    private String groupNm;
    private String groupCd;
    private String description;
    private String isActive;
    private LocalDateTime createdAt;
    private int detailCount;
}
