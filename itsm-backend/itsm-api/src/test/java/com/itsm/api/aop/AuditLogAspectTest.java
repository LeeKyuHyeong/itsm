package com.itsm.api.aop;

import com.itsm.core.domain.common.AuditLog;
import com.itsm.core.dto.ApiResponse;
import com.itsm.core.repository.common.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuditLogAspectTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogAspect auditLogAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Auditable auditable;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private com.itsm.api.security.ClientIpResolver clientIpResolver;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("logAudit - 올바른 actionType과 targetType으로 감사 로그를 저장한다")
    void logAudit_savesAuditLogWithCorrectActionTypeAndTargetType() {
        // given
        given(auditable.actionType()).willReturn("CREATE");
        given(auditable.targetType()).willReturn("INCIDENT");
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(1L);
        given(requestAttributes.getRequest()).willReturn(httpServletRequest);
        given(clientIpResolver.resolve(httpServletRequest)).willReturn("127.0.0.1");
        given(joinPoint.getArgs()).willReturn(new Object[]{});

        // when
        auditLogAspect.logAudit(joinPoint, auditable, null);

        // then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertThat(savedLog.getActionType()).isEqualTo("CREATE");
        assertThat(savedLog.getTargetType()).isEqualTo("INCIDENT");
        assertThat(savedLog.getUserId()).isEqualTo(1L);
        assertThat(savedLog.getIpAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("logAudit - 메서드 인자에서 targetId를 추출한다")
    void logAudit_extractsTargetIdFromMethodArguments() {
        // given
        given(auditable.actionType()).willReturn("UPDATE");
        given(auditable.targetType()).willReturn("SR");
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(2L);
        given(requestAttributes.getRequest()).willReturn(httpServletRequest);
        given(clientIpResolver.resolve(httpServletRequest)).willReturn("192.168.1.1");
        given(joinPoint.getArgs()).willReturn(new Object[]{100L, "someOtherArg"});

        // when
        auditLogAspect.logAudit(joinPoint, auditable, null);

        // then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertThat(savedLog.getTargetId()).isEqualTo(100L);
        assertThat(savedLog.getActionType()).isEqualTo("UPDATE");
        assertThat(savedLog.getTargetType()).isEqualTo("SR");
        assertThat(savedLog.getUserId()).isEqualTo(2L);
        assertThat(savedLog.getIpAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    @DisplayName("logAudit - 인증 정보가 없어도 정상적으로 처리한다")
    void logAudit_handlesNullAuthenticationGracefully() {
        // given
        given(auditable.actionType()).willReturn("DELETE");
        given(auditable.targetType()).willReturn("CHANGE");
        given(securityContext.getAuthentication()).willReturn(null);
        given(requestAttributes.getRequest()).willReturn(httpServletRequest);
        given(clientIpResolver.resolve(httpServletRequest)).willReturn("10.0.0.1");
        given(joinPoint.getArgs()).willReturn(new Object[]{});

        // when
        auditLogAspect.logAudit(joinPoint, auditable, null);

        // then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertThat(savedLog.getUserId()).isNull();
        assertThat(savedLog.getActionType()).isEqualTo("DELETE");
        assertThat(savedLog.getTargetType()).isEqualTo("CHANGE");
        assertThat(savedLog.getIpAddress()).isEqualTo("10.0.0.1");
    }

    // ── 컨트롤러 반환값(ApiResponse<DTO>)에서 targetId 추출 — 2026-09-16 전수조사 P5 ──
    // 컨트롤러 create() 는 Long 인자가 없고 ApiResponse 로 감싸 반환하므로, 래퍼를 벗기고
    // targetType 에 맞는 *Id getter 를 골라야 한다 (getMethods() 순서는 비결정적 → 휴리스틱 필요).

    /** IncidentResponse 를 흉내낸 DTO: companyId 가 incidentId 보다 먼저 선언돼 있어도 incidentId 를 골라야 한다 */
    public static class FakeIncidentDto {
        public Long getCompanyId() { return 7L; }
        public Long getIncidentId() { return 55L; }
        public Long getMainManagerId() { return 9L; }
    }

    /** SrResponse 를 흉내낸 DTO: SERVICE_REQUEST 의 정확한 getter(getServiceRequestId) 가 없고 getRequestId 만 있다 */
    public static class FakeSrDto {
        public Long getCompanyId() { return 7L; }
        public Long getRequestId() { return 66L; }
    }

    /** DepartmentResponse: 토큰 매칭이 불가능(DEPARTMENT ↔ deptId) → 어노테이션의 targetIdProperty 로 지정 */
    public static class FakeDeptDto {
        public Long getCompanyId() { return 7L; }
        public Long getDeptId() { return 77L; }
    }

    private void stubCommon(String targetType, String targetIdProperty) {
        given(auditable.actionType()).willReturn("CREATE");
        given(auditable.targetType()).willReturn(targetType);
        // 인자에서 targetId 를 얻는 경로에서는 호출되지 않으므로 strict-stub 예외를 피하려고 lenient
        lenient().when(auditable.targetIdProperty()).thenReturn(targetIdProperty);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(1L);
        given(requestAttributes.getRequest()).willReturn(httpServletRequest);
        given(clientIpResolver.resolve(httpServletRequest)).willReturn("127.0.0.1");
        // targetIdProperty 로 먼저 찾으면 인자를 보지 않으므로 lenient
        lenient().when(joinPoint.getArgs()).thenReturn(new Object[]{});
    }

    private Long savedTargetId() {
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        return captor.getValue().getTargetId();
    }

    @Test
    @DisplayName("logAudit - ApiResponse 로 감싼 반환값을 벗겨 targetType 과 일치하는 getter(getIncidentId) 를 고른다")
    void logAudit_unwrapsApiResponseAndPicksGetterMatchingTargetType() {
        stubCommon("INCIDENT", "");

        auditLogAspect.logAudit(joinPoint, auditable, ApiResponse.success(new FakeIncidentDto()));

        assertThat(savedTargetId()).isEqualTo(55L);
    }

    @Test
    @DisplayName("logAudit - 정확한 getter 가 없으면 targetType 토큰(REQUEST)을 포함하는 getter(getRequestId) 를 고른다")
    void logAudit_fallsBackToGetterContainingTargetTypeToken() {
        stubCommon("SERVICE_REQUEST", "");

        auditLogAspect.logAudit(joinPoint, auditable, ApiResponse.success(new FakeSrDto()));

        assertThat(savedTargetId()).isEqualTo(66L);
    }

    @Test
    @DisplayName("logAudit - targetIdProperty 가 지정되면 그 프로퍼티의 getter 를 우선 사용한다")
    void logAudit_prefersExplicitTargetIdProperty() {
        stubCommon("DEPARTMENT", "deptId");

        auditLogAspect.logAudit(joinPoint, auditable, ApiResponse.success(new FakeDeptDto()));

        assertThat(savedTargetId()).isEqualTo(77L);
    }

    @Test
    @DisplayName("logAudit - Long 인자가 있으면 반환값보다 인자를 우선한다 (update/{id} 패턴)")
    void logAudit_argumentWinsOverResult() {
        stubCommon("INCIDENT", "");
        given(joinPoint.getArgs()).willReturn(new Object[]{100L});

        auditLogAspect.logAudit(joinPoint, auditable, ApiResponse.success(new FakeIncidentDto()));

        assertThat(savedTargetId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("logAudit - ApiResponse<Void>(data=null) 이고 인자도 없으면 targetId 는 null")
    void logAudit_nullTargetIdWhenNothingToExtract() {
        stubCommon("INCIDENT", "");

        auditLogAspect.logAudit(joinPoint, auditable, ApiResponse.success());

        assertThat(savedTargetId()).isNull();
    }
}
