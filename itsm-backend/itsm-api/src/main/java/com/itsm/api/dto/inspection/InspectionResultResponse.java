package com.itsm.api.dto.inspection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionResultResponse {

    private Long resultId;
    private Long itemId;
    private String itemNm;
    private String resultValue;
    private String isNormal;
    private String remark;
}
