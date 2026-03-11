package com.itsm.api.service.common;

import com.itsm.api.dto.common.*;
import com.itsm.core.domain.common.CommonCode;
import com.itsm.core.domain.common.CommonCodeDetail;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.common.CommonCodeDetailRepository;
import com.itsm.core.repository.common.CommonCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommonCodeService {

    private final CommonCodeRepository commonCodeRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;

    @Transactional(readOnly = true)
    public List<CommonCodeGroupResponse> getGroups() {
        List<CommonCode> groups = commonCodeRepository.findAll();
        return groups.stream()
                .map(this::toGroupResponse)
                .toList();
    }

    public CommonCodeGroupResponse createGroup(CommonCodeGroupCreateRequest req, Long currentUserId) {
        if (commonCodeRepository.existsByGroupCd(req.getGroupCd())) {
            throw new BusinessException(ErrorCode.DUPLICATE_VALUE, "이미 존재하는 그룹코드입니다.");
        }

        CommonCode commonCode = CommonCode.builder()
                .groupNm(req.getGroupNm())
                .groupCd(req.getGroupCd())
                .description(req.getDescription())
                .isActive("Y")
                .createdBy(currentUserId)
                .build();

        CommonCode savedGroup = commonCodeRepository.save(commonCode);
        return toGroupResponse(savedGroup);
    }

    public CommonCodeGroupResponse updateGroup(Long groupId, CommonCodeGroupUpdateRequest req, Long currentUserId) {
        CommonCode commonCode = commonCodeRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "공통코드 그룹을 찾을 수 없습니다."));

        commonCode.update(req.getGroupNm(), req.getDescription());
        return toGroupResponse(commonCode);
    }

    public void changeGroupStatus(Long groupId, String isActive) {
        CommonCode commonCode = commonCodeRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "공통코드 그룹을 찾을 수 없습니다."));

        if ("Y".equals(isActive)) {
            commonCode.activate();
        } else {
            commonCode.deactivate();
        }
    }

    @Transactional(readOnly = true)
    public List<CommonCodeDetailResponse> getDetails(Long groupId) {
        commonCodeRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "공통코드 그룹을 찾을 수 없습니다."));

        List<CommonCodeDetail> details = commonCodeDetailRepository.findByCommonCode_GroupIdOrderBySortOrder(groupId);
        return details.stream()
                .map(this::toDetailResponse)
                .toList();
    }

    public CommonCodeDetailResponse createDetail(Long groupId, CommonCodeDetailCreateRequest req, Long currentUserId) {
        CommonCode commonCode = commonCodeRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "공통코드 그룹을 찾을 수 없습니다."));

        if (commonCodeDetailRepository.existsByCommonCode_GroupIdAndCodeVal(groupId, req.getCodeVal())) {
            throw new BusinessException(ErrorCode.DUPLICATE_VALUE, "이미 존재하는 코드값입니다.");
        }

        CommonCodeDetail detail = CommonCodeDetail.builder()
                .commonCode(commonCode)
                .codeVal(req.getCodeVal())
                .codeNm(req.getCodeNm())
                .sortOrder(req.getSortOrder())
                .isActive("Y")
                .createdBy(currentUserId)
                .build();

        CommonCodeDetail savedDetail = commonCodeDetailRepository.save(detail);
        return toDetailResponse(savedDetail);
    }

    public CommonCodeDetailResponse updateDetail(Long groupId, Long detailId, CommonCodeDetailUpdateRequest req, Long currentUserId) {
        CommonCodeDetail detail = commonCodeDetailRepository.findById(detailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "공통코드 상세를 찾을 수 없습니다."));

        detail.update(req.getCodeNm(), req.getSortOrder());
        return toDetailResponse(detail);
    }

    public void changeDetailStatus(Long detailId, String isActive) {
        CommonCodeDetail detail = commonCodeDetailRepository.findById(detailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "공통코드 상세를 찾을 수 없습니다."));

        if ("Y".equals(isActive)) {
            detail.activate();
        } else {
            detail.deactivate();
        }
    }

    @Transactional(readOnly = true)
    public List<CommonCodeDetailResponse> getActiveDetailsByGroupCd(String groupCd) {
        CommonCode commonCode = commonCodeRepository.findByGroupCd(groupCd)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "공통코드 그룹을 찾을 수 없습니다."));

        List<CommonCodeDetail> details = commonCodeDetailRepository
                .findByCommonCode_GroupIdAndIsActiveOrderBySortOrder(commonCode.getGroupId(), "Y");
        return details.stream()
                .map(this::toDetailResponse)
                .toList();
    }

    private CommonCodeGroupResponse toGroupResponse(CommonCode commonCode) {
        return CommonCodeGroupResponse.builder()
                .groupId(commonCode.getGroupId())
                .groupNm(commonCode.getGroupNm())
                .groupCd(commonCode.getGroupCd())
                .description(commonCode.getDescription())
                .isActive(commonCode.getIsActive())
                .createdAt(commonCode.getCreatedAt())
                .detailCount(commonCode.getDetails() != null ? commonCode.getDetails().size() : 0)
                .build();
    }

    private CommonCodeDetailResponse toDetailResponse(CommonCodeDetail detail) {
        return CommonCodeDetailResponse.builder()
                .detailId(detail.getDetailId())
                .codeVal(detail.getCodeVal())
                .codeNm(detail.getCodeNm())
                .sortOrder(detail.getSortOrder())
                .isActive(detail.getIsActive())
                .createdAt(detail.getCreatedAt())
                .build();
    }
}
