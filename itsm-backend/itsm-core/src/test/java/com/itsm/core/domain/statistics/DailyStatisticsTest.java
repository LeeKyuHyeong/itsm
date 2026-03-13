package com.itsm.core.domain.statistics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DailyStatistics 엔티티 테스트")
class DailyStatisticsTest {

    @Test
    @DisplayName("일별 통계 생성")
    void createDailyStatistics() {
        LocalDate statDate = LocalDate.of(2026, 3, 13);

        DailyStatistics stat = DailyStatistics.builder()
                .statDate(statDate)
                .statType("INCIDENT")
                .statKey("total_count")
                .statValue(new BigDecimal("15.00"))
                .statDetail("{\"open\": 5, \"closed\": 10}")
                .build();

        assertThat(stat.getStatDate()).isEqualTo(statDate);
        assertThat(stat.getStatType()).isEqualTo("INCIDENT");
        assertThat(stat.getStatKey()).isEqualTo("total_count");
        assertThat(stat.getStatValue()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(stat.getStatDetail()).isEqualTo("{\"open\": 5, \"closed\": 10}");
    }

    @Test
    @DisplayName("통계 값 업데이트")
    void updateValue() {
        DailyStatistics stat = DailyStatistics.builder()
                .statDate(LocalDate.of(2026, 3, 13))
                .statType("INCIDENT")
                .statKey("total_count")
                .statValue(new BigDecimal("10.00"))
                .build();

        stat.updateValue(new BigDecimal("20.00"), "{\"open\": 8, \"closed\": 12}");

        assertThat(stat.getStatValue()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(stat.getStatDetail()).isEqualTo("{\"open\": 8, \"closed\": 12}");
    }
}
