package com.itsm.api.aop;

import com.itsm.core.domain.common.AuditLog;
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
        given(httpServletRequest.getRemoteAddr()).willReturn("127.0.0.1");
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
        given(httpServletRequest.getRemoteAddr()).willReturn("192.168.1.1");
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
        given(httpServletRequest.getRemoteAddr()).willReturn("10.0.0.1");
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
}
