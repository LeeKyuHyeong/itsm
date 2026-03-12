package com.itsm.core.repository.board;

import com.itsm.core.domain.board.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    List<BoardComment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
