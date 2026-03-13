package com.itsm.batch.job.traffic;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.log.LoginHistory;
import com.itsm.core.domain.log.MenuAccessLog;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.log.LoginHistoryRepository;
import com.itsm.core.repository.log.SimMenuAccessLogRepository;
import com.itsm.core.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrafficSimulationJob 테스트")
class TrafficSimulationJobTest {

    @InjectMocks
    private TrafficSimulationJob job;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @Mock
    private SimMenuAccessLogRepository menuAccessLogRepository;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("활성 사용자가 없으면 트래픽 생성을 스킵한다")
    void execute_noActiveUsers_skip() {
        // given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        verify(loginHistoryRepository, never()).save(any(LoginHistory.class));
        verify(menuAccessLogRepository, never()).save(any(MenuAccessLog.class));
        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("ACTIVE가 아닌 사용자는 필터링된다")
    void execute_filtersNonActiveUsers() {
        // given
        User activeUser = mock(User.class);
        lenient().doReturn(1L).when(activeUser).getUserId();
        lenient().doReturn("사용자1").when(activeUser).getUserNm();
        lenient().doReturn("ACTIVE").when(activeUser).getStatus();

        User deletedUser = mock(User.class);
        lenient().doReturn(2L).when(deletedUser).getUserId();
        lenient().doReturn("삭제사용자").when(deletedUser).getUserNm();
        lenient().doReturn("DELETED").when(deletedUser).getStatus();

        when(userRepository.findAll()).thenReturn(List.of(activeUser, deletedUser));
        lenient().when(loginHistoryRepository.findByUserIdAndLogoutAtIsNull(anyLong()))
                .thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then - login events should only be created for the active user (userId=1)
        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository, atLeastOnce()).save(captor.capture());

        List<Long> savedUserIds = captor.getAllValues().stream()
                .map(LoginHistory::getUserId)
                .distinct()
                .toList();
        assertThat(savedUserIds).contains(1L);
        assertThat(savedUserIds).doesNotContain(2L);
    }

    @Test
    @DisplayName("로그인 이벤트를 생성한다")
    void execute_generateLoginEvents() {
        // given
        List<User> users = createMockUsers(10);
        when(userRepository.findAll()).thenReturn(users);
        lenient().when(loginHistoryRepository.findByUserIdAndLogoutAtIsNull(anyLong()))
                .thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        verify(loginHistoryRepository, atLeastOnce()).save(any(LoginHistory.class));
    }

    @Test
    @DisplayName("로그인 이벤트 생성 시 IP 주소와 User-Agent가 설정된다")
    void execute_loginEventHasIpAndUserAgent() {
        // given
        List<User> users = createMockUsers(10);
        when(userRepository.findAll()).thenReturn(users);
        lenient().when(loginHistoryRepository.findByUserIdAndLogoutAtIsNull(anyLong()))
                .thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository, atLeastOnce()).save(captor.capture());

        LoginHistory saved = captor.getAllValues().get(0);
        assertThat(saved.getIpAddress()).isNotBlank();
        assertThat(saved.getUserAgent()).isNotBlank();
        assertThat(saved.getLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("메뉴 접근 로그를 생성한다")
    void execute_generateMenuAccessLogs() {
        // given
        List<User> users = createMockUsers(10);
        when(userRepository.findAll()).thenReturn(users);
        lenient().when(loginHistoryRepository.findByUserIdAndLogoutAtIsNull(anyLong()))
                .thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        verify(menuAccessLogRepository, atLeastOnce()).save(any(MenuAccessLog.class));
    }

    @Test
    @DisplayName("메뉴 접근 로그에 경로와 메뉴명이 설정된다")
    void execute_menuAccessLogHasPathAndName() {
        // given
        List<User> users = createMockUsers(10);
        when(userRepository.findAll()).thenReturn(users);
        lenient().when(loginHistoryRepository.findByUserIdAndLogoutAtIsNull(anyLong()))
                .thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        ArgumentCaptor<MenuAccessLog> captor = ArgumentCaptor.forClass(MenuAccessLog.class);
        verify(menuAccessLogRepository, atLeastOnce()).save(captor.capture());

        MenuAccessLog saved = captor.getAllValues().get(0);
        assertThat(saved.getMenuPath()).isNotBlank();
        assertThat(saved.getMenuNm()).isNotBlank();
        assertThat(saved.getAccessedAt()).isNotNull();
    }

    @Test
    @DisplayName("업무시간에는 알림을 생성한다")
    void execute_generateNotifications_duringBusinessHours() {
        // given
        List<User> users = createMockUsers(10);
        when(userRepository.findAll()).thenReturn(users);
        lenient().when(loginHistoryRepository.findByUserIdAndLogoutAtIsNull(anyLong()))
                .thenReturn(Collections.emptyList());

        // when
        job.execute();

        // then
        int hour = LocalDateTime.now().getHour();
        if (hour >= 7 && hour <= 20) {
            // During business hours, notifications should be generated
            verify(notificationService, atLeastOnce()).sendNotification(
                    anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
        }
        // Outside business hours, minimal traffic (no notifications) is acceptable
    }

    @Test
    @DisplayName("기존 로그인 세션이 있으면 로그아웃 처리를 시도한다")
    void execute_generateLogoutEvents() {
        // given
        List<User> users = createMockUsers(5);
        when(userRepository.findAll()).thenReturn(users);

        LoginHistory openSession = mock(LoginHistory.class);
        lenient().when(loginHistoryRepository.findByUserIdAndLogoutAtIsNull(anyLong()))
                .thenReturn(List.of(openSession));

        // when - execute multiple times to overcome 50% randomness
        for (int i = 0; i < 20; i++) {
            job.execute();
        }

        // then - at least one logout should have been recorded over many executions
        // During business hours, logout is attempted; off-hours only minimal traffic
        int hour = LocalDateTime.now().getHour();
        if (hour >= 7 && hour <= 20) {
            verify(openSession, atLeastOnce()).recordLogout(any());
        }
    }

    private List<User> createMockUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = mock(User.class);
            lenient().doReturn((long) i).when(user).getUserId();
            lenient().doReturn("사용자" + i).when(user).getUserNm();
            lenient().doReturn("ACTIVE").when(user).getStatus();
            users.add(user);
        }
        return users;
    }
}
