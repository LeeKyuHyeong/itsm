package com.itsm.core.repository.board;

import com.itsm.core.domain.board.BoardConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardConfigRepository extends JpaRepository<BoardConfig, Long> {
    List<BoardConfig> findAllByOrderBySortOrderAsc();
}
