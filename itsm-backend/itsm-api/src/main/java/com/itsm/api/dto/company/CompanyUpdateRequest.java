package com.itsm.api.dto.company;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUpdateRequest {

    @NotBlank(message = "회사명은 필수입니다.")
    private String companyNm;

    private String bizNo;

    private String ceoNm;

    private String tel;

    private Long defaultPmId;
}
