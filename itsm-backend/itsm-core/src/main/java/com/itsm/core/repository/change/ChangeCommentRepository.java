package com.itsm.core.repository.change;

import com.itsm.core.domain.change.ChangeComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeCommentRepository extends JpaRepository<ChangeComment, Long> {
    List<ChangeComment> findByChangeIdOrderByCreatedAtAsc(Long changeId);
}
