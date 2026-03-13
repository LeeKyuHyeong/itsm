package com.itsm.core.repository.batch;

import com.itsm.core.domain.batch.BatchJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BatchJobRepository extends JpaRepository<BatchJob, Long> {
    Optional<BatchJob> findByJobName(String jobName);
    List<BatchJob> findByIsActive(String isActive);
    List<BatchJob> findAllByOrderByJobNameAsc();
}
