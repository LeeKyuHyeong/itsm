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
public class BoardFileResponse {

    private Long fileId;
    private Long postId;
    private String originalNm;
    private String savedNm;
    private String filePath;
    private Long fileSize;
    private String extension;
    private LocalDateTime createdAt;
}
