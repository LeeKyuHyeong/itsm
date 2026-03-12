package com.itsm.core.domain.board;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_board_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardConfig boardConfig;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_cnt", nullable = false)
    private int viewCnt;

    @Column(name = "is_notice", nullable = false, columnDefinition = "char(1)")
    private String isNotice;

    @Builder
    public BoardPost(BoardConfig boardConfig, String title, String content, String isNotice) {
        this.boardConfig = boardConfig;
        this.title = title;
        this.content = content;
        this.viewCnt = 0;
        this.isNotice = isNotice != null ? isNotice : "N";
    }

    public void incrementViewCnt() {
        this.viewCnt++;
    }

    public void update(String title, String content, String isNotice) {
        this.title = title;
        this.content = content;
        this.isNotice = isNotice;
    }
}
