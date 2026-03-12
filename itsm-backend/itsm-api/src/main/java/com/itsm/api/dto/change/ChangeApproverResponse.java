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
public class ChangeApproverResponse {

    private Long changeId;
    private Long userId;
    private String userNm;
    private int approveOrder;
    private String approveStatus;
    private LocalDateTime approvedAt;
    private String comment;
}
