package com.itsm.api.service.report;

import com.itsm.api.dto.report.*;
import com.itsm.core.domain.report.Report;
import com.itsm.core.domain.report.ReportForm;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.report.ReportFormRepository;
import com.itsm.core.repository.report.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportFormRepository reportFormRepository;
    private final ReportRepository reportRepository;

    // ===== ReportForm =====

    @Transactional(readOnly = true)
    public Page<ReportFormResponse> searchForms(String keyword, String formTypeCd, String isActive, Pageable pageable) {
        return reportFormRepository.search(keyword, formTypeCd, isActive, pageable)
                .map(this::toFormResponse);
    }

    @Transactional(readOnly = true)
    public ReportFormResponse getFormDetail(Long formId) {
        ReportForm form = findFormById(formId);
        return toFormResponse(form);
    }

    public ReportFormResponse createForm(ReportFormRequest req, Long currentUserId) {
        ReportForm form = ReportForm.builder()
                .formNm(req.getFormNm())
                .formTypeCd(req.getFormTypeCd())
                .formSchema(req.getFormSchema())
                .isActive(req.getIsActive() != null ? req.getIsActive() : "Y")
                .build();
        form.setCreatedBy(currentUserId);

        ReportForm saved = reportFormRepository.save(form);
        return toFormResponse(saved);
    }

    public ReportFormResponse updateForm(Long formId, ReportFormRequest req, Long currentUserId) {
        ReportForm form = findFormById(formId);
        form.update(req.getFormNm(), req.getFormTypeCd(), req.getFormSchema(),
                req.getIsActive() != null ? req.getIsActive() : form.getIsActive());
        form.setUpdatedBy(currentUserId);
        return toFormResponse(form);
    }

    public void deleteForm(Long formId) {
        ReportForm form = findFormById(formId);
        reportFormRepository.delete(form);
    }

    // ===== Report =====

    @Transactional(readOnly = true)
    public Page<ReportResponse> searchReports(String refType, Long refId, Pageable pageable) {
        return reportRepository.search(refType, refId, pageable)
                .map(this::toReportResponse);
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportDetail(Long reportId) {
        Report report = findReportById(reportId);
        return toReportResponse(report);
    }

    public ReportResponse createReport(ReportCreateRequest req, Long currentUserId) {
        ReportForm form = findFormById(req.getFormId());

        Report report = Report.builder()
                .reportForm(form)
                .refType(req.getRefType())
                .refId(req.getRefId())
                .reportContent(req.getReportContent())
                .build();
        report.setCreatedBy(currentUserId);

        Report saved = reportRepository.save(report);
        return toReportResponse(saved);
    }

    public ReportResponse updateReport(Long reportId, String reportContent, Long currentUserId) {
        Report report = findReportById(reportId);
        report.update(reportContent);
        report.setUpdatedBy(currentUserId);
        return toReportResponse(report);
    }

    public void deleteReport(Long reportId) {
        Report report = findReportById(reportId);
        reportRepository.delete(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByRef(String refType, Long refId) {
        return reportRepository.findByRefTypeAndRefId(refType, refId).stream()
                .map(this::toReportResponse)
                .toList();
    }

    private ReportForm findFormById(Long formId) {
        return reportFormRepository.findById(formId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "보고서 양식을 찾을 수 없습니다."));
    }

    private Report findReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "보고서를 찾을 수 없습니다."));
    }

    private ReportFormResponse toFormResponse(ReportForm form) {
        return ReportFormResponse.builder()
                .formId(form.getFormId())
                .formNm(form.getFormNm())
                .formTypeCd(form.getFormTypeCd())
                .formSchema(form.getFormSchema())
                .isActive(form.getIsActive())
                .createdAt(form.getCreatedAt())
                .updatedAt(form.getUpdatedAt())
                .build();
    }

    private ReportResponse toReportResponse(Report report) {
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .formId(report.getReportForm().getFormId())
                .formNm(report.getReportForm().getFormNm())
                .refType(report.getRefType())
                .refId(report.getRefId())
                .reportContent(report.getReportContent())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
