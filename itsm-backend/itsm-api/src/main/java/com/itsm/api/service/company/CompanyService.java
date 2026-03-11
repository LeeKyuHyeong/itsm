package com.itsm.api.service.company;

import com.itsm.api.dto.company.*;
import com.itsm.core.domain.company.*;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.company.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyHistoryRepository companyHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentHistoryRepository departmentHistoryRepository;

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getCompanies(String keyword, Pageable pageable) {
        Page<Company> companies;
        if (StringUtils.hasText(keyword)) {
            companies = companyRepository.search(keyword, pageable);
        } else {
            companies = companyRepository.findAll(pageable);
        }
        return companies.map(this::toCompanyResponse);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "회사를 찾을 수 없습니다."));
        return toCompanyResponse(company);
    }

    public CompanyResponse createCompany(CompanyCreateRequest req, Long currentUserId) {
        // Check bizNo duplicate if provided
        if (StringUtils.hasText(req.getBizNo()) && companyRepository.existsByBizNo(req.getBizNo())) {
            throw new BusinessException(ErrorCode.DUPLICATE_VALUE, "이미 존재하는 사업자등록번호입니다.");
        }

        Company company = Company.builder()
                .companyNm(req.getCompanyNm())
                .bizNo(req.getBizNo())
                .ceoNm(req.getCeoNm())
                .tel(req.getTel())
                .defaultPmId(req.getDefaultPmId())
                .status("ACTIVE")
                .build();

        company.setCreatedBy(currentUserId);
        Company savedCompany = companyRepository.save(company);
        return toCompanyResponse(savedCompany);
    }

    public CompanyResponse updateCompany(Long companyId, CompanyUpdateRequest req, Long currentUserId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "회사를 찾을 수 없습니다."));

        // Create history for changed fields
        createCompanyHistoryForChanges(company, req, currentUserId);

        company.update(req.getCompanyNm(), req.getBizNo(), req.getCeoNm(), req.getTel(), req.getDefaultPmId());
        company.setUpdatedBy(currentUserId);

        return toCompanyResponse(company);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartments(Long companyId) {
        List<Department> departments = departmentRepository.findByCompany_CompanyId(companyId);
        return departments.stream()
                .map(this::toDepartmentResponse)
                .toList();
    }

    public DepartmentResponse createDepartment(Long companyId, DepartmentCreateRequest req, Long currentUserId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "회사를 찾을 수 없습니다."));

        Department department = Department.builder()
                .deptNm(req.getDeptNm())
                .company(company)
                .status("ACTIVE")
                .build();

        department.setCreatedBy(currentUserId);
        Department savedDepartment = departmentRepository.save(department);
        return toDepartmentResponse(savedDepartment);
    }

    public DepartmentResponse updateDepartment(Long deptId, DepartmentUpdateRequest req, Long currentUserId) {
        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "부서를 찾을 수 없습니다."));

        // Create history
        if (!Objects.equals(department.getDeptNm(), req.getDeptNm())) {
            DepartmentHistory history = DepartmentHistory.builder()
                    .deptId(department.getDeptId())
                    .deptNm(department.getDeptNm())
                    .companyId(department.getCompany().getCompanyId())
                    .changedField("deptNm")
                    .beforeValue(department.getDeptNm())
                    .afterValue(req.getDeptNm())
                    .createdBy(currentUserId)
                    .build();
            departmentHistoryRepository.save(history);
        }

        department.update(req.getDeptNm());
        department.setUpdatedBy(currentUserId);

        return toDepartmentResponse(department);
    }

    public void changeDepartmentStatus(Long deptId, String status, Long currentUserId) {
        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "부서를 찾을 수 없습니다."));

        DepartmentHistory history = DepartmentHistory.builder()
                .deptId(department.getDeptId())
                .deptNm(department.getDeptNm())
                .companyId(department.getCompany().getCompanyId())
                .changedField("status")
                .beforeValue(department.getStatus())
                .afterValue(status)
                .createdBy(currentUserId)
                .build();
        departmentHistoryRepository.save(history);

        department.changeStatus(status);
        department.setUpdatedBy(currentUserId);
    }

    private void createCompanyHistoryForChanges(Company company, CompanyUpdateRequest req, Long currentUserId) {
        List<String[]> changes = new ArrayList<>();

        if (!Objects.equals(company.getCompanyNm(), req.getCompanyNm())) {
            changes.add(new String[]{"companyNm", company.getCompanyNm(), req.getCompanyNm()});
        }
        if (!Objects.equals(company.getBizNo(), req.getBizNo())) {
            changes.add(new String[]{"bizNo", company.getBizNo(), req.getBizNo()});
        }
        if (!Objects.equals(company.getCeoNm(), req.getCeoNm())) {
            changes.add(new String[]{"ceoNm", company.getCeoNm(), req.getCeoNm()});
        }
        if (!Objects.equals(company.getTel(), req.getTel())) {
            changes.add(new String[]{"tel", company.getTel(), req.getTel()});
        }
        if (!Objects.equals(company.getDefaultPmId(), req.getDefaultPmId())) {
            changes.add(new String[]{"defaultPmId", String.valueOf(company.getDefaultPmId()), String.valueOf(req.getDefaultPmId())});
        }

        for (String[] change : changes) {
            CompanyHistory history = CompanyHistory.builder()
                    .companyId(company.getCompanyId())
                    .companyNm(company.getCompanyNm())
                    .bizNo(company.getBizNo())
                    .ceoNm(company.getCeoNm())
                    .changedField(change[0])
                    .beforeValue(change[1])
                    .afterValue(change[2])
                    .createdBy(currentUserId)
                    .build();
            companyHistoryRepository.save(history);
        }
    }

    private CompanyResponse toCompanyResponse(Company company) {
        return CompanyResponse.builder()
                .companyId(company.getCompanyId())
                .companyNm(company.getCompanyNm())
                .bizNo(company.getBizNo())
                .ceoNm(company.getCeoNm())
                .tel(company.getTel())
                .defaultPmId(company.getDefaultPmId())
                .status(company.getStatus())
                .createdAt(company.getCreatedAt())
                .build();
    }

    private DepartmentResponse toDepartmentResponse(Department department) {
        return DepartmentResponse.builder()
                .deptId(department.getDeptId())
                .deptNm(department.getDeptNm())
                .companyId(department.getCompany().getCompanyId())
                .companyNm(department.getCompany().getCompanyNm())
                .status(department.getStatus())
                .build();
    }
}
