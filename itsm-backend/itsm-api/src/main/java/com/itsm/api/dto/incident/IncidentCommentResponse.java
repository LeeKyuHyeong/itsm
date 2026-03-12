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
public class IncidentCommentResponse {

    private Long commentId;
    private Long incidentId;
    private String content;
    private Long createdBy;
    private String createdByNm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
