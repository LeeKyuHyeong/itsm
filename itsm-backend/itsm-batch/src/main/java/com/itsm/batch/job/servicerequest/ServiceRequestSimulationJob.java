package com.itsm.batch.job.servicerequest;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.servicerequest.ServiceRequest;
import com.itsm.core.domain.servicerequest.ServiceRequestAssignee;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.servicerequest.ServiceRequestAssigneeRepository;
import com.itsm.core.repository.servicerequest.ServiceRequestRepository;
import com.itsm.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestSimulationJob {

    private static final int DAILY_MIN = 3;
    private static final int DAILY_MAX = 8;
    private static final Long SYSTEM_USER_ID = 1L;

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestAssigneeRepository serviceRequestAssigneeRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    private final Random random = new Random();

    private static final String[][] SR_TEMPLATES = {
            {"계정 생성/변경", "[요청] 신규 입사자 계정 생성 요청", "신규 입사자의 AD 계정 및 이메일 계정 생성을 요청합니다."},
            {"권한 요청", "[요청] ERP 시스템 접근 권한 요청", "업무 수행을 위해 ERP 시스템 읽기/쓰기 권한을 요청합니다."},
            {"SW 설치", "[요청] Microsoft Office 설치 요청", "업무용 PC에 Microsoft Office 설치를 요청합니다."},
            {"HW 교체", "[요청] 모니터 교체 요청", "모니터 화면 불량으로 교체를 요청합니다."},
            {"VPN 설정", "[요청] VPN 접속 설정 요청", "재택근무를 위한 VPN 접속 설정을 요청합니다."},
            {"이메일 설정", "[요청] 메일링 리스트 추가 요청", "팀 메일링 리스트에 추가를 요청합니다."},
            {"프린터 설정", "[요청] 프린터 드라이버 설치 요청", "3층 복합기 프린터 드라이버 설치를 요청합니다."},
            {"네트워크 접근 요청", "[요청] 네트워크 포트 활성화 요청", "사무실 이동으로 인한 네트워크 포트 활성화를 요청합니다."},
    };

    private static final String[] PRIORITIES = {"CRITICAL", "HIGH", "MEDIUM", "LOW"};

    private static final String[][] SATISFACTION_COMMENTS = {
            {"빠르게 처리해주셔서 감사합니다.", "친절하고 신속한 처리 감사합니다."},
            {"잘 처리되었습니다. 감사합니다.", "원활하게 처리되었습니다."},
            {"처리는 되었으나 시간이 다소 소요되었습니다.", "추가 안내가 있었으면 좋겠습니다."},
    };

    public void execute() {
        log.info("[ServiceRequestSimulationJob] 시작");

        List<Company> companies = companyRepository.findAll();
        List<User> users = userRepository.findAll();

        if (companies.isEmpty() || users.isEmpty()) {
            log.info("[ServiceRequestSimulationJob] 회사 또는 사용자 데이터 없음 - 스킵");
            return;
        }

        createNewServiceRequests(companies);
        assignReceivedRequests(users);
        startAssignedRequests();
        completeInProgressRequests();
        closeCompletedRequests();

        log.info("[ServiceRequestSimulationJob] 완료");
    }

    private void createNewServiceRequests(List<Company> companies) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        long todayCount = serviceRequestRepository.countByCreatedAtBetween(todayStart, todayEnd);

        int dailyQuota = DAILY_MIN + random.nextInt(DAILY_MAX - DAILY_MIN + 1);
        if (todayCount >= dailyQuota) {
            log.info("[ServiceRequestSimulationJob] 일일 할당량({}) 충족 - 생성 스킵", dailyQuota);
            return;
        }

        int toCreate = (int) (dailyQuota - todayCount);
        // 매 시간 실행되므로 한 번에 1-2개씩 생성
        toCreate = Math.min(toCreate, 1 + random.nextInt(2));

        for (int i = 0; i < toCreate; i++) {
            String[][] template = {SR_TEMPLATES[random.nextInt(SR_TEMPLATES.length)]};
            Company company = companies.get(random.nextInt(companies.size()));
            String priority = PRIORITIES[1 + random.nextInt(PRIORITIES.length - 1)]; // MEDIUM, LOW 위주

            ServiceRequest sr = ServiceRequest.builder()
                    .title(template[0][1])
                    .content(template[0][2])
                    .requestTypeCd(template[0][0])
                    .priorityCd(priority)
                    .occurredAt(LocalDateTime.now().minusMinutes(random.nextInt(60)))
                    .company(company)
                    .build();

            serviceRequestRepository.save(sr);
            log.info("[ServiceRequestSimulationJob] 신규 SR 생성: {}", sr.getTitle());
        }
    }

    private void assignReceivedRequests(List<User> users) {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        List<ServiceRequest> receivedRequests = serviceRequestRepository
                .findByStatusCdAndUpdatedAtBefore("RECEIVED", thirtyMinutesAgo);

        for (ServiceRequest sr : receivedRequests) {
            User assignee = users.get(random.nextInt(users.size()));

            sr.changeStatus("ASSIGNED");

            ServiceRequestAssignee sra = ServiceRequestAssignee.builder()
                    .requestId(sr.getRequestId())
                    .userId(assignee.getUserId())
                    .grantedBy(SYSTEM_USER_ID)
                    .build();

            serviceRequestAssigneeRepository.save(sra);
            log.info("[ServiceRequestSimulationJob] SR #{} 배정 → 사용자 {}", sr.getRequestId(), assignee.getUserId());
        }
    }

    private void startAssignedRequests() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<ServiceRequest> assignedRequests = serviceRequestRepository
                .findByStatusCdAndUpdatedAtBefore("ASSIGNED", oneHourAgo);

        for (ServiceRequest sr : assignedRequests) {
            sr.changeStatus("IN_PROGRESS");
            log.info("[ServiceRequestSimulationJob] SR #{} 처리 시작", sr.getRequestId());
        }
    }

    private void completeInProgressRequests() {
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        List<ServiceRequest> inProgressRequests = serviceRequestRepository
                .findByStatusCdAndUpdatedAtBefore("IN_PROGRESS", twoHoursAgo);

        for (ServiceRequest sr : inProgressRequests) {
            sr.changeStatus("PENDING_COMPLETE");
            log.info("[ServiceRequestSimulationJob] SR #{} 완료대기", sr.getRequestId());
        }
    }

    private void closeCompletedRequests() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<ServiceRequest> pendingRequests = serviceRequestRepository
                .findByStatusCdAndUpdatedAtBefore("PENDING_COMPLETE", oneHourAgo);

        for (ServiceRequest sr : pendingRequests) {
            int score = generateSatisfactionScore();
            String comment = generateSatisfactionComment(score);

            sr.submitSatisfaction(score, comment);
            sr.changeStatus("CLOSED");
            log.info("[ServiceRequestSimulationJob] SR #{} 마감 (만족도: {}점)", sr.getRequestId(), score);
        }
    }

    private int generateSatisfactionScore() {
        // 3.5~5.0 distribution: mostly 4-5, occasionally 3
        double rand = random.nextDouble();
        if (rand < 0.1) return 3;       // 10%
        if (rand < 0.45) return 4;      // 35%
        return 5;                        // 55%
    }

    private String generateSatisfactionComment(int score) {
        return switch (score) {
            case 5 -> SATISFACTION_COMMENTS[0][random.nextInt(SATISFACTION_COMMENTS[0].length)];
            case 4 -> SATISFACTION_COMMENTS[1][random.nextInt(SATISFACTION_COMMENTS[1].length)];
            default -> SATISFACTION_COMMENTS[2][random.nextInt(SATISFACTION_COMMENTS[2].length)];
        };
    }
}
