package com.itsm.core.repository.board;

import com.itsm.core.domain.board.BoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
    Page<BoardPost> findByBoardConfig_BoardIdOrderByIsNoticeDescCreatedAtDesc(Long boardId, Pageable pageable);
}
