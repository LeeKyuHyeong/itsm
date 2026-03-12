package com.itsm.core.domain.incident;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentCommentTest {

    @Test
    @DisplayName("Builder로 IncidentComment를 생성한다")
    void builder_createsComment() {
        IncidentComment comment = IncidentComment.builder()
                .incidentId(1L)
                .content("확인 중입니다.")
                .createdBy(10L)
                .build();

        assertThat(comment.getIncidentId()).isEqualTo(1L);
        assertThat(comment.getContent()).isEqualTo("확인 중입니다.");
        assertThat(comment.getCreatedBy()).isEqualTo(10L);
    }

    @Test
    @DisplayName("댓글 내용을 수정한다")
    void updateContent_changesContent() {
        IncidentComment comment = IncidentComment.builder()
                .incidentId(1L)
                .content("확인 중입니다.")
                .createdBy(10L)
                .build();

        comment.updateContent("처리 완료되었습니다.", 10L);

        assertThat(comment.getContent()).isEqualTo("처리 완료되었습니다.");
        assertThat(comment.getUpdatedBy()).isEqualTo(10L);
    }
}
