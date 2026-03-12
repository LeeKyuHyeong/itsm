package com.itsm.core.repository.board;

import com.itsm.core.domain.board.BoardFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardFileRepository extends JpaRepository<BoardFile, Long> {
    List<BoardFile> findByPostIdOrderByCreatedAtAsc(Long postId);
}
