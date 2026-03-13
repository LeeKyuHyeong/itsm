package com.itsm.core.repository.statistics;

import com.itsm.core.domain.statistics.DailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatisticsRepository extends JpaRepository<DailyStatistics, Long> {
    List<DailyStatistics> findByStatDate(LocalDate statDate);
    List<DailyStatistics> findByStatDateAndStatType(LocalDate statDate, String statType);
    Optional<DailyStatistics> findByStatDateAndStatTypeAndStatKey(LocalDate statDate, String statType, String statKey);
    List<DailyStatistics> findByStatDateBetweenAndStatType(LocalDate from, LocalDate to, String statType);
    List<DailyStatistics> findByStatDateBetween(LocalDate from, LocalDate to);
}
