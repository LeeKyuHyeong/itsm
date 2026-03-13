package com.itsm.core.domain.statistics;

import com.itsm.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_daily_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long statId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "stat_type", nullable = false, length = 50)
    private String statType;

    @Column(name = "stat_key", nullable = false, length = 100)
    private String statKey;

    @Column(name = "stat_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal statValue;

    @Column(name = "stat_detail", columnDefinition = "TEXT")
    private String statDetail;

    @Builder
    public DailyStatistics(LocalDate statDate, String statType, String statKey, BigDecimal statValue, String statDetail) {
        this.statDate = statDate;
        this.statType = statType;
        this.statKey = statKey;
        this.statValue = statValue;
        this.statDetail = statDetail;
    }

    public void updateValue(BigDecimal statValue, String statDetail) {
        this.statValue = statValue;
        this.statDetail = statDetail;
    }
}
