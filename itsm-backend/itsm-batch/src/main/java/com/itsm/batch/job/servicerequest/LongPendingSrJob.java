package com.itsm.batch.job.servicerequest;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.servicerequest.ServiceRequest;
import com.itsm.core.repository.servicerequest.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LongPendingSrJob {

    private static final Long ADMIN_USER_ID = 1L;

    private final ServiceRequestRepository serviceRequestRepository;
    private final NotificationService notificationService;

    public void execute() {
        log.info("[LongPendingSrJob] 시작");
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);

        List<ServiceRequest> requests = serviceRequestRepository
                .findByStatusCdAndUpdatedAtBefore("PENDING_COMPLETE", twoDaysAgo);

        for (ServiceRequest sr : requests) {
            notificationService.sendNotification(
                    ADMIN_USER_ID,
                    "LONG_PENDING_SR",
                    String.format("[장기 미처리 SR] SR #%d 완료대기 2일 초과", sr.getRequestId()),
                    String.format("서비스요청 '%s'이(가) 완료대기 상태로 2일 이상 경과하였습니다.",
                            sr.getTitle()),
                    "SERVICE_REQUEST",
                    sr.getRequestId()
            );
        }
        log.info("[LongPendingSrJob] 완료 - {}건 알림 발송", requests.size());
    }
}
