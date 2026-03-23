package com.itsm.api.security;

import com.itsm.api.dto.batch.BatchJobUpdateRequest;
import com.itsm.api.dto.common.*;
import com.itsm.api.dto.user.*;
import com.itsm.api.service.batch.BatchJobService;
import com.itsm.api.service.common.CommonCodeService;
import com.itsm.api.service.common.NotificationPolicyService;
import com.itsm.api.service.common.SlaPolicyService;
import com.itsm.api.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("메서드 레벨 권한 검증 테스트")
class MethodSecurityTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CommonCodeService commonCodeService;

    @Autowired
    private SlaPolicyService slaPolicyService;

    @Autowired
    private NotificationPolicyService notificationPolicyService;

    @Autowired
    private BatchJobService batchJobService;

    @Nested
    @DisplayName("UserService 권한 검증")
    class UserServiceSecurity {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 사용자 생성이 거부된다")
        void createUser_withUserRole_denied() {
            UserCreateRequest req = new UserCreateRequest(
                    "newuser", "Password1!", "신규", null, null, null, null);
            assertThatThrownBy(() -> userService.createUser(req, 1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN은 사용자 생성이 허용된다")
        void createUser_withSuperAdmin_allowed() {
            // SUPER_ADMIN은 접근 가능 (비즈니스 예외는 별개)
            assertThatCode(() -> {
                try {
                    userService.createUser(
                            new UserCreateRequest("test", "Password1!", "테스트", null, null, null, null), 1L);
                } catch (AccessDeniedException e) {
                    throw e; // 권한 예외만 재발생
                } catch (Exception e) {
                    // 비즈니스 예외는 무시 (권한 통과 확인 목적)
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @WithMockUser(roles = "ITSM_ADMIN")
        @DisplayName("ITSM_ADMIN은 사용자 생성이 허용된다")
        void createUser_withItsmAdmin_allowed() {
            assertThatCode(() -> {
                try {
                    userService.createUser(
                            new UserCreateRequest("test2", "Password1!", "테스트2", null, null, null, null), 1L);
                } catch (AccessDeniedException e) {
                    throw e;
                } catch (Exception e) {
                    // 비즈니스 예외 무시
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 사용자 상태 변경이 거부된다")
        void changeUserStatus_withUserRole_denied() {
            assertThatThrownBy(() -> userService.changeUserStatus(1L, new UserStatusRequest("INACTIVE"), 1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 역할 부여가 거부된다")
        void grantRole_withUserRole_denied() {
            assertThatThrownBy(() -> userService.grantRole(1L, new RoleGrantRequest(1L), 1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 역할 회수가 거부된다")
        void revokeRole_withUserRole_denied() {
            assertThatThrownBy(() -> userService.revokeRole(1L, 1L, 1L))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("CommonCodeService 권한 검증")
    class CommonCodeServiceSecurity {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 공통코드 그룹 생성이 거부된다")
        void createGroup_withUserRole_denied() {
            CommonCodeGroupCreateRequest req = new CommonCodeGroupCreateRequest(
                    "테스트", "Test", "TEST", "desc");
            assertThatThrownBy(() -> commonCodeService.createGroup(req, 1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN은 공통코드 그룹 생성이 허용된다")
        void createGroup_withSuperAdmin_allowed() {
            assertThatCode(() -> {
                try {
                    commonCodeService.createGroup(
                            new CommonCodeGroupCreateRequest("테스트", "Test", "TEST", "desc"), 1L);
                } catch (AccessDeniedException e) {
                    throw e;
                } catch (Exception e) {
                    // 비즈니스 예외 무시
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 공통코드 상태 변경이 거부된다")
        void changeGroupStatus_withUserRole_denied() {
            assertThatThrownBy(() -> commonCodeService.changeGroupStatus(1L, "N"))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("SlaPolicyService 권한 검증")
    class SlaPolicyServiceSecurity {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 SLA 정책 생성이 거부된다")
        void createPolicy_withUserRole_denied() {
            SlaPolicyCreateRequest req = new SlaPolicyCreateRequest(null, "HIGH", 24, 80);
            assertThatThrownBy(() -> slaPolicyService.createPolicy(req, 1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN은 SLA 정책 생성이 허용된다")
        void createPolicy_withSuperAdmin_allowed() {
            assertThatCode(() -> {
                try {
                    slaPolicyService.createPolicy(
                            new SlaPolicyCreateRequest(null, "HIGH", 24, 80), 1L);
                } catch (AccessDeniedException e) {
                    throw e;
                } catch (Exception e) {
                    // 비즈니스 예외 무시
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 SLA 정책 상태 변경이 거부된다")
        void changePolicyStatus_withUserRole_denied() {
            assertThatThrownBy(() -> slaPolicyService.changePolicyStatus(1L, "N"))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("NotificationPolicyService 권한 검증")
    class NotificationPolicyServiceSecurity {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 알림 정책 생성이 거부된다")
        void createPolicy_withUserRole_denied() {
            NotificationPolicyCreateRequest req = new NotificationPolicyCreateRequest(
                    "INCIDENT", "CREATED", "ADMIN");
            assertThatThrownBy(() -> notificationPolicyService.createPolicy(req, 1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @WithMockUser(roles = "ITSM_ADMIN")
        @DisplayName("ITSM_ADMIN은 알림 정책 생성이 허용된다")
        void createPolicy_withItsmAdmin_allowed() {
            assertThatCode(() -> {
                try {
                    notificationPolicyService.createPolicy(
                            new NotificationPolicyCreateRequest("INCIDENT", "CREATED", "ADMIN"), 1L);
                } catch (AccessDeniedException e) {
                    throw e;
                } catch (Exception e) {
                    // 비즈니스 예외 무시
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("BatchJobService 권한 검증")
    class BatchJobServiceSecurity {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 배치 작업 수정이 거부된다")
        void updateJob_withUserRole_denied() {
            BatchJobUpdateRequest req = new BatchJobUpdateRequest("0 0 * * * *", "Y", "desc", "Desc");
            assertThatThrownBy(() -> batchJobService.updateJob(1L, req, 1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("일반 사용자는 배치 작업 즉시 실행이 거부된다")
        void executeNow_withUserRole_denied() {
            assertThatThrownBy(() -> batchJobService.executeNow(1L, 1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("SUPER_ADMIN은 배치 작업 수정이 허용된다")
        void updateJob_withSuperAdmin_allowed() {
            assertThatCode(() -> {
                try {
                    batchJobService.updateJob(1L,
                            new BatchJobUpdateRequest("0 0 * * * *", "Y", "desc", "Desc"), 1L);
                } catch (AccessDeniedException e) {
                    throw e;
                } catch (Exception e) {
                    // 비즈니스 예외 무시
                }
            }).doesNotThrowAnyException();
        }
    }
}
