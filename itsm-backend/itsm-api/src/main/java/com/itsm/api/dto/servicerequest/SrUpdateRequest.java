package com.itsm.api.dto.servicerequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SrUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "요청 내용은 필수입니다.")
    private String content;

    @NotBlank(message = "요청유형코드는 필수입니다.")
    private String requestTypeCd;

    @NotBlank(message = "우선순위코드는 필수입니다.")
    private String priorityCd;

    @NotNull(message = "요청일시는 필수입니다.")
    private LocalDateTime occurredAt;
}
