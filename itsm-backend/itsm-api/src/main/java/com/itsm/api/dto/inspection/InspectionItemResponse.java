package com.itsm.api.dto.inspection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionItemResponse {

    private Long itemId;
    private String itemNm;
    private Integer sortOrder;
    private String isRequired;
}
