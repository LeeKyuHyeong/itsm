package com.itsm.api.controller.company;

import com.itsm.api.dto.company.*;
import com.itsm.api.service.company.CompanyService;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ApiResponse<Page<CompanyResponse>> getCompanies(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ApiResponse.success(companyService.getCompanies(keyword, pageable));
    }

    @PostMapping
    public ApiResponse<CompanyResponse> createCompany(
            @Valid @RequestBody CompanyCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(companyService.createCompany(req, currentUserId));
    }

    @GetMapping("/{companyId}")
    public ApiResponse<CompanyResponse> getCompany(@PathVariable Long companyId) {
        return ApiResponse.success(companyService.getCompany(companyId));
    }

    @PatchMapping("/{companyId}")
    public ApiResponse<CompanyResponse> updateCompany(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(companyService.updateCompany(companyId, req, currentUserId));
    }

    @GetMapping("/{companyId}/departments")
    public ApiResponse<List<DepartmentResponse>> getDepartments(@PathVariable Long companyId) {
        return ApiResponse.success(companyService.getDepartments(companyId));
    }

    @PostMapping("/{companyId}/departments")
    public ApiResponse<DepartmentResponse> createDepartment(
            @PathVariable Long companyId,
            @Valid @RequestBody DepartmentCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(companyService.createDepartment(companyId, req, currentUserId));
    }

    @PatchMapping("/departments/{deptId}")
    public ApiResponse<DepartmentResponse> updateDepartment(
            @PathVariable Long deptId,
            @Valid @RequestBody DepartmentUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(companyService.updateDepartment(deptId, req, currentUserId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
