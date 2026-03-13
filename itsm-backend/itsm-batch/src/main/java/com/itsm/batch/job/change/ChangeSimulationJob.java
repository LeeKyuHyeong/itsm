package com.itsm.batch.job.change;

import com.itsm.core.domain.change.Change;
import com.itsm.core.domain.change.ChangeApprover;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.change.ChangeApproverRepository;
import com.itsm.core.repository.change.ChangeRepository;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChangeSimulationJob {

    private static final int WEEKLY_MIN = 2;
    private static final int WEEKLY_MAX = 4;
    private static final double REJECTION_RATE = 0.12;

    private final ChangeRepository changeRepository;
    private final ChangeApproverRepository changeApproverRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    private final Random random = new Random();

    private static final String[][] CHANGE_TEMPLATES = {
            {"패치 적용", "[변경] WEB-SVR01 보안 패치 적용", "CVE-2026-12345 취약점 대응을 위한 보안 패치를 적용합니다."},
            {"설정 변경", "[변경] 방화벽 정책 변경", "신규 서비스 오픈에 따른 방화벽 인바운드 정책 추가"},
            {"업그레이드", "[변경] 메일 서비스 버전 업그레이드", "성능 개선 및 버그 수정을 위한 버전 업그레이드"},
            {"인프라 확장", "[변경] 웹 서버 스케일아웃", "트래픽 증가 대응을 위한 웹 서버 2대 추가"},
            {"보안 정책 변경", "[변경] 패스워드 정책 강화", "보안 감사 결과에 따른 패스워드 복잡도/주기 정책 변경"},
            {"네트워크 변경", "[변경] VLAN 재구성", "네트워크 세그먼트 분리를 위한 VLAN 재구성"},
    };

    private static final String[] PRIORITIES = {"CRITICAL", "HIGH", "MEDIUM", "LOW"};

    private static final String[] ROLLBACK_PLANS = {
            "이전 버전 패키지로 롤백, 롤백 소요시간: 약 30분",
            "변경 전 설정 파일 백업본으로 복원",
            "스냅샷 기반 원복, 복원 소요시간: 약 1시간",
    };

    public void execute() {
        log.info("[ChangeSimulationJob] 시작");

        List<Company> companies = companyRepository.findAll();
        List<User> users = userRepository.findAll();

        if (companies.isEmpty() || users.isEmpty()) {
            log.info("[ChangeSimulationJob] 회사 또는 사용자 데이터 없음 - 스킵");
            return;
        }

        createNewChanges(companies);
        submitDraftsForApproval();
        processApprovals(users);
        startApprovedChanges();
        completeInProgressChanges();
        closeCompletedChanges();

        log.info("[ChangeSimulationJob] 완료");
    }

    private void createNewChanges(List<Company> companies) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStartDt = weekStart.atStartOfDay();
        LocalDateTime weekEndDt = weekStart.plusWeeks(1).atStartOfDay();

        long weeklyCount = changeRepository.countByCreatedAtBetween(weekStartDt, weekEndDt);
        int weeklyQuota = WEEKLY_MIN + random.nextInt(WEEKLY_MAX - WEEKLY_MIN + 1);

        if (weeklyCount >= weeklyQuota) {
            log.info("[ChangeSimulationJob] 주간 할당량({}) 충족 - 생성 스킵", weeklyQuota);
            return;
        }

        // 주중(월~금)에만 생성
        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return;
        }

        String[][] template = {CHANGE_TEMPLATES[random.nextInt(CHANGE_TEMPLATES.length)]};
        Company company = companies.get(random.nextInt(companies.size()));
        String priority = PRIORITIES[1 + random.nextInt(PRIORITIES.length - 1)];

        Change change = Change.builder()
                .title(template[0][1])
                .content(template[0][2])
                .changeTypeCd(template[0][0])
                .priorityCd(priority)
                .scheduledAt(LocalDateTime.now().plusDays(2 + random.nextInt(5)))
                .rollbackPlan(ROLLBACK_PLANS[random.nextInt(ROLLBACK_PLANS.length)])
                .company(company)
                .build();

        changeRepository.save(change);
        log.info("[ChangeSimulationJob] 신규 변경 생성: {}", change.getTitle());
    }

    private void submitDraftsForApproval() {
        List<Change> drafts = changeRepository.findByStatusCd("DRAFT");

        for (Change change : drafts) {
            // DRAFT 상태에서 1일 이상 경과한 것만 승인요청
            if (change.getCreatedAt() != null
                    && change.getCreatedAt().isAfter(LocalDateTime.now().minusDays(1))) {
                continue;
            }
            change.changeStatus("APPROVAL_REQUESTED");
            log.info("[ChangeSimulationJob] 변경 #{} 승인요청", change.getChangeId());
        }
    }

    private void processApprovals(List<User> users) {
        List<Change> approvalRequested = changeRepository.findByStatusCd("APPROVAL_REQUESTED");

        for (Change change : approvalRequested) {
            List<ChangeApprover> existingApprovers = changeApproverRepository
                    .findByChangeIdOrderByApproveOrderAsc(change.getChangeId());

            // 승인자가 없으면 1-2명 추가
            if (existingApprovers.isEmpty()) {
                int approverCount = 1 + random.nextInt(2);
                for (int i = 0; i < approverCount && i < users.size(); i++) {
                    User approver = users.get(random.nextInt(users.size()));
                    ChangeApprover ca = ChangeApprover.builder()
                            .changeId(change.getChangeId())
                            .userId(approver.getUserId())
                            .approveOrder(i + 1)
                            .build();
                    changeApproverRepository.save(ca);
                }
                log.info("[ChangeSimulationJob] 변경 #{} 승인자 {}명 배정", change.getChangeId(), approverCount);
                continue;
            }

            // 승인자가 있으면 승인/반려 처리
            boolean rejected = random.nextDouble() < REJECTION_RATE;

            if (rejected) {
                ChangeApprover firstApprover = existingApprovers.get(0);
                firstApprover.reject("변경 계획 보완이 필요합니다.");
                change.changeStatus("REJECTED");
                log.info("[ChangeSimulationJob] 변경 #{} 반려", change.getChangeId());
            } else {
                for (ChangeApprover approver : existingApprovers) {
                    if ("PENDING".equals(approver.getApproveStatus())) {
                        approver.approve("변경 계획을 검토하였습니다. 승인합니다.");
                    }
                }
                change.changeStatus("APPROVED");
                log.info("[ChangeSimulationJob] 변경 #{} 승인", change.getChangeId());
            }
        }
    }

    private void startApprovedChanges() {
        List<Change> approved = changeRepository.findByStatusCd("APPROVED");

        for (Change change : approved) {
            if (change.getCreatedAt() != null
                    && change.getCreatedAt().isAfter(LocalDateTime.now().minusDays(1))) {
                continue;
            }
            change.changeStatus("IN_PROGRESS");
            log.info("[ChangeSimulationJob] 변경 #{} 시작", change.getChangeId());
        }
    }

    private void completeInProgressChanges() {
        List<Change> inProgress = changeRepository.findByStatusCd("IN_PROGRESS");

        for (Change change : inProgress) {
            if (change.getCreatedAt() != null
                    && change.getCreatedAt().isAfter(LocalDateTime.now().minusDays(1))) {
                continue;
            }
            change.changeStatus("COMPLETED");
            log.info("[ChangeSimulationJob] 변경 #{} 완료", change.getChangeId());
        }
    }

    private void closeCompletedChanges() {
        List<Change> completed = changeRepository.findByStatusCd("COMPLETED");

        for (Change change : completed) {
            if (change.getCreatedAt() != null
                    && change.getCreatedAt().isAfter(LocalDateTime.now().minusDays(1))) {
                continue;
            }
            change.changeStatus("CLOSED");
            log.info("[ChangeSimulationJob] 변경 #{} 마감", change.getChangeId());
        }
    }
}
