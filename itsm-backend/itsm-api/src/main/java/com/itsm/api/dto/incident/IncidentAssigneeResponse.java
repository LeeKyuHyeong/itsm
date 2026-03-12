package com.itsm.api.dto.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentAssigneeResponse {

    private Long incidentId;
    private Long userId;
    private String userNm;
    private LocalDateTime grantedAt;
}
