package com.itsm.api.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    private Long companyId;
    private String companyNm;
    private String bizNo;
    private String ceoNm;
    private String tel;
    private Long defaultPmId;
    private String status;
    private LocalDateTime createdAt;
}
