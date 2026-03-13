package com.itsm.core.domain.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MenuAccessLog (log) 엔티티 테스트")
class MenuAccessLogTest {

    @Test
    @DisplayName("메뉴 접근 로그 생성")
    void createMenuAccessLog() {
        LocalDateTime accessedAt = LocalDateTime.of(2026, 3, 13, 10, 30, 0);

        MenuAccessLog log = MenuAccessLog.builder()
                .userId(1L)
                .menuPath("/incident/list")
                .menuNm("장애관리")
                .accessedAt(accessedAt)
                .build();

        assertThat(log.getUserId()).isEqualTo(1L);
        assertThat(log.getMenuPath()).isEqualTo("/incident/list");
        assertThat(log.getMenuNm()).isEqualTo("장애관리");
        assertThat(log.getAccessedAt()).isEqualTo(accessedAt);
    }
}
