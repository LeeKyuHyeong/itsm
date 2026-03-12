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
public class IncidentHistoryResponse {

    private Long historyId;
    private String changedField;
    private String beforeValue;
    private String afterValue;
    private Long createdBy;
    private LocalDateTime createdAt;
}
