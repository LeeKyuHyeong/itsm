package com.itsm.api.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardPostResponse {

    private Long postId;
    private Long boardId;
    private String boardNm;
    private String title;
    private String content;
    private int viewCnt;
    private String isNotice;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
