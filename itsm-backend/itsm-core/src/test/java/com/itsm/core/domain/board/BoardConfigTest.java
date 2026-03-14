package com.itsm.core.domain.board;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class BoardConfigTest {

    @Test
    @DisplayName("게시판 설정을 생성할 수 있다")
    void create_boardConfig() {
        BoardConfig config = BoardConfig.builder()
                .boardNm("공지사항")
                .boardTypeCd("NOTICE")
                .allowExt("pdf,doc,hwp")
                .maxFileSize(10)
                .allowComment("Y")
                .rolePermission("{\"read\":[\"USER\"],\"write\":[\"ADMIN\"]}")
                .isActive("Y")
                .sortOrder(1)
                .build();

        assertThat(config.getBoardNm()).isEqualTo("공지사항");
        assertThat(config.getBoardTypeCd()).isEqualTo("NOTICE");
        assertThat(config.getAllowExt()).isEqualTo("pdf,doc,hwp");
        assertThat(config.getMaxFileSize()).isEqualTo(10);
        assertThat(config.getAllowComment()).isEqualTo("Y");
        assertThat(config.getIsActive()).isEqualTo("Y");
        assertThat(config.getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시판 활성/비활성 토글이 가능하다")
    void toggleActive() {
        BoardConfig config = BoardConfig.builder()
                .boardNm("자유게시판")
                .boardTypeCd("FREE")
                .allowComment("Y")
                .rolePermission("{}")
                .isActive("Y")
                .sortOrder(0)
                .build();

        assertThat(config.getIsActive()).isEqualTo("Y");

        config.deactivate();
        assertThat(config.getIsActive()).isEqualTo("N");

        config.activate();
        assertThat(config.getIsActive()).isEqualTo("Y");
    }

    @Test
    @DisplayName("게시판 설정을 수정할 수 있다")
    void update_boardConfig() {
        BoardConfig config = BoardConfig.builder()
                .boardNm("공지사항")
                .boardTypeCd("NOTICE")
                .allowComment("Y")
                .rolePermission("{}")
                .isActive("Y")
                .sortOrder(0)
                .build();

        config.update("수정된 게시판", "Updated Board", "FREE", "jpg,png", 20, "N",
                "{\"read\":[\"USER\"]}", "N", 5);

        assertThat(config.getBoardNm()).isEqualTo("수정된 게시판");
        assertThat(config.getBoardTypeCd()).isEqualTo("FREE");
        assertThat(config.getAllowExt()).isEqualTo("jpg,png");
        assertThat(config.getMaxFileSize()).isEqualTo(20);
        assertThat(config.getAllowComment()).isEqualTo("N");
        assertThat(config.getIsActive()).isEqualTo("N");
        assertThat(config.getSortOrder()).isEqualTo(5);
    }

    @Test
    @DisplayName("게시글을 생성할 수 있다")
    void create_boardPost() {
        BoardConfig config = BoardConfig.builder()
                .boardNm("공지사항")
                .boardTypeCd("NOTICE")
                .allowComment("Y")
                .rolePermission("{}")
                .isActive("Y")
                .sortOrder(0)
                .build();
        ReflectionTestUtils.setField(config, "boardId", 1L);

        BoardPost post = BoardPost.builder()
                .boardConfig(config)
                .title("첫 번째 공지")
                .content("공지 내용입니다.")
                .isNotice("Y")
                .build();

        assertThat(post.getTitle()).isEqualTo("첫 번째 공지");
        assertThat(post.getContent()).isEqualTo("공지 내용입니다.");
        assertThat(post.getViewCnt()).isEqualTo(0);
        assertThat(post.getIsNotice()).isEqualTo("Y");
        assertThat(post.getBoardConfig().getBoardId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("게시글 조회수를 증가시킬 수 있다")
    void incrementViewCnt() {
        BoardConfig config = BoardConfig.builder()
                .boardNm("자유게시판")
                .boardTypeCd("FREE")
                .allowComment("Y")
                .rolePermission("{}")
                .isActive("Y")
                .sortOrder(0)
                .build();

        BoardPost post = BoardPost.builder()
                .boardConfig(config)
                .title("테스트 글")
                .content("내용")
                .isNotice("N")
                .build();

        assertThat(post.getViewCnt()).isEqualTo(0);
        post.incrementViewCnt();
        assertThat(post.getViewCnt()).isEqualTo(1);
        post.incrementViewCnt();
        assertThat(post.getViewCnt()).isEqualTo(2);
    }

    @Test
    @DisplayName("게시글을 수정할 수 있다")
    void update_boardPost() {
        BoardConfig config = BoardConfig.builder()
                .boardNm("자유게시판")
                .boardTypeCd("FREE")
                .allowComment("Y")
                .rolePermission("{}")
                .isActive("Y")
                .sortOrder(0)
                .build();

        BoardPost post = BoardPost.builder()
                .boardConfig(config)
                .title("원래 제목")
                .content("원래 내용")
                .isNotice("N")
                .build();

        post.update("수정 제목", "수정 내용", "Y");

        assertThat(post.getTitle()).isEqualTo("수정 제목");
        assertThat(post.getContent()).isEqualTo("수정 내용");
        assertThat(post.getIsNotice()).isEqualTo("Y");
    }
}
