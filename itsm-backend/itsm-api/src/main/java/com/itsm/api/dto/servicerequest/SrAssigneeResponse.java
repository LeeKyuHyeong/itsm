package com.itsm.api.dto.servicerequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SrAssigneeResponse {

    private Long requestId;
    private Long userId;
    private String userNm;
    private String processStatus;
    private LocalDateTime grantedAt;
}
