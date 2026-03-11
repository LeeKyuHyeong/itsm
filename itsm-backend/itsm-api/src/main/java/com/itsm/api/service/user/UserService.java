package com.itsm.api.service.user;

import com.itsm.api.dto.user.*;
import com.itsm.core.domain.company.Department;
import com.itsm.core.domain.user.*;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.company.DepartmentRepository;
import com.itsm.core.repository.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserHistoryRepository userHistoryRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
    );

    @Transactional(readOnly = true)
    public Page<UserListResponse> getUsers(String keyword, Pageable pageable) {
        Page<User> users;
        if (StringUtils.hasText(keyword)) {
            users = userRepository.search(keyword, pageable);
        } else {
            users = userRepository.findAllActive(pageable);
        }

        return users.map(this::toUserListResponse);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(userId);
        return toUserDetailResponse(user, userRoles);
    }

    public UserDetailResponse createUser(UserCreateRequest req, Long currentUserId) {
        // Check loginId duplicate
        if (userRepository.existsByLoginId(req.getLoginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_VALUE, "이미 존재하는 로그인 ID입니다.");
        }

        // Validate password policy
        if (!PASSWORD_PATTERN.matcher(req.getPassword()).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "비밀번호는 8자 이상이며 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.");
        }

        // Find department if deptId provided
        Department department = null;
        if (req.getDeptId() != null) {
            department = departmentRepository.findById(req.getDeptId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "부서를 찾을 수 없습니다."));
        }

        User user = User.builder()
                .loginId(req.getLoginId())
                .password(passwordEncoder.encode(req.getPassword()))
                .userNm(req.getUserNm())
                .employeeNo(req.getEmployeeNo())
                .department(department)
                .email(req.getEmail())
                .tel(req.getTel())
                .status("ACTIVE")
                .build();

        user.setCreatedBy(currentUserId);
        User savedUser = userRepository.save(user);

        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(savedUser.getUserId());
        return toUserDetailResponse(savedUser, userRoles);
    }

    public UserDetailResponse updateUser(Long userId, UserUpdateRequest req, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if ("DELETED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "삭제된 사용자는 수정할 수 없습니다.");
        }

        // Create history for changed fields
        createUserHistoryForChanges(user, req, currentUserId);

        // Find department if deptId provided
        Department department = null;
        if (req.getDeptId() != null) {
            department = departmentRepository.findById(req.getDeptId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "부서를 찾을 수 없습니다."));
        }

        user.update(req.getUserNm(), req.getEmployeeNo(), department, req.getEmail(), req.getTel());
        user.setUpdatedBy(currentUserId);

        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(userId);
        return toUserDetailResponse(user, userRoles);
    }

    public void changeUserStatus(Long userId, UserStatusRequest req, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // Create history
        UserHistory history = UserHistory.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .userNm(user.getUserNm())
                .employeeNo(user.getEmployeeNo())
                .deptId(user.getDepartment() != null ? user.getDepartment().getDeptId() : null)
                .email(user.getEmail())
                .tel(user.getTel())
                .status(user.getStatus())
                .changedField("status")
                .beforeValue(user.getStatus())
                .afterValue(req.getStatus())
                .validFrom(user.getValidFrom())
                .validTo(user.getValidTo())
                .createdBy(currentUserId)
                .build();
        userHistoryRepository.save(history);

        user.changeStatus(req.getStatus());
        user.setUpdatedBy(currentUserId);
    }

    @Transactional(readOnly = true)
    public List<UserHistory> getUserHistory(Long userId) {
        return userHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void grantRole(Long userId, RoleGrantRequest req, Long currentUserId) {
        // Check user exists
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        // Check role exists
        if (!roleRepository.existsById(req.getRoleId())) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "역할을 찾을 수 없습니다.");
        }

        // Check not already granted
        UserRoleId userRoleId = new UserRoleId(userId, req.getRoleId());
        if (userRoleRepository.existsById(userRoleId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_VALUE, "이미 부여된 역할입니다.");
        }

        UserRole userRole = new UserRole(userId, req.getRoleId(), currentUserId);
        userRoleRepository.save(userRole);
    }

    public void revokeRole(Long userId, Long roleId, Long currentUserId) {
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
    }

    private void createUserHistoryForChanges(User user, UserUpdateRequest req, Long currentUserId) {
        List<String[]> changes = new ArrayList<>();

        if (!Objects.equals(user.getUserNm(), req.getUserNm())) {
            changes.add(new String[]{"userNm", user.getUserNm(), req.getUserNm()});
        }
        if (!Objects.equals(user.getEmployeeNo(), req.getEmployeeNo())) {
            changes.add(new String[]{"employeeNo", user.getEmployeeNo(), req.getEmployeeNo()});
        }
        Long currentDeptId = user.getDepartment() != null ? user.getDepartment().getDeptId() : null;
        if (!Objects.equals(currentDeptId, req.getDeptId())) {
            changes.add(new String[]{"deptId", String.valueOf(currentDeptId), String.valueOf(req.getDeptId())});
        }
        if (!Objects.equals(user.getEmail(), req.getEmail())) {
            changes.add(new String[]{"email", user.getEmail(), req.getEmail()});
        }
        if (!Objects.equals(user.getTel(), req.getTel())) {
            changes.add(new String[]{"tel", user.getTel(), req.getTel()});
        }

        for (String[] change : changes) {
            UserHistory history = UserHistory.builder()
                    .userId(user.getUserId())
                    .loginId(user.getLoginId())
                    .userNm(user.getUserNm())
                    .employeeNo(user.getEmployeeNo())
                    .deptId(currentDeptId)
                    .email(user.getEmail())
                    .tel(user.getTel())
                    .status(user.getStatus())
                    .changedField(change[0])
                    .beforeValue(change[1])
                    .afterValue(change[2])
                    .validFrom(user.getValidFrom())
                    .validTo(user.getValidTo())
                    .createdBy(currentUserId)
                    .build();
            userHistoryRepository.save(history);
        }
    }

    private UserListResponse toUserListResponse(User user) {
        String deptName = null;
        String companyName = null;
        if (user.getDepartment() != null) {
            deptName = user.getDepartment().getDeptNm();
            if (user.getDepartment().getCompany() != null) {
                companyName = user.getDepartment().getCompany().getCompanyNm();
            }
        }

        List<String> roles = userRoleRepository.findByUserIdWithRole(user.getUserId()).stream()
                .map(ur -> ur.getRole().getRoleCd())
                .toList();

        return UserListResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .userNm(user.getUserNm())
                .employeeNo(user.getEmployeeNo())
                .email(user.getEmail())
                .status(user.getStatus())
                .deptName(deptName)
                .companyName(companyName)
                .roles(roles)
                .build();
    }

    private UserDetailResponse toUserDetailResponse(User user, List<UserRole> userRoles) {
        Long deptId = null;
        String deptName = null;
        Long companyId = null;
        String companyName = null;

        if (user.getDepartment() != null) {
            deptId = user.getDepartment().getDeptId();
            deptName = user.getDepartment().getDeptNm();
            if (user.getDepartment().getCompany() != null) {
                companyId = user.getDepartment().getCompany().getCompanyId();
                companyName = user.getDepartment().getCompany().getCompanyNm();
            }
        }

        List<UserDetailResponse.RoleInfo> roleInfos = userRoles.stream()
                .map(ur -> UserDetailResponse.RoleInfo.builder()
                        .roleId(ur.getRole().getRoleId())
                        .roleNm(ur.getRole().getRoleNm())
                        .roleCd(ur.getRole().getRoleCd())
                        .build())
                .toList();

        return UserDetailResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .userNm(user.getUserNm())
                .employeeNo(user.getEmployeeNo())
                .deptId(deptId)
                .deptName(deptName)
                .companyId(companyId)
                .companyName(companyName)
                .email(user.getEmail())
                .tel(user.getTel())
                .status(user.getStatus())
                .validFrom(user.getValidFrom())
                .validTo(user.getValidTo())
                .lastLoginAt(user.getLastLoginAt())
                .pwdChangedAt(user.getPwdChangedAt())
                .roles(roleInfos)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
