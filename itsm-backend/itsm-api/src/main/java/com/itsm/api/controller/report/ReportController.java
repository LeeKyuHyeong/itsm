package com.itsm.api.controller.report;

import com.itsm.api.dto.report.*;
import com.itsm.api.service.report.ReportService;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ===== ReportForm =====

    @GetMapping("/api/v1/report-forms")
    public ApiResponse<Page<ReportFormResponse>> searchForms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String formTypeCd,
            @RequestParam(required = false) String isActive,
            Pageable pageable) {
        return ApiResponse.success(reportService.searchForms(keyword, formTypeCd, isActive, pageable));
    }

    @GetMapping("/api/v1/report-forms/{formId}")
    public ApiResponse<ReportFormResponse> getFormDetail(@PathVariable Long formId) {
        return ApiResponse.success(reportService.getFormDetail(formId));
    }

    @PostMapping("/api/v1/report-forms")
    public ApiResponse<ReportFormResponse> createForm(
            @Valid @RequestBody ReportFormRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(reportService.createForm(req, currentUserId));
    }

    @PatchMapping("/api/v1/report-forms/{formId}")
    public ApiResponse<ReportFormResponse> updateForm(
            @PathVariable Long formId,
            @Valid @RequestBody ReportFormRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(reportService.updateForm(formId, req, currentUserId));
    }

    @DeleteMapping("/api/v1/report-forms/{formId}")
    public ApiResponse<Void> deleteForm(@PathVariable Long formId) {
        reportService.deleteForm(formId);
        return ApiResponse.success();
    }

    // ===== Report =====

    @GetMapping("/api/v1/reports")
    public ApiResponse<Page<ReportResponse>> searchReports(
            @RequestParam(required = false) String refType,
            @RequestParam(required = false) Long refId,
            Pageable pageable) {
        return ApiResponse.success(reportService.searchReports(refType, refId, pageable));
    }

    @PostMapping("/api/v1/reports")
    public ApiResponse<ReportResponse> createReport(
            @Valid @RequestBody ReportCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(reportService.createReport(req, currentUserId));
    }

    @GetMapping("/api/v1/reports/{reportId}")
    public ApiResponse<ReportResponse> getReportDetail(@PathVariable Long reportId) {
        return ApiResponse.success(reportService.getReportDetail(reportId));
    }

    @PatchMapping("/api/v1/reports/{reportId}")
    public ApiResponse<ReportResponse> updateReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(reportService.updateReport(reportId, body.get("reportContent"), currentUserId));
    }

    @DeleteMapping("/api/v1/reports/{reportId}")
    public ApiResponse<Void> deleteReport(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ApiResponse.success();
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
