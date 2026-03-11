package com.itsm.api.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long notiId;
    private String notiTypeCd;
    private String title;
    private String content;
    private String refType;
    private Long refId;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
