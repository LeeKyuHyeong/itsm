package com.itsm.core.repository.user;

import com.itsm.core.domain.user.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {

    List<UserHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
