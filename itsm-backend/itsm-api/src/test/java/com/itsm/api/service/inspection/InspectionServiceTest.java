package com.itsm.api.service.inspection;

import com.itsm.api.dto.inspection.*;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.inspection.*;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.inspection.*;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock private InspectionRepository inspectionRepository;
    @Mock private InspectionItemRepository inspectionItemRepository;
    @Mock private InspectionResultRepository inspectionResultRepository;
    @Mock private InspectionHistoryRepository inspectionHistoryRepository;
    @Mock private CompanyRepository companyRepository;

    @InjectMocks
    private InspectionService inspectionService;

    private Company company;
    private Inspection inspection;

    @BeforeEach
    void setUp() {
        company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        inspection = Inspection.builder()
                .title("서버 정기점검")
                .inspectionTypeCd("MONTHLY")
                .scheduledAt(LocalDate.of(2026, 3, 20))
                .company(company)
                .description("월별 정기점검")
                .build();
        ReflectionTestUtils.setField(inspection, "inspectionId", 1L);
    }

    @Test
    @DisplayName("점검 목록을 검색 조건으로 조회한다")
    void search_returnsList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Inspection> page = new PageImpl<>(List.of(inspection), pageable, 1);
        given(inspectionRepository.search(null, null, null, null, pageable)).willReturn(page);
        given(inspectionItemRepository.countByInspectionId(1L)).willReturn(3L);
        given(inspectionResultRepository.countByInspectionId(1L)).willReturn(1L);

        Page<InspectionResponse> result = inspectionService.search(null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("서버 정기점검");
        assertThat(result.getContent().get(0).getItemCount()).isEqualTo(3);
        assertThat(result.getContent().get(0).getCompletedItemCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("점검 상세를 조회한다")
    void getDetail_returnsResponse() {
        given(inspectionRepository.findById(1L)).willReturn(Optional.of(inspection));
        given(inspectionItemRepository.countByInspectionId(1L)).willReturn(5L);
        given(inspectionResultRepository.countByInspectionId(1L)).willReturn(2L);

        InspectionResponse result = inspectionService.getDetail(1L);

        assertThat(result.getInspectionId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("서버 정기점검");
        assertThat(result.getItemCount()).isEqualTo(5);
        assertThat(result.getCompletedItemCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("존재하지 않는 점검 조회 시 예외가 발생한다")
    void getDetail_notFound_throwsException() {
        given(inspectionRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> inspectionService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("점검을 성공적으로 생성한다")
    void create_success() {
        InspectionCreateRequest req = new InspectionCreateRequest(
                "서버 정기점검", "MONTHLY", LocalDate.of(2026, 3, 20), 1L, null, "월별 정기점검");

        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(inspectionRepository.save(any(Inspection.class))).willAnswer(inv -> {
            Inspection saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "inspectionId", 2L);
            return saved;
        });

        InspectionResponse result = inspectionService.create(req, 1L);

        assertThat(result.getTitle()).isEqualTo("서버 정기점검");
        assertThat(result.getStatusCd()).isEqualTo("SCHEDULED");
        verify(inspectionRepository).save(any(Inspection.class));
    }

    @Test
    @DisplayName("점검을 수정하면 변경 이력이 저장된다")
    void update_savesHistory() {
        InspectionUpdateRequest req = new InspectionUpdateRequest(
                "네트워크 점검", "WEEKLY", LocalDate.of(2026, 4, 1), null, "주별 점검");

        given(inspectionRepository.findById(1L)).willReturn(Optional.of(inspection));
        given(inspectionItemRepository.countByInspectionId(1L)).willReturn(0L);
        given(inspectionResultRepository.countByInspectionId(1L)).willReturn(0L);

        InspectionResponse result = inspectionService.update(1L, req, 1L);

        assertThat(result.getTitle()).isEqualTo("네트워크 점검");
        verify(inspectionHistoryRepository).saveAll(any());
    }

    @Test
    @DisplayName("점검 상태를 변경한다")
    void changeStatus_success() {
        given(inspectionRepository.findById(1L)).willReturn(Optional.of(inspection));

        inspectionService.changeStatus(1L, "IN_PROGRESS", 1L);

        assertThat(inspection.getStatusCd()).isEqualTo("IN_PROGRESS");
        verify(inspectionHistoryRepository).save(any(InspectionHistory.class));
    }

    @Test
    @DisplayName("유효하지 않은 상태 전이 시 예외가 발생한다")
    void changeStatus_invalidTransition_throwsException() {
        given(inspectionRepository.findById(1L)).willReturn(Optional.of(inspection));

        assertThatThrownBy(() -> inspectionService.changeStatus(1L, "CLOSED", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);
    }

    @Test
    @DisplayName("점검항목을 추가한다")
    void addItem_success() {
        InspectionItemRequest req = new InspectionItemRequest("CPU 사용률 점검", 1, "Y");
        given(inspectionRepository.findById(1L)).willReturn(Optional.of(inspection));
        given(inspectionItemRepository.save(any(InspectionItem.class))).willAnswer(inv -> {
            InspectionItem saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "itemId", 1L);
            return saved;
        });

        InspectionItemResponse result = inspectionService.addItem(1L, req, 1L);

        assertThat(result.getItemNm()).isEqualTo("CPU 사용률 점검");
        assertThat(result.getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("점검항목 목록을 조회한다")
    void getItems_returnsList() {
        InspectionItem item = InspectionItem.builder()
                .inspectionId(1L).itemNm("CPU 점검").sortOrder(1).isRequired("Y").build();
        ReflectionTestUtils.setField(item, "itemId", 1L);
        given(inspectionItemRepository.findByInspectionIdOrderBySortOrderAsc(1L)).willReturn(List.of(item));

        List<InspectionItemResponse> result = inspectionService.getItems(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemNm()).isEqualTo("CPU 점검");
    }

    @Test
    @DisplayName("점검항목을 삭제한다")
    void deleteItem_success() {
        InspectionItem item = InspectionItem.builder()
                .inspectionId(1L).itemNm("CPU 점검").sortOrder(1).isRequired("Y").build();
        ReflectionTestUtils.setField(item, "itemId", 1L);
        given(inspectionItemRepository.findById(1L)).willReturn(Optional.of(item));

        inspectionService.deleteItem(1L, 1L);

        verify(inspectionItemRepository).delete(item);
    }

    @Test
    @DisplayName("점검결과를 입력한다")
    void addResult_success() {
        InspectionResultRequest req = new InspectionResultRequest(1L, "정상", "Y", "이상 없음");
        InspectionItem item = InspectionItem.builder()
                .inspectionId(1L).itemNm("CPU 점검").sortOrder(1).isRequired("Y").build();
        ReflectionTestUtils.setField(item, "itemId", 1L);

        given(inspectionRepository.findById(1L)).willReturn(Optional.of(inspection));
        given(inspectionItemRepository.findById(1L)).willReturn(Optional.of(item));
        given(inspectionResultRepository.findByInspectionIdAndItemId(1L, 1L)).willReturn(Optional.empty());
        given(inspectionResultRepository.save(any(InspectionResult.class))).willAnswer(inv -> {
            InspectionResult saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "resultId", 1L);
            return saved;
        });

        InspectionResultResponse result = inspectionService.addResult(1L, req, 1L);

        assertThat(result.getResultValue()).isEqualTo("정상");
        assertThat(result.getIsNormal()).isEqualTo("Y");
        assertThat(result.getItemNm()).isEqualTo("CPU 점검");
    }

    @Test
    @DisplayName("이미 존재하는 점검결과를 수정한다")
    void addResult_existingResult_updates() {
        InspectionResultRequest req = new InspectionResultRequest(1L, "비정상", "N", "CPU 과부하");
        InspectionItem item = InspectionItem.builder()
                .inspectionId(1L).itemNm("CPU 점검").sortOrder(1).isRequired("Y").build();
        ReflectionTestUtils.setField(item, "itemId", 1L);

        InspectionResult existingResult = InspectionResult.builder()
                .inspectionId(1L).itemId(1L).resultValue("정상").isNormal("Y").build();
        ReflectionTestUtils.setField(existingResult, "resultId", 1L);

        given(inspectionRepository.findById(1L)).willReturn(Optional.of(inspection));
        given(inspectionItemRepository.findById(1L)).willReturn(Optional.of(item));
        given(inspectionResultRepository.findByInspectionIdAndItemId(1L, 1L)).willReturn(Optional.of(existingResult));

        InspectionResultResponse result = inspectionService.addResult(1L, req, 1L);

        assertThat(result.getResultValue()).isEqualTo("비정상");
        assertThat(result.getIsNormal()).isEqualTo("N");
    }

    @Test
    @DisplayName("점검결과 목록을 조회한다")
    void getResults_returnsList() {
        InspectionResult resultEntity = InspectionResult.builder()
                .inspectionId(1L).itemId(1L).resultValue("정상").isNormal("Y").build();
        ReflectionTestUtils.setField(resultEntity, "resultId", 1L);

        InspectionItem item = InspectionItem.builder()
                .inspectionId(1L).itemNm("CPU 점검").sortOrder(1).isRequired("Y").build();
        ReflectionTestUtils.setField(item, "itemId", 1L);

        given(inspectionResultRepository.findByInspectionId(1L)).willReturn(List.of(resultEntity));
        given(inspectionItemRepository.findById(1L)).willReturn(Optional.of(item));

        List<InspectionResultResponse> result = inspectionService.getResults(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemNm()).isEqualTo("CPU 점검");
    }

    @Test
    @DisplayName("변경 이력을 조회한다")
    void getHistory_returnsList() {
        InspectionHistory history = InspectionHistory.builder()
                .inspectionId(1L).changedField("statusCd")
                .beforeValue("SCHEDULED").afterValue("IN_PROGRESS")
                .createdBy(1L).build();
        ReflectionTestUtils.setField(history, "historyId", 1L);
        given(inspectionHistoryRepository.findByInspectionIdOrderByCreatedAtDesc(1L)).willReturn(List.of(history));

        List<InspectionHistoryResponse> result = inspectionService.getHistory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChangedField()).isEqualTo("statusCd");
    }
}
