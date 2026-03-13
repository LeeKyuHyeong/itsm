package com.itsm.batch.job.inspection;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.domain.inspection.Inspection;
import com.itsm.core.domain.inspection.InspectionItem;
import com.itsm.core.domain.inspection.InspectionResult;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.incident.IncidentRepository;
import com.itsm.core.repository.inspection.InspectionItemRepository;
import com.itsm.core.repository.inspection.InspectionRepository;
import com.itsm.core.repository.inspection.InspectionResultRepository;
import com.itsm.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InspectionSimulationJob {

    private static final double ABNORMAL_RATE = 0.10;

    private final InspectionRepository inspectionRepository;
    private final InspectionItemRepository inspectionItemRepository;
    private final InspectionResultRepository inspectionResultRepository;
    private final IncidentRepository incidentRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    private final Random random = new Random();

    private static final Map<String, String[]> INSPECTION_TYPE_ITEMS = new LinkedHashMap<>();

    static {
        INSPECTION_TYPE_ITEMS.put("SERVER", new String[]{
                "CPU 사용률 확인", "메모리 사용률 확인", "디스크 사용률 확인",
                "프로세스 상태 확인", "로그 에러 확인", "백업 상태 확인"
        });
        INSPECTION_TYPE_ITEMS.put("NETWORK", new String[]{
                "스위치 포트 상태", "대역폭 사용률", "패킷 손실률",
                "라우팅 테이블 확인", "방화벽 룰 확인"
        });
        INSPECTION_TYPE_ITEMS.put("SECURITY", new String[]{
                "백신 업데이트 상태", "패치 적용 현황", "비인가 접근 로그",
                "계정 잠금 현황", "인증서 만료일 확인"
        });
        INSPECTION_TYPE_ITEMS.put("STORAGE", new String[]{
                "디스크 용량 확인", "RAID 상태 확인", "I/O 성능 확인",
                "백업 복구 테스트", "스냅샷 정리"
        });
        INSPECTION_TYPE_ITEMS.put("UPS", new String[]{
                "배터리 상태 확인", "출력 전압 확인", "부하율 확인",
                "자동 절체 테스트"
        });
    }

    private static final Map<String, String[]> INSPECTION_TYPE_TITLES = new LinkedHashMap<>();

    static {
        INSPECTION_TYPE_TITLES.put("SERVER", new String[]{"서버 점검", "서버 정기 점검"});
        INSPECTION_TYPE_TITLES.put("NETWORK", new String[]{"네트워크 점검", "네트워크 정기 점검"});
        INSPECTION_TYPE_TITLES.put("SECURITY", new String[]{"보안 점검", "보안 정기 점검"});
        INSPECTION_TYPE_TITLES.put("STORAGE", new String[]{"스토리지 점검", "스토리지 정기 점검"});
        INSPECTION_TYPE_TITLES.put("UPS", new String[]{"UPS/전원 점검", "전원 설비 정기 점검"});
    }

    private static final String[][] NORMAL_VALUES = {
            {"정상 (CPU 45%)", "정상 (CPU 32%)", "정상 (CPU 58%)"},
            {"정상 (메모리 62%)", "정상 (메모리 55%)", "정상 (메모리 70%)"},
            {"정상 (디스크 73%)", "정상 (디스크 65%)", "정상 (디스크 48%)"},
            {"정상", "정상 - 이상 없음", "양호"},
    };

    private static final String[] ABNORMAL_VALUES = {
            "비정상 (CPU 95%)", "경고 (디스크 92%)", "비정상 (백업 실패)",
            "비정상 (메모리 96%)", "경고 (패킷 손실 5%)", "비정상 (인증서 만료 임박)"
    };

    public void execute() {
        log.info("[InspectionSimulationJob] 시작");

        List<Company> companies = companyRepository.findAll();
        List<User> users = userRepository.findAll();

        if (companies.isEmpty() || users.isEmpty()) {
            log.info("[InspectionSimulationJob] 회사 또는 사용자 데이터 없음 - 스킵");
            return;
        }

        createScheduledInspections(companies, users);
        startScheduledInspections();
        completeInProgressInspections();
        closeCompletedInspections(companies);

        log.info("[InspectionSimulationJob] 완료");
    }

    private void createScheduledInspections(List<Company> companies, List<User> users) {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        List<Inspection> existing = inspectionRepository
                .findByStatusCdAndScheduledAtBetween("SCHEDULED", today, nextWeek);

        if (!existing.isEmpty()) {
            log.info("[InspectionSimulationJob] 이미 예정된 점검 {}건 존재 - 생성 스킵", existing.size());
            return;
        }

        // 주간 점검 2-3개, 월간 점검 1개 생성
        List<String> types = new ArrayList<>(INSPECTION_TYPE_ITEMS.keySet());
        int inspectionCount = 2 + random.nextInt(2);

        for (int i = 0; i < inspectionCount; i++) {
            String type = types.get(random.nextInt(types.size()));
            Company company = companies.get(random.nextInt(companies.size()));
            User manager = users.get(random.nextInt(users.size()));

            String[] titles = INSPECTION_TYPE_TITLES.get(type);
            String title = titles[random.nextInt(titles.length)];

            LocalDate scheduledDate = today.plusDays(1 + random.nextInt(6));

            Inspection inspection = Inspection.builder()
                    .title(title)
                    .inspectionTypeCd(type)
                    .scheduledAt(scheduledDate)
                    .company(company)
                    .managerId(manager.getUserId())
                    .description(title + " - 자동 생성된 정기 점검입니다.")
                    .build();

            inspectionRepository.save(inspection);
            log.info("[InspectionSimulationJob] 점검 생성: {} (예정일: {})", title, scheduledDate);
        }
    }

    private void startScheduledInspections() {
        LocalDate today = LocalDate.now();
        List<Inspection> scheduled = inspectionRepository
                .findByStatusCdAndScheduledAtBefore("SCHEDULED", today.plusDays(1));

        for (Inspection inspection : scheduled) {
            if (!inspection.getScheduledAt().isAfter(today)) {
                inspection.changeStatus("IN_PROGRESS");
                log.info("[InspectionSimulationJob] 점검 #{} 시작", inspection.getInspectionId());
            }
        }
    }

    private void completeInProgressInspections() {
        List<Inspection> inProgress = inspectionRepository.findByStatusCd("IN_PROGRESS");

        for (Inspection inspection : inProgress) {
            Long inspectionId = inspection.getInspectionId();
            String typeCd = inspection.getInspectionTypeCd();

            // 점검 항목이 없으면 생성
            long itemCount = inspectionItemRepository.countByInspectionId(inspectionId);
            if (itemCount == 0) {
                createInspectionItems(inspectionId, typeCd);
            }

            // 결과가 없으면 생성
            long resultCount = inspectionResultRepository.countByInspectionId(inspectionId);
            if (resultCount == 0) {
                List<InspectionItem> items = inspectionItemRepository
                        .findByInspectionIdOrderBySortOrderAsc(inspectionId);
                createInspectionResults(inspectionId, items);
            }

            inspection.changeStatus("COMPLETED");
            log.info("[InspectionSimulationJob] 점검 #{} 완료", inspectionId);
        }
    }

    private void createInspectionItems(Long inspectionId, String typeCd) {
        String[] itemNames = INSPECTION_TYPE_ITEMS.getOrDefault(typeCd,
                INSPECTION_TYPE_ITEMS.get("SERVER"));

        for (int i = 0; i < itemNames.length; i++) {
            InspectionItem item = InspectionItem.builder()
                    .inspectionId(inspectionId)
                    .itemNm(itemNames[i])
                    .sortOrder(i + 1)
                    .isRequired("Y")
                    .build();
            inspectionItemRepository.save(item);
        }
        log.info("[InspectionSimulationJob] 점검 #{} 항목 {}건 생성", inspectionId, itemNames.length);
    }

    private void createInspectionResults(Long inspectionId, List<InspectionItem> items) {
        for (InspectionItem item : items) {
            boolean isAbnormal = random.nextDouble() < ABNORMAL_RATE;

            String resultValue;
            String isNormal;

            if (isAbnormal) {
                resultValue = ABNORMAL_VALUES[random.nextInt(ABNORMAL_VALUES.length)];
                isNormal = "N";
            } else {
                String[] normalGroup = NORMAL_VALUES[random.nextInt(NORMAL_VALUES.length)];
                resultValue = normalGroup[random.nextInt(normalGroup.length)];
                isNormal = "Y";
            }

            InspectionResult result = InspectionResult.builder()
                    .inspectionId(inspectionId)
                    .itemId(item.getItemId())
                    .resultValue(resultValue)
                    .isNormal(isNormal)
                    .remark(isAbnormal ? "점검 결과 이상 발견 - 조치 필요" : null)
                    .build();
            inspectionResultRepository.save(result);
        }
    }

    private void closeCompletedInspections(List<Company> companies) {
        List<Inspection> completed = inspectionRepository.findByStatusCd("COMPLETED");

        for (Inspection inspection : completed) {
            Long inspectionId = inspection.getInspectionId();

            // 비정상 결과 확인 후 장애 생성
            List<InspectionResult> results = inspectionResultRepository.findByInspectionId(inspectionId);
            List<InspectionResult> abnormals = results.stream()
                    .filter(r -> "N".equals(r.getIsNormal()))
                    .collect(Collectors.toList());

            if (!abnormals.isEmpty()) {
                createIncidentsForAbnormals(inspection, abnormals);
            }

            inspection.changeStatus("CLOSED");
            log.info("[InspectionSimulationJob] 점검 #{} 마감 (비정상 {}건)", inspectionId, abnormals.size());
        }
    }

    private void createIncidentsForAbnormals(Inspection inspection, List<InspectionResult> abnormals) {
        List<InspectionItem> items = inspectionItemRepository
                .findByInspectionIdOrderBySortOrderAsc(inspection.getInspectionId());
        Map<Long, String> itemNameMap = items.stream()
                .collect(Collectors.toMap(InspectionItem::getItemId, InspectionItem::getItemNm));

        for (InspectionResult abnormal : abnormals) {
            String itemName = itemNameMap.getOrDefault(abnormal.getItemId(), "점검 항목");

            Incident incident = Incident.builder()
                    .title(String.format("[점검발견] %s - %s 이상", inspection.getTitle(), itemName))
                    .content(String.format("점검 '%s'에서 '%s' 항목 이상 발견: %s",
                            inspection.getTitle(), itemName, abnormal.getResultValue()))
                    .incidentTypeCd("INSPECTION")
                    .priorityCd("HIGH")
                    .occurredAt(LocalDateTime.now())
                    .company(inspection.getCompany())
                    .build();

            incidentRepository.save(incident);
            log.info("[InspectionSimulationJob] 점검 #{} 비정상 → 장애 생성: {}",
                    inspection.getInspectionId(), itemName);
        }
    }
}
