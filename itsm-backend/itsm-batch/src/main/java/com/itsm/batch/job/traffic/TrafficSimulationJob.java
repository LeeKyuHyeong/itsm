package com.itsm.batch.job.traffic;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.log.LoginHistory;
import com.itsm.core.domain.log.MenuAccessLog;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.log.LoginHistoryRepository;
import com.itsm.core.repository.log.SimMenuAccessLogRepository;
import com.itsm.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficSimulationJob {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final SimMenuAccessLogRepository menuAccessLogRepository;
    private final NotificationService notificationService;

    private static final Random RANDOM = new Random();

    private static final String[][] MENUS = {
            {"/dashboard", "대시보드"},
            {"/incidents", "장애관리"},
            {"/incidents/create", "장애등록"},
            {"/service-requests", "서비스요청"},
            {"/service-requests/create", "서비스요청등록"},
            {"/changes", "변경관리"},
            {"/changes/create", "변경등록"},
            {"/assets", "자산관리"},
            {"/assets/hw", "HW자산목록"},
            {"/assets/sw", "SW자산목록"},
            {"/inspections", "정기점검"},
            {"/inspections/create", "점검등록"},
            {"/admin/users", "사용자관리"},
            {"/admin/roles", "역할관리"},
            {"/admin/codes", "코드관리"},
            {"/admin/menus", "메뉴관리"},
            {"/admin/companies", "회사관리"},
            {"/admin/batch-jobs", "배치관리"},
            {"/notifications", "알림"},
            {"/my-page", "마이페이지"},
    };

    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Edge/120.0.0.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 Chrome/120.0.0.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
    };

    private static final String[] IP_PREFIXES = {"192.168.1.", "192.168.2.", "192.168.3.", "10.0.1.", "10.0.2."};

    private static final String[][] NOTIFICATION_TEMPLATES = {
            {"SYSTEM_NOTICE", "시스템 점검 안내", "시스템 정기 점검이 예정되어 있습니다."},
            {"SYSTEM_NOTICE", "공지사항 등록", "새로운 공지사항이 등록되었습니다."},
            {"TASK_ASSIGNED", "업무 배정 알림", "새로운 업무가 배정되었습니다. 확인해 주세요."},
            {"TASK_ASSIGNED", "장애 처리 요청", "장애 티켓이 배정되었습니다."},
            {"APPROVAL_REQUEST", "승인 요청", "변경 요청에 대한 승인이 필요합니다."},
            {"APPROVAL_REQUEST", "서비스요청 승인", "서비스 요청에 대한 검토가 필요합니다."},
    };

    public void execute() {
        List<User> allUsers = userRepository.findAll();
        List<User> activeUsers = allUsers.stream()
                .filter(u -> "ACTIVE".equals(u.getStatus()))
                .toList();

        if (activeUsers.isEmpty()) {
            log.warn("활성 사용자가 없어 트래픽 시뮬레이션을 건너뜁니다.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();

        if (hour < 7 || hour > 20) {
            log.info("업무시간 외 - 최소 트래픽만 생성합니다.");
            generateMinimalTraffic(activeUsers, now);
            return;
        }

        log.info("[TrafficSimulationJob] 시작 - 활성 사용자 {}명", activeUsers.size());

        // 1. Generate logout events for existing open sessions
        generateLogoutEvents(activeUsers, now);

        // 2. Generate login events (3-8 users per 15min during business hours)
        generateLoginEvents(activeUsers, now);

        // 3. Generate menu access logs (10-30 per 15min)
        generateMenuAccessLogs(activeUsers, now);

        // 4. Generate notifications (2-5 per 15min)
        generateNotifications(activeUsers, now);

        log.info("[TrafficSimulationJob] 완료");
    }

    private void generateMinimalTraffic(List<User> activeUsers, LocalDateTime now) {
        // Off-hours: 1-2 logins, 2-5 page views, 0-1 notifications
        int loginCount = 1 + RANDOM.nextInt(2);
        List<User> shuffled = shuffleAndLimit(activeUsers, loginCount);

        for (User user : shuffled) {
            createLoginEvent(user, now);
        }

        int pageViews = 2 + RANDOM.nextInt(4);
        for (int i = 0; i < pageViews; i++) {
            User user = activeUsers.get(RANDOM.nextInt(activeUsers.size()));
            createMenuAccessLog(user, now);
        }
    }

    private void generateLoginEvents(List<User> activeUsers, LocalDateTime now) {
        int loginCount = 3 + RANDOM.nextInt(6); // 3-8
        List<User> candidates = shuffleAndLimit(activeUsers, Math.min(loginCount, activeUsers.size()));

        int created = 0;
        for (User user : candidates) {
            createLoginEvent(user, now);
            created++;
        }
        log.info("[TrafficSimulationJob] 로그인 이벤트 {}건 생성", created);
    }

    private void generateLogoutEvents(List<User> activeUsers, LocalDateTime now) {
        int logoutCount = 0;
        for (User user : activeUsers) {
            List<LoginHistory> openSessions =
                    loginHistoryRepository.findByUserIdAndLogoutAtIsNull(user.getUserId());
            for (LoginHistory session : openSessions) {
                // Randomly close some sessions (50% chance)
                if (RANDOM.nextBoolean()) {
                    LocalDateTime logoutTime = now.minusMinutes(RANDOM.nextInt(15));
                    session.recordLogout(logoutTime);
                    loginHistoryRepository.save(session);
                    logoutCount++;
                }
            }
        }
        if (logoutCount > 0) {
            log.info("[TrafficSimulationJob] 로그아웃 이벤트 {}건 처리", logoutCount);
        }
    }

    private void generateMenuAccessLogs(List<User> activeUsers, LocalDateTime now) {
        int accessCount = 10 + RANDOM.nextInt(21); // 10-30
        int created = 0;

        for (int i = 0; i < accessCount; i++) {
            User user = activeUsers.get(RANDOM.nextInt(activeUsers.size()));
            createMenuAccessLog(user, now);
            created++;
        }
        log.info("[TrafficSimulationJob] 메뉴 접근 로그 {}건 생성", created);
    }

    private void generateNotifications(List<User> activeUsers, LocalDateTime now) {
        int notiCount = 2 + RANDOM.nextInt(4); // 2-5
        int created = 0;

        for (int i = 0; i < notiCount; i++) {
            User user = activeUsers.get(RANDOM.nextInt(activeUsers.size()));
            String[][] template = NOTIFICATION_TEMPLATES;
            String[] selected = template[RANDOM.nextInt(template.length)];

            notificationService.sendNotification(
                    user.getUserId(),
                    selected[0],
                    selected[1],
                    selected[2],
                    "SIMULATION",
                    (long) (RANDOM.nextInt(1000) + 1)
            );
            created++;
        }
        log.info("[TrafficSimulationJob] 알림 {}건 생성", created);
    }

    private void createLoginEvent(User user, LocalDateTime now) {
        LocalDateTime loginTime = now.minusMinutes(RANDOM.nextInt(15));
        String ipAddress = IP_PREFIXES[RANDOM.nextInt(IP_PREFIXES.length)]
                + (RANDOM.nextInt(254) + 1);
        String userAgent = USER_AGENTS[RANDOM.nextInt(USER_AGENTS.length)];

        LoginHistory loginHistory = LoginHistory.builder()
                .userId(user.getUserId())
                .loginAt(loginTime)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        loginHistoryRepository.save(loginHistory);
    }

    private void createMenuAccessLog(User user, LocalDateTime now) {
        String[] menu = MENUS[RANDOM.nextInt(MENUS.length)];
        LocalDateTime accessTime = now.minusMinutes(RANDOM.nextInt(15));

        MenuAccessLog accessLog = MenuAccessLog.builder()
                .userId(user.getUserId())
                .menuPath(menu[0])
                .menuNm(menu[1])
                .accessedAt(accessTime)
                .build();
        menuAccessLogRepository.save(accessLog);
    }

    private List<User> shuffleAndLimit(List<User> users, int limit) {
        List<User> mutable = new java.util.ArrayList<>(users);
        Collections.shuffle(mutable, RANDOM);
        return mutable.subList(0, Math.min(limit, mutable.size()));
    }
}
