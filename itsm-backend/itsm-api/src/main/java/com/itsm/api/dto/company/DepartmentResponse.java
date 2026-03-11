package com.itsm.api.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private Long deptId;
    private String deptNm;
    private Long companyId;
    private String companyNm;
    private String status;
}
