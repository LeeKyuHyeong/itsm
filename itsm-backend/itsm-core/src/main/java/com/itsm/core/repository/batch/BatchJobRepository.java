package com.itsm.core.repository.batch;

import com.itsm.core.domain.batch.BatchJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BatchJobRepository extends JpaRepository<BatchJob, Long> {
    Optional<BatchJob> findByJobName(String jobName);
    List<BatchJob> findByIsActive(String isActive);
    List<BatchJob> findAllByOrderByJobNameAsc();
    List<BatchJob> findByTriggerNow(String triggerNow);

    @Modifying
    @Transactional
    @Query("UPDATE BatchJob b SET b.triggerNow = 'N' WHERE b.batchJobId = :batchJobId")
    void clearTrigger(@Param("batchJobId") Long batchJobId);

    @Modifying
    @Transactional
    @Query("UPDATE BatchJob b SET b.lastExecutedAt = :executedAt, b.lastResult = :result, " +
            "b.lastResultMessage = :message, b.updatedAt = :executedAt WHERE b.jobName = :jobName")
    void updateExecutionResult(@Param("jobName") String jobName,
                               @Param("executedAt") LocalDateTime executedAt,
                               @Param("result") String result,
                               @Param("message") String message);
}
