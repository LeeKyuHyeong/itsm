package com.itsm.api.service.company;

import com.itsm.api.dto.company.*;
import com.itsm.core.domain.company.*;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.company.*;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyHistoryRepository companyHistoryRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentHistoryRepository departmentHistoryRepository;

    @InjectMocks
    private CompanyService companyService;

    private Company company;
    private Department department;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .companyNm("테스트회사")
                .bizNo("123-45-67890")
                .ceoNm("홍길동")
                .tel("02-1234-5678")
                .status("ACTIVE")
                .build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        department = Department.builder()
                .deptNm("IT부서")
                .company(company)
                .status("ACTIVE")
                .build();
        ReflectionTestUtils.setField(department, "deptId", 1L);
    }

    @Test
    @DisplayName("회사 목록을 조회한다")
    void getCompanies_returnsPageOfCompanies() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Company> companyPage = new PageImpl<>(List.of(company), pageable, 1);
        given(companyRepository.findAll(pageable)).willReturn(companyPage);

        // when
        Page<CompanyResponse> result = companyService.getCompanies(null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCompanyNm()).isEqualTo("테스트회사");
    }

    @Test
    @DisplayName("회사를 성공적으로 생성한다")
    void createCompany_success() {
        // given
        CompanyCreateRequest req = new CompanyCreateRequest(
                "신규회사", "999-88-77777", "김철수", "02-9999-8888", null);
        given(companyRepository.existsByBizNo("999-88-77777")).willReturn(false);
        given(companyRepository.save(any(Company.class))).willAnswer(invocation -> {
            Company saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "companyId", 2L);
            return saved;
        });

        // when
        CompanyResponse result = companyService.createCompany(req, 1L);

        // then
        assertThat(result.getCompanyNm()).isEqualTo("신규회사");
        assertThat(result.getBizNo()).isEqualTo("999-88-77777");
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    @DisplayName("중복 사업자등록번호로 회사 생성 시 예외가 발생한다")
    void createCompany_duplicateBizNo_throwsException() {
        // given
        CompanyCreateRequest req = new CompanyCreateRequest(
                "중복회사", "123-45-67890", "이영희", null, null);
        given(companyRepository.existsByBizNo("123-45-67890")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> companyService.createCompany(req, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_VALUE);
    }

    @Test
    @DisplayName("회사 수정 시 변경 이력이 생성된다")
    void updateCompany_createsHistory() {
        // given
        CompanyUpdateRequest req = new CompanyUpdateRequest(
                "변경된회사명", "123-45-67890", "홍길동", "02-1234-5678", null);
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));

        // when
        CompanyResponse result = companyService.updateCompany(1L, req, 1L);

        // then
        assertThat(result.getCompanyNm()).isEqualTo("변경된회사명");
        verify(companyHistoryRepository).save(any(CompanyHistory.class));
    }

    @Test
    @DisplayName("회사의 부서 목록을 조회한다")
    void getDepartments_returnsDepartmentList() {
        // given
        given(departmentRepository.findByCompany_CompanyId(1L)).willReturn(List.of(department));

        // when
        List<DepartmentResponse> result = companyService.getDepartments(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeptNm()).isEqualTo("IT부서");
        assertThat(result.get(0).getCompanyNm()).isEqualTo("테스트회사");
    }

    @Test
    @DisplayName("부서를 성공적으로 생성한다")
    void createDepartment_success() {
        // given
        DepartmentCreateRequest req = new DepartmentCreateRequest("신규부서");
        given(companyRepository.findById(1L)).willReturn(Optional.of(company));
        given(departmentRepository.save(any(Department.class))).willAnswer(invocation -> {
            Department saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "deptId", 2L);
            return saved;
        });

        // when
        DepartmentResponse result = companyService.createDepartment(1L, req, 1L);

        // then
        assertThat(result.getDeptNm()).isEqualTo("신규부서");
        assertThat(result.getCompanyNm()).isEqualTo("테스트회사");
        verify(departmentRepository).save(any(Department.class));
    }
}
