package com.itsm.core.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("Builder로 User 생성 시 기본값이 설정된다")
    void builder_createsUserWithDefaults() {
        // given & when
        User user = User.builder()
                .loginId("testuser")
                .password("encoded_password")
                .userNm("테스트유저")
                .email("test@example.com")
                .build();

        // then
        assertThat(user.getLoginId()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("encoded_password");
        assertThat(user.getUserNm()).isEqualTo("테스트유저");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getStatus()).isEqualTo("ACTIVE");
        assertThat(user.getLoginFailCnt()).isEqualTo(0);
        assertThat(user.getValidFrom()).isNotNull();
        assertThat(user.getValidTo()).isNull();
        assertThat(user.getLastLoginAt()).isNull();
        assertThat(user.getPwdChangedAt()).isNull();
    }

    @Test
    @DisplayName("Builder로 User 생성 시 status를 지정하면 해당 값이 사용된다")
    void builder_withExplicitStatus() {
        // given & when
        User user = User.builder()
                .loginId("testuser")
                .password("pw")
                .userNm("테스트")
                .status("INACTIVE")
                .build();

        // then
        assertThat(user.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("changeStatus를 DELETED로 변경하면 loginId가 마스킹되고 validTo가 설정된다")
    void changeStatus_toDeleted_masksLoginIdAndSetsValidTo() {
        // given
        User user = User.builder()
                .loginId("testuser")
                .password("pw")
                .userNm("테스트")
                .build();
        // userId is null in unit test (not persisted)

        // when
        user.changeStatus("DELETED");

        // then
        assertThat(user.getStatus()).isEqualTo("DELETED");
        assertThat(user.getLoginId()).isEqualTo("DELETED_null_testuser");
        assertThat(user.getValidTo()).isNotNull();
    }

    @Test
    @DisplayName("changeStatus를 DELETED가 아닌 상태로 변경하면 loginId가 변하지 않는다")
    void changeStatus_toNonDeleted_doesNotMaskLoginId() {
        // given
        User user = User.builder()
                .loginId("testuser")
                .password("pw")
                .userNm("테스트")
                .build();

        // when
        user.changeStatus("INACTIVE");

        // then
        assertThat(user.getStatus()).isEqualTo("INACTIVE");
        assertThat(user.getLoginId()).isEqualTo("testuser");
        assertThat(user.getValidTo()).isNull();
    }

    @Test
    @DisplayName("changePassword로 비밀번호 변경 시 password와 pwdChangedAt이 업데이트된다")
    void changePassword_updatesPasswordAndPwdChangedAt() {
        // given
        User user = User.builder()
                .loginId("testuser")
                .password("old_password")
                .userNm("테스트")
                .build();

        // when
        user.changePassword("new_encoded_password");

        // then
        assertThat(user.getPassword()).isEqualTo("new_encoded_password");
        assertThat(user.getPwdChangedAt()).isNotNull();
    }

    @Test
    @DisplayName("recordLoginSuccess 호출 시 lastLoginAt이 설정되고 loginFailCnt가 0이 된다")
    void recordLoginSuccess_setsLastLoginAtAndResetsFailCount() {
        // given
        User user = User.builder()
                .loginId("testuser")
                .password("pw")
                .userNm("테스트")
                .build();
        user.recordLoginFailure();
        user.recordLoginFailure();
        assertThat(user.getLoginFailCnt()).isEqualTo(2);

        // when
        user.recordLoginSuccess();

        // then
        assertThat(user.getLastLoginAt()).isNotNull();
        assertThat(user.getLoginFailCnt()).isEqualTo(0);
    }

    @Test
    @DisplayName("recordLoginFailure 호출 시 loginFailCnt가 1 증가한다")
    void recordLoginFailure_incrementsLoginFailCnt() {
        // given
        User user = User.builder()
                .loginId("testuser")
                .password("pw")
                .userNm("테스트")
                .build();
        assertThat(user.getLoginFailCnt()).isEqualTo(0);

        // when
        user.recordLoginFailure();

        // then
        assertThat(user.getLoginFailCnt()).isEqualTo(1);

        // when
        user.recordLoginFailure();

        // then
        assertThat(user.getLoginFailCnt()).isEqualTo(2);
    }

    @Test
    @DisplayName("lock 호출 시 status가 LOCKED로 변경된다")
    void lock_changesStatusToLocked() {
        // given
        User user = User.builder()
                .loginId("testuser")
                .password("pw")
                .userNm("테스트")
                .build();
        assertThat(user.getStatus()).isEqualTo("ACTIVE");

        // when
        user.lock();

        // then
        assertThat(user.getStatus()).isEqualTo("LOCKED");
        assertThat(user.isLocked()).isTrue();
    }

    @Test
    @DisplayName("unlock 호출 시 status가 ACTIVE로 변경되고 loginFailCnt가 0이 된다")
    void unlock_changesStatusToActiveAndResetsFailCount() {
        // given
        User user = User.builder()
                .loginId("testuser")
                .password("pw")
                .userNm("테스트")
                .build();
        user.recordLoginFailure();
        user.recordLoginFailure();
        user.lock();

        // when
        user.unlock();

        // then
        assertThat(user.getStatus()).isEqualTo("ACTIVE");
        assertThat(user.isLocked()).isFalse();
        assertThat(user.getLoginFailCnt()).isEqualTo(0);
    }

    @Test
    @DisplayName("update 호출 시 사용자 정보가 변경된다")
    void update_changesUserInfo() {
        // given
        User user = User.builder()
                .loginId("testuser")
                .password("pw")
                .userNm("원래이름")
                .employeeNo("EMP001")
                .email("old@example.com")
                .tel("010-1111-1111")
                .build();

        // when
        user.update("새이름", "EMP002", null, "new@example.com", "010-2222-2222");

        // then
        assertThat(user.getUserNm()).isEqualTo("새이름");
        assertThat(user.getEmployeeNo()).isEqualTo("EMP002");
        assertThat(user.getDepartment()).isNull();
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getTel()).isEqualTo("010-2222-2222");
    }

    @Test
    @DisplayName("resetLoginFailCount 호출 시 loginFailCnt가 0이 된다")
    void resetLoginFailCount_resetsToZero() {
        // given
        User user = User.builder()
                .loginId("testuser")
                .password("pw")
                .userNm("테스트")
                .build();
        user.recordLoginFailure();
        user.recordLoginFailure();
        user.recordLoginFailure();
        assertThat(user.getLoginFailCnt()).isEqualTo(3);

        // when
        user.resetLoginFailCount();

        // then
        assertThat(user.getLoginFailCnt()).isEqualTo(0);
    }
}
