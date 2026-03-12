package com.itsm.api.service.report;

import com.itsm.api.dto.report.*;
import com.itsm.core.domain.report.Report;
import com.itsm.core.domain.report.ReportForm;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.report.ReportFormRepository;
import com.itsm.core.repository.report.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportFormRepository reportFormRepository;
    @Mock private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private ReportForm reportForm;
    private Report report;

    @BeforeEach
    void setUp() {
        reportForm = ReportForm.builder()
                .formNm("점검 보고서 양식")
                .formTypeCd("INSPECTION")
                .formSchema("{\"fields\": []}")
                .isActive("Y")
                .build();
        ReflectionTestUtils.setField(reportForm, "formId", 1L);

        report = Report.builder()
                .reportForm(reportForm)
                .refType("INSPECTION")
                .refId(1L)
                .reportContent("{\"result\": \"pass\"}")
                .build();
        ReflectionTestUtils.setField(report, "reportId", 1L);
    }

    // ===== ReportForm =====

    @Test
    @DisplayName("보고서 양식 목록을 조회한다")
    void searchForms_returnsList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReportForm> page = new PageImpl<>(List.of(reportForm), pageable, 1);
        given(reportFormRepository.search(null, null, null, pageable)).willReturn(page);

        Page<ReportFormResponse> result = reportService.searchForms(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFormNm()).isEqualTo("점검 보고서 양식");
    }

    @Test
    @DisplayName("보고서 양식 상세를 조회한다")
    void getFormDetail_returnsResponse() {
        given(reportFormRepository.findById(1L)).willReturn(Optional.of(reportForm));

        ReportFormResponse result = reportService.getFormDetail(1L);

        assertThat(result.getFormId()).isEqualTo(1L);
        assertThat(result.getFormNm()).isEqualTo("점검 보고서 양식");
    }

    @Test
    @DisplayName("존재하지 않는 보고서 양식 조회 시 예외가 발생한다")
    void getFormDetail_notFound_throwsException() {
        given(reportFormRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getFormDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("보고서 양식을 생성한다")
    void createForm_success() {
        ReportFormRequest req = new ReportFormRequest(
                "점검 보고서 양식", "INSPECTION", "{\"fields\": []}", "Y");

        given(reportFormRepository.save(any(ReportForm.class))).willAnswer(inv -> {
            ReportForm saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "formId", 2L);
            return saved;
        });

        ReportFormResponse result = reportService.createForm(req, 1L);

        assertThat(result.getFormNm()).isEqualTo("점검 보고서 양식");
        verify(reportFormRepository).save(any(ReportForm.class));
    }

    @Test
    @DisplayName("보고서 양식을 수정한다")
    void updateForm_success() {
        ReportFormRequest req = new ReportFormRequest(
                "수정된 양식", "CHANGE", "{\"fields\": [\"new\"]}", "N");

        given(reportFormRepository.findById(1L)).willReturn(Optional.of(reportForm));

        ReportFormResponse result = reportService.updateForm(1L, req, 1L);

        assertThat(result.getFormNm()).isEqualTo("수정된 양식");
        assertThat(result.getFormTypeCd()).isEqualTo("CHANGE");
    }

    @Test
    @DisplayName("보고서 양식을 삭제한다")
    void deleteForm_success() {
        given(reportFormRepository.findById(1L)).willReturn(Optional.of(reportForm));

        reportService.deleteForm(1L);

        verify(reportFormRepository).delete(reportForm);
    }

    // ===== Report =====

    @Test
    @DisplayName("보고서 목록을 조회한다")
    void searchReports_returnsList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Report> page = new PageImpl<>(List.of(report), pageable, 1);
        given(reportRepository.search("INSPECTION", null, pageable)).willReturn(page);

        Page<ReportResponse> result = reportService.searchReports("INSPECTION", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRefType()).isEqualTo("INSPECTION");
    }

    @Test
    @DisplayName("보고서 상세를 조회한다")
    void getReportDetail_returnsResponse() {
        given(reportRepository.findById(1L)).willReturn(Optional.of(report));

        ReportResponse result = reportService.getReportDetail(1L);

        assertThat(result.getReportId()).isEqualTo(1L);
        assertThat(result.getFormNm()).isEqualTo("점검 보고서 양식");
    }

    @Test
    @DisplayName("존재하지 않는 보고서 조회 시 예외가 발생한다")
    void getReportDetail_notFound_throwsException() {
        given(reportRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReportDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("보고서를 생성한다")
    void createReport_success() {
        ReportCreateRequest req = new ReportCreateRequest(
                1L, "INSPECTION", 1L, "{\"result\": \"pass\"}");

        given(reportFormRepository.findById(1L)).willReturn(Optional.of(reportForm));
        given(reportRepository.save(any(Report.class))).willAnswer(inv -> {
            Report saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "reportId", 2L);
            return saved;
        });

        ReportResponse result = reportService.createReport(req, 1L);

        assertThat(result.getRefType()).isEqualTo("INSPECTION");
        assertThat(result.getFormNm()).isEqualTo("점검 보고서 양식");
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    @DisplayName("보고서를 수정한다")
    void updateReport_success() {
        given(reportRepository.findById(1L)).willReturn(Optional.of(report));

        ReportResponse result = reportService.updateReport(1L, "{\"result\": \"updated\"}", 1L);

        assertThat(result.getReportContent()).isEqualTo("{\"result\": \"updated\"}");
    }

    @Test
    @DisplayName("보고서를 삭제한다")
    void deleteReport_success() {
        given(reportRepository.findById(1L)).willReturn(Optional.of(report));

        reportService.deleteReport(1L);

        verify(reportRepository).delete(report);
    }

    @Test
    @DisplayName("참조유형과 참조ID로 보고서 목록을 조회한다")
    void getReportsByRef_returnsList() {
        given(reportRepository.findByRefTypeAndRefId("INSPECTION", 1L)).willReturn(List.of(report));

        List<ReportResponse> result = reportService.getReportsByRef("INSPECTION", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRefType()).isEqualTo("INSPECTION");
    }
}
