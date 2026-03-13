package com.itsm.api.controller.admin;

import com.itsm.api.dto.batch.BatchJobResponse;
import com.itsm.api.dto.batch.BatchJobUpdateRequest;
import com.itsm.api.service.batch.BatchJobService;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/batch-jobs")
@RequiredArgsConstructor
public class BatchJobController {

    private final BatchJobService batchJobService;

    @GetMapping
    public ApiResponse<List<BatchJobResponse>> getAllJobs() {
        return ApiResponse.success(batchJobService.getAllJobs());
    }

    @GetMapping("/{batchJobId}")
    public ApiResponse<BatchJobResponse> getJob(@PathVariable Long batchJobId) {
        return ApiResponse.success(batchJobService.getJob(batchJobId));
    }

    @PatchMapping("/{batchJobId}")
    public ApiResponse<BatchJobResponse> updateJob(
            @PathVariable Long batchJobId,
            @Valid @RequestBody BatchJobUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        return ApiResponse.success(batchJobService.updateJob(batchJobId, req, currentUserId));
    }
}
