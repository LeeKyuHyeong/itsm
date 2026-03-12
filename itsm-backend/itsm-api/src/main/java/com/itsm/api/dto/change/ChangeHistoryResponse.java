package com.itsm.api.dto.change;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeHistoryResponse {

    private Long historyId;
    private String changedField;
    private String beforeValue;
    private String afterValue;
    private Long createdBy;
    private LocalDateTime createdAt;
}
