package com.itsm.batch.job.incident;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.domain.incident.IncidentAssignee;
import com.itsm.core.domain.incident.IncidentComment;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.incident.IncidentAssigneeRepository;
import com.itsm.core.repository.incident.IncidentCommentRepository;
import com.itsm.core.repository.incident.IncidentRepository;
import com.itsm.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentSimulationJob {

    private final IncidentRepository incidentRepository;
    private final IncidentCommentRepository incidentCommentRepository;
    private final IncidentAssigneeRepository incidentAssigneeRepository;
    private final AssetHwRepository assetHwRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    private static final Random RANDOM = new Random();
    private static final int DAILY_MIN = 2;
    private static final int DAILY_MAX = 5;

    // ── 장애 유형별 템플릿 ──

    private static final String[][] SERVER_DOWN_TEMPLATES = {
            {"[긴급] 웹 서버 응답 없음",
                    "웹 서버(WEB-SRV-01)에서 응답이 없습니다. ping 응답 없음, SSH 접속 불가. 서비스 접속 장애 발생 중입니다. 즉시 확인 요청합니다."},
            {"[장애] DB 서버 연결 불가",
                    "DB 서버(DB-SRV-02)와의 연결이 끊어졌습니다. 애플리케이션에서 Connection Refused 오류 다수 발생. 데이터 서비스 중단 상태입니다."},
            {"[긴급] AP 서버 다운",
                    "AP 서버(AP-SRV-03)가 다운되었습니다. 프로세스 비정상 종료 확인. 사용자 서비스 접속 불가 상태입니다."},
            {"[장애] 배치 서버 장애",
                    "배치 서버(BAT-SRV-01)에서 프로세스가 응답하지 않습니다. 야간 배치 작업이 중단된 상태입니다."},
            {"[긴급] 메일 서버 서비스 중단",
                    "메일 서버(MAIL-SRV-01)의 SMTP/IMAP 서비스가 중단되었습니다. 전사 메일 송수신 불가 상태입니다."},
            {"[장애] WAS 인스턴스 다운",
                    "WAS 인스턴스(WAS-02)가 비정상 종료되었습니다. Out of Memory 에러 로그 확인. 자동 재시작 실패."}
    };

    private static final String[][] NETWORK_FAULT_TEMPLATES = {
            {"[장애] 코어 스위치 포트 장애",
                    "코어 스위치(NET-SW-001) 포트 Gi0/24에서 CRC 에러가 다수 발생하고 있습니다. 해당 포트에 연결된 서버군 네트워크 불안정."},
            {"[장애] VPN 연결 불안정",
                    "VPN 게이트웨이에서 간헐적 연결 끊김 현상 발생. 재택근무 사용자 다수 불편 접수. 터널 재설정 필요 판단."},
            {"[장애] 네트워크 대역폭 포화",
                    "10번 VLAN 대역폭 사용률 95% 초과. 패킷 드롭 발생 중. 트래픽 원인 분석 필요합니다."},
            {"[장애] DNS 서버 응답 지연",
                    "내부 DNS 서버 응답 시간이 5초 이상으로 지연되고 있습니다. 도메인 이름 해석 실패 건 다수 발생."},
            {"[장애] 방화벽 정책 이상",
                    "방화벽(NET-FW-001) 정책 변경 후 특정 서비스 포트 접근 불가. 긴급 정책 롤백 필요합니다."}
    };

    private static final String[][] DB_PERF_TEMPLATES = {
            {"[경고] DB 쿼리 응답 지연",
                    "운영 DB(Oracle)에서 특정 쿼리 응답 시간이 10초 이상으로 지연되고 있습니다. 풀스캔 발생 의심. 실행 계획 확인 필요."},
            {"[경고] DB Connection Pool 부족",
                    "WAS Connection Pool 사용률 90% 초과. 신규 연결 요청 대기 중. Pool 확장 또는 비정상 세션 정리 필요합니다."},
            {"[경고] DB 테이블스페이스 임계치 초과",
                    "USERS 테이블스페이스 사용률 92%. 자동 확장 한계 도달 예상. 데이터 아카이빙 또는 확장 필요."},
            {"[경고] DB 락 경합 발생",
                    "운영 DB에서 Row Lock 대기 세션이 50건 이상 발생하고 있습니다. 특정 배치 프로세스 원인 확인 필요."},
            {"[경고] DB 복제 지연",
                    "DR DB 복제 지연 시간이 30분 이상입니다. 네트워크 대역폭 또는 원본 DB 부하 확인이 필요합니다."}
    };

    private static final String[][] STORAGE_TEMPLATES = {
            {"[경고] 스토리지 사용률 90% 초과",
                    "SAN 스토리지(STG-SAN-001) 전체 사용률 91%. 로그 데이터 및 임시 파일 정리 필요. 용량 증설 검토 요청."},
            {"[긴급] 로그 파티션 용량 부족",
                    "/var/log 파티션 사용률 98%. 서비스 로그 기록 실패 임박. 즉시 로그 로테이션 및 정리 필요합니다."},
            {"[경고] 백업 스토리지 용량 경고",
                    "백업 스토리지 잔여 용량 500GB. 주간 풀백업 수행 불가 예상. 보존 정책 검토 필요합니다."},
            {"[긴급] 디스크 I/O 지연",
                    "서버(SRV-RACK-005) 디스크 I/O 대기 시간 급증. await 200ms 이상. 디스크 상태 점검 필요합니다."},
            {"[경고] NAS 접근 지연",
                    "NAS(STG-NAS-001) 파일 접근 응답 시간 5초 이상. 동시 접속 사용자 수 확인 및 성능 튜닝 필요."}
    };

    private static final String[][] SECURITY_TEMPLATES = {
            {"[보안] 비정상 로그인 시도 감지",
                    "관리자 계정(admin)에 대해 10분 내 50회 이상 로그인 실패 감지. 브루트포스 공격 의심. 소스 IP: 외부 대역. 계정 잠금 조치 완료."},
            {"[보안] 악성코드 탐지",
                    "사용자 PC(PC-0042)에서 악성코드(Trojan.GenericKD) 탐지. 격리 조치 완료. 추가 확산 여부 확인 필요합니다."},
            {"[보안] 비인가 포트 스캔 감지",
                    "IDS에서 내부 IP(10.10.2.55)로부터의 대규모 포트 스캔 활동 감지. 의도적 보안 점검인지 확인 필요합니다."},
            {"[보안] SSL 인증서 만료 임박",
                    "외부 서비스 도메인(service.company.co.kr) SSL 인증서가 7일 후 만료됩니다. 갱신 절차 진행 필요."},
            {"[보안] 개인정보 유출 의심",
                    "DLP 시스템에서 대량 개인정보 포함 파일 외부 전송 시도 감지. 차단 완료. 해당 사용자 확인 필요합니다."}
    };

    // ── 장애 유형 코드 ──
    private static final String[] INCIDENT_TYPE_CODES = {
            "SERVER_DOWN", "NETWORK_FAULT", "DB_PERFORMANCE", "STORAGE_CAPACITY", "SECURITY_EVENT"
    };

    // ── 우선순위 ──
    private static final String[] PRIORITIES = {"CRITICAL", "HIGH", "MEDIUM", "LOW"};
    private static final int[] PRIORITY_WEIGHTS = {10, 25, 45, 20}; // 가중치 합 = 100

    // ── SLA 시간 (시간 단위) ──
    private static final Map<String, Integer> SLA_HOURS = Map.of(
            "CRITICAL", 4,
            "HIGH", 8,
            "MEDIUM", 24,
            "LOW", 72
    );

    // ── 처리 내용 템플릿 ──
    private static final String[] PROCESS_CONTENTS = {
            "원인 분석 완료. 로그 확인 결과 메모리 부족으로 인한 서비스 중단으로 확인되었습니다. 메모리 증설 후 서비스 재시작 완료.",
            "장비 점검 완료. 원인: 디스크 용량 부족으로 인한 서비스 중단. 불필요 로그 정리 및 디스크 확장 조치 완료.",
            "설정 변경 적용 완료. 네트워크 설정 오류 수정 후 정상 동작 확인. 모니터링 결과 안정적.",
            "벤더 지원 요청 후 패치 적용 완료. 펌웨어 업그레이드로 문제 해결. 재발 방지를 위해 모니터링 강화.",
            "하드웨어 교체 완료. 결함 디스크 교체 후 RAID 재구축 진행. 데이터 무결성 확인 완료.",
            "소프트웨어 패치 적용 완료. 보안 취약점 패치 및 애플리케이션 재시작. 서비스 정상 동작 확인.",
            "네트워크 장비 재부팅으로 일시적 해결. 근본 원인은 펌웨어 버그로 판단되어 업그레이드 일정 수립.",
            "DB 튜닝 완료. 문제 쿼리 인덱스 추가 및 실행 계획 최적화. 응답 시간 1초 이내로 개선.",
            "보안 정책 수정 완료. 오탐으로 확인된 규칙 조정 및 화이트리스트 업데이트. 정상 트래픽 허용 확인.",
            "용량 확장 완료. 스토리지 볼륨 500GB 추가 할당. 모니터링 임계치 조정 완료."
    };

    // ── 댓글 템플릿 ──
    private static final String[] RECEIVED_COMMENTS = {
            "장애 접수 확인. 담당자 배정 예정입니다.",
            "장애 접수 완료. 현황 파악 중입니다.",
            "해당 장애 접수되었습니다. 담당팀 확인 중."
    };

    private static final String[] ASSIGN_COMMENTS = {
            "담당자 배정 완료. 현장 확인 진행합니다.",
            "담당자 배정 완료. 원격 접속하여 상태 확인 중입니다.",
            "배정 완료. 원인 분석 착수합니다."
    };

    private static final String[] PROGRESS_COMMENTS = {
            "원인 파악 완료. 조치 진행합니다.",
            "현장 확인 중입니다. 예상 소요 시간 1시간.",
            "로그 분석 중. 원인 후보 3건 확인.",
            "벤더 기술지원 요청 완료. 원격 접속 대기 중."
    };

    private static final String[] COMPLETE_COMMENTS = {
            "조치 완료. 정상 동작 확인 중입니다.",
            "처리 완료되었습니다. 모니터링 결과 정상입니다.",
            "복구 완료. 서비스 정상 확인. 재발 방지 조치 검토 중.",
            "문제 해결 완료. 관련 문서 업데이트 예정입니다."
    };

    private static final String[] CLOSE_COMMENTS = {
            "최종 확인 완료. 장애 종료 처리합니다.",
            "모니터링 기간 경과 후 재발 없음 확인. 종료합니다.",
            "장애 종료. 사후 분석 보고서 작성 예정."
    };

    public void execute() {
        log.info("[IncidentSimulationJob] 시작");

        List<Company> companies = companyRepository.findAll();
        List<User> users = userRepository.findAll();

        if (companies.isEmpty() || users.isEmpty()) {
            log.warn("회사 또는 사용자 데이터가 없어 장애 시뮬레이션을 건너뜁니다.");
            return;
        }

        // Phase 1: 신규 장애 생성
        int created = createNewIncidents(companies, users);

        // Phase 2: 미배정 장애 자동 배정 (RECEIVED → IN_PROGRESS)
        int assigned = processUnassignedIncidents(users);

        // Phase 3: 진행중 장애 자동 완료 (IN_PROGRESS → COMPLETED)
        int completed = processInProgressIncidents();

        // Phase 4: 완료 장애 자동 종료 (COMPLETED → CLOSED)
        int closed = processCompletedIncidents();

        log.info("[IncidentSimulationJob] 완료 - 신규: {}건, 배정: {}건, 완료: {}건, 종료: {}건",
                created, assigned, completed, closed);
    }

    private int createNewIncidents(List<Company> companies, List<User> users) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        long todayCount = incidentRepository.countByCreatedAtBetween(todayStart, todayEnd);
        int dailyTarget = RANDOM.nextInt(DAILY_MAX - DAILY_MIN + 1) + DAILY_MIN;

        if (todayCount >= dailyTarget) {
            log.info("[IncidentSimulationJob] 오늘 할당량 충족 (현재: {}건, 목표: {}건)", todayCount, dailyTarget);
            return 0;
        }

        int toCreate = (int) (dailyTarget - todayCount);
        int created = 0;

        for (int i = 0; i < toCreate; i++) {
            String incidentTypeCd = pickRandom(INCIDENT_TYPE_CODES);
            String[][] templates = getTemplates(incidentTypeCd);
            String[] template = templates[RANDOM.nextInt(templates.length)];

            String priorityCd = pickWeightedPriority();
            LocalDateTime occurredAt = generateOccurredAt();

            // SLA 위반 비율 5-10%
            boolean slaViolation = RANDOM.nextInt(100) < 8;

            Company company = pickRandom(companies);

            Incident incident = Incident.builder()
                    .title(template[0])
                    .content(template[1])
                    .incidentTypeCd(incidentTypeCd)
                    .priorityCd(priorityCd)
                    .occurredAt(occurredAt)
                    .company(company)
                    .build();

            // SLA 데드라인 설정
            int slaHours = SLA_HOURS.getOrDefault(priorityCd, 24);
            if (slaViolation) {
                // SLA 위반: occurredAt을 과거로 설정하여 이미 SLA 초과
                LocalDateTime pastOccurred = LocalDateTime.now().minusHours(slaHours + RANDOM.nextInt(4) + 1);
                incident = Incident.builder()
                        .title(template[0])
                        .content(template[1])
                        .incidentTypeCd(incidentTypeCd)
                        .priorityCd(priorityCd)
                        .occurredAt(pastOccurred)
                        .company(company)
                        .build();
                incident.setSlaDeadline(pastOccurred.plusHours(slaHours));
            } else {
                incident.setSlaDeadline(occurredAt.plusHours(slaHours));
            }

            Incident saved = incidentRepository.save(incident);

            // 접수 댓글 추가
            addComment(saved.getIncidentId(), pickRandom(RECEIVED_COMMENTS), 1L);

            created++;
            log.debug("[IncidentSimulationJob] 장애 생성 - ID: {}, 유형: {}, 우선순위: {}",
                    saved.getIncidentId(), incidentTypeCd, priorityCd);
        }

        return created;
    }

    private int processUnassignedIncidents(List<User> users) {
        List<Incident> receivedIncidents = incidentRepository.findByStatusCd("RECEIVED");
        int assigned = 0;

        for (Incident incident : receivedIncidents) {
            if (incident.getCreatedAt() == null) {
                continue;
            }

            // 접수 후 10~60분 경과했는지 확인
            long minutesSinceCreated = java.time.Duration.between(
                    incident.getCreatedAt(), LocalDateTime.now()).toMinutes();
            int assignDelay = RANDOM.nextInt(51) + 10; // 10~60분

            if (minutesSinceCreated >= assignDelay) {
                User assignee = pickRandom(users);

                incident.assignMainManager(assignee);
                incident.changeStatus("IN_PROGRESS");

                // 배정 기록
                IncidentAssignee incidentAssignee = IncidentAssignee.builder()
                        .incidentId(incident.getIncidentId())
                        .userId(assignee.getUserId())
                        .grantedBy(1L)
                        .build();
                incidentAssigneeRepository.save(incidentAssignee);

                // 배정 댓글
                addComment(incident.getIncidentId(), pickRandom(ASSIGN_COMMENTS), 1L);

                assigned++;
                log.debug("[IncidentSimulationJob] 장애 배정 - ID: {}, 담당자: {}",
                        incident.getIncidentId(), assignee.getUserId());
            }
        }

        return assigned;
    }

    private int processInProgressIncidents() {
        List<Incident> inProgressIncidents = incidentRepository.findByStatusCd("IN_PROGRESS");
        int completed = 0;

        for (Incident incident : inProgressIncidents) {
            if (incident.getCreatedAt() == null) {
                continue;
            }

            long hoursSinceCreated = java.time.Duration.between(
                    incident.getCreatedAt(), LocalDateTime.now()).toHours();

            // 우선순위별 완료 시간 (CRITICAL 빠름, LOW 느림)
            int completeHours = getCompleteHours(incident.getPriorityCd());

            if (hoursSinceCreated >= completeHours) {
                incident.writeProcessContent(pickRandom(PROCESS_CONTENTS));
                incident.changeStatus("COMPLETED");

                // 진행 댓글 + 완료 댓글
                addComment(incident.getIncidentId(), pickRandom(PROGRESS_COMMENTS), 1L);
                addComment(incident.getIncidentId(), pickRandom(COMPLETE_COMMENTS), 1L);

                completed++;
                log.debug("[IncidentSimulationJob] 장애 완료 - ID: {}", incident.getIncidentId());
            }
        }

        return completed;
    }

    private int processCompletedIncidents() {
        List<Incident> completedIncidents = incidentRepository.findByStatusCd("COMPLETED");
        int closed = 0;

        for (Incident incident : completedIncidents) {
            if (incident.getCompletedAt() == null) {
                continue;
            }

            long hoursSinceCompleted = java.time.Duration.between(
                    incident.getCompletedAt(), LocalDateTime.now()).toHours();

            // 완료 후 1~24시간 후 종료
            int closeDelay = RANDOM.nextInt(24) + 1;

            if (hoursSinceCompleted >= closeDelay) {
                incident.changeStatus("CLOSED");

                addComment(incident.getIncidentId(), pickRandom(CLOSE_COMMENTS), 1L);

                closed++;
                log.debug("[IncidentSimulationJob] 장애 종료 - ID: {}", incident.getIncidentId());
            }
        }

        return closed;
    }

    // ── 헬퍼 메서드 ──

    private String[][] getTemplates(String typeCd) {
        return switch (typeCd) {
            case "SERVER_DOWN" -> SERVER_DOWN_TEMPLATES;
            case "NETWORK_FAULT" -> NETWORK_FAULT_TEMPLATES;
            case "DB_PERFORMANCE" -> DB_PERF_TEMPLATES;
            case "STORAGE_CAPACITY" -> STORAGE_TEMPLATES;
            case "SECURITY_EVENT" -> SECURITY_TEMPLATES;
            default -> SERVER_DOWN_TEMPLATES;
        };
    }

    private String pickWeightedPriority() {
        int roll = RANDOM.nextInt(100);
        int cumulative = 0;
        for (int i = 0; i < PRIORITIES.length; i++) {
            cumulative += PRIORITY_WEIGHTS[i];
            if (roll < cumulative) {
                return PRIORITIES[i];
            }
        }
        return "MEDIUM";
    }

    private LocalDateTime generateOccurredAt() {
        // 업무시간(09-18) 가중치 높게
        int hour;
        if (RANDOM.nextInt(100) < 70) {
            // 70% 확률로 업무시간
            hour = RANDOM.nextInt(10) + 9; // 9~18
        } else {
            // 30% 확률로 비업무시간
            hour = RANDOM.nextInt(24);
        }
        int minute = RANDOM.nextInt(60);
        return LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute));
    }

    private int getCompleteHours(String priorityCd) {
        return switch (priorityCd) {
            case "CRITICAL" -> RANDOM.nextInt(2) + 1;  // 1-2시간
            case "HIGH" -> RANDOM.nextInt(3) + 2;      // 2-4시간
            case "MEDIUM" -> RANDOM.nextInt(4) + 3;    // 3-6시간
            case "LOW" -> RANDOM.nextInt(4) + 5;       // 5-8시간
            default -> 4;
        };
    }

    private void addComment(Long incidentId, String content, Long createdBy) {
        IncidentComment comment = IncidentComment.builder()
                .incidentId(incidentId)
                .content(content)
                .createdBy(createdBy)
                .build();
        incidentCommentRepository.save(comment);
    }

    @SuppressWarnings("unchecked")
    private <T> T pickRandom(T[] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    private <T> T pickRandom(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}
