package com.itsm.api.service.batch;

import com.itsm.api.dto.batch.BatchJobResponse;
import com.itsm.api.dto.batch.BatchJobUpdateRequest;
import com.itsm.core.domain.batch.BatchJob;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.batch.BatchJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;

    @Transactional(readOnly = true)
    public List<BatchJobResponse> getAllJobs() {
        return batchJobRepository.findAllByOrderByJobNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BatchJobResponse getJob(Long batchJobId) {
        return toResponse(findById(batchJobId));
    }

    public BatchJobResponse updateJob(Long batchJobId, BatchJobUpdateRequest req, Long currentUserId) {
        BatchJob job = findById(batchJobId);
        job.update(req.getCronExpression(), req.getIsActive(), req.getJobDescription());
        job.setUpdatedBy(currentUserId);
        return toResponse(job);
    }

    public void executeNow(Long batchJobId) {
        findById(batchJobId); // verify exists
        // Actual execution happens in batch module - this just validates
    }

    private BatchJob findById(Long batchJobId) {
        return batchJobRepository.findById(batchJobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "배치 작업을 찾을 수 없습니다."));
    }

    private BatchJobResponse toResponse(BatchJob job) {
        return BatchJobResponse.builder()
                .batchJobId(job.getBatchJobId())
                .jobName(job.getJobName())
                .jobDescription(job.getJobDescription())
                .cronExpression(job.getCronExpression())
                .isActive(job.getIsActive())
                .lastExecutedAt(job.getLastExecutedAt())
                .lastResult(job.getLastResult())
                .lastResultMessage(job.getLastResultMessage())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
