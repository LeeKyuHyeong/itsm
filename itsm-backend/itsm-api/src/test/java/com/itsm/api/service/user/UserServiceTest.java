package com.itsm.api.service.user;

import com.itsm.api.dto.user.*;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.company.Department;
import com.itsm.core.domain.user.*;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.company.DepartmentRepository;
import com.itsm.core.repository.user.*;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserHistoryRepository userHistoryRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User activeUser;
    private Company company;
    private Department department;
    private Role role;
    private UserRole userRole;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .companyNm("테스트회사")
                .bizNo("123-45-67890")
                .status("ACTIVE")
                .build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        department = Department.builder()
                .deptNm("IT부서")
                .company(company)
                .status("ACTIVE")
                .build();
        ReflectionTestUtils.setField(department, "deptId", 1L);

        activeUser = User.builder()
                .loginId("testuser")
                .password("encodedPassword")
                .userNm("테스트사용자")
                .employeeNo("EMP001")
                .department(department)
                .email("test@test.com")
                .tel("010-1234-5678")
                .status("ACTIVE")
                .build();
        ReflectionTestUtils.setField(activeUser, "userId", 1L);

        role = Role.builder()
                .roleNm("관리자")
                .roleCd("ADMIN")
                .build();
        ReflectionTestUtils.setField(role, "roleId", 1L);

        userRole = new UserRole(1L, 1L, 1L);
        ReflectionTestUtils.setField(userRole, "role", role);
    }

    @Test
    @DisplayName("키워드로 사용자 목록을 조회한다")
    void getUsers_withKeyword_searchesUsers() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(activeUser), pageable, 1);
        given(userRepository.search("test", pageable)).willReturn(userPage);
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));

        // when
        Page<UserListResponse> result = userService.getUsers("test", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserNm()).isEqualTo("테스트사용자");
        assertThat(result.getContent().get(0).getRoles()).containsExactly("ADMIN");
        verify(userRepository).search("test", pageable);
    }

    @Test
    @DisplayName("사용자를 성공적으로 생성한다")
    void createUser_success() {
        // given
        UserCreateRequest req = new UserCreateRequest(
                "newuser", "Password1!", "신규사용자", "EMP002", 1L, "new@test.com", "010-0000-0000");

        given(userRepository.existsByLoginId("newuser")).willReturn(false);
        given(passwordEncoder.encode("Password1!")).willReturn("encodedNewPassword");
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedUser, "userId", 2L);
            return savedUser;
        });
        given(userRoleRepository.findByUserIdWithRole(2L)).willReturn(Collections.emptyList());

        // when
        UserDetailResponse result = userService.createUser(req, 1L);

        // then
        assertThat(result.getLoginId()).isEqualTo("newuser");
        assertThat(result.getUserNm()).isEqualTo("신규사용자");
        assertThat(result.getDeptName()).isEqualTo("IT부서");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 로그인 ID로 사용자 생성 시 예외가 발생한다")
    void createUser_duplicateLoginId_throwsException() {
        // given
        UserCreateRequest req = new UserCreateRequest(
                "testuser", "Password1!", "중복사용자", null, null, null, null);
        given(userRepository.existsByLoginId("testuser")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(req, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_VALUE);
    }

    @Test
    @DisplayName("비밀번호 정책에 위반되는 비밀번호로 사용자 생성 시 예외가 발생한다")
    void createUser_invalidPasswordPolicy_throwsException() {
        // given
        UserCreateRequest req = new UserCreateRequest(
                "newuser", "weak", "신규사용자", null, null, null, null);
        given(userRepository.existsByLoginId("newuser")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.createUser(req, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("사용자 수정 시 변경 이력이 생성된다")
    void updateUser_createsHistory() {
        // given
        UserUpdateRequest req = new UserUpdateRequest(
                "변경된이름", "EMP001", 1L, "changed@test.com", "010-1234-5678");

        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(userRole));

        // when
        UserDetailResponse result = userService.updateUser(1L, req, 1L);

        // then
        assertThat(result.getUserNm()).isEqualTo("변경된이름");
        // userNm changed and email changed = 2 history records
        verify(userHistoryRepository, atLeastOnce()).save(any(UserHistory.class));
    }

    @Test
    @DisplayName("사용자 상태를 DELETED로 변경하면 loginId가 마스킹된다")
    void changeUserStatus_toDeleted_masksLoginId() {
        // given
        UserStatusRequest req = new UserStatusRequest("DELETED");
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));

        // when
        userService.changeUserStatus(1L, req, 1L);

        // then
        assertThat(activeUser.getStatus()).isEqualTo("DELETED");
        assertThat(activeUser.getLoginId()).startsWith("DELETED_");
        verify(userHistoryRepository).save(any(UserHistory.class));
    }

    @Test
    @DisplayName("역할 부여에 성공한다")
    void grantRole_success() {
        // given
        RoleGrantRequest req = new RoleGrantRequest(2L);
        given(userRepository.existsById(1L)).willReturn(true);
        given(roleRepository.existsById(2L)).willReturn(true);
        given(userRoleRepository.existsById(any(UserRoleId.class))).willReturn(false);

        // when
        userService.grantRole(1L, req, 1L);

        // then
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    @DisplayName("역할 회수에 성공한다")
    void revokeRole_success() {
        // when
        userService.revokeRole(1L, 2L, 1L);

        // then
        verify(userRoleRepository).deleteByUserIdAndRoleId(1L, 2L);
    }
}
