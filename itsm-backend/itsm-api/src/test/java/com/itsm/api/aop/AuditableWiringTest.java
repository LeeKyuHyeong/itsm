package com.itsm.api.aop;

import com.itsm.api.controller.asset.AssetHwController;
import com.itsm.api.controller.asset.AssetOaController;
import com.itsm.api.controller.asset.AssetSwController;
import com.itsm.api.controller.change.ChangeController;
import com.itsm.api.controller.company.CompanyController;
import com.itsm.api.controller.incident.IncidentController;
import com.itsm.api.controller.inspection.InspectionController;
import com.itsm.api.controller.servicerequest.ServiceRequestController;
import com.itsm.api.controller.user.UserController;
import com.itsm.core.domain.common.AuditLog;
import com.itsm.core.dto.ApiResponse;
import com.itsm.core.repository.common.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * 감사 로그 AOP 배선 검증 (2026-09-16 전수조사 P5).
 * 2026-09-16 이전에는 @Auditable 을 붙인 메서드가 0개라 AuditLogAspect 는 한 번도 실행되지 않았고
 * tb_audit_log 는 비어 있었다. 이 테스트는 (1) 실제 스프링 컨텍스트에서 어스펙트가 발동하는지,
 * (2) 도메인 컨트롤러의 변경 엔드포인트에 어노테이션이 실제로 붙어 있는지를 고정한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(AuditableWiringTest.ProbeConfig.class)
class AuditableWiringTest {

    /** 컨텍스트 안에서 @Auditable 을 가진 최소 빈 */
    public static class AuditProbe {
        public record ProbeDto(Long probeId) {
            public Long getProbeId() { return probeId; }
        }

        @Auditable(actionType = "PROBE", targetType = "PROBE")
        public ApiResponse<ProbeDto> run(Long id) {
            return ApiResponse.success(new ProbeDto(id));
        }

        @Auditable(actionType = "PROBE_CREATE", targetType = "PROBE")
        public ApiResponse<ProbeDto> create() {
            return ApiResponse.success(new ProbeDto(4242L));
        }
    }

    @TestConfiguration
    static class ProbeConfig {
        @Bean
        AuditProbe auditProbe() {
            return new AuditProbe();
        }
    }

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditProbe auditProbe;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("@Auditable 메서드를 호출하면 어스펙트가 실행되어 tb_audit_log 저장이 일어난다 (인자에서 targetId)")
    void aspectFiresOnAnnotatedMethod() {
        assertThat(AopUtils.isAopProxy(auditProbe)).as("프로브 빈이 AOP 프록시여야 한다").isTrue();

        auditProbe.run(42L);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getActionType()).isEqualTo("PROBE");
        assertThat(captor.getValue().getTargetType()).isEqualTo("PROBE");
        assertThat(captor.getValue().getTargetId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("인자가 없는 create 형 메서드는 ApiResponse 반환값에서 targetId 를 얻는다")
    void aspectExtractsTargetIdFromApiResponse() {
        auditProbe.create();

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getTargetId()).isEqualTo(4242L);
    }

    static Stream<Arguments> controllersWithMinimumAuditedMethods() {
        return Stream.of(
                Arguments.of(IncidentController.class, 8),
                Arguments.of(ServiceRequestController.class, 9),
                Arguments.of(ChangeController.class, 6),
                Arguments.of(InspectionController.class, 6),
                Arguments.of(AssetHwController.class, 5),
                Arguments.of(AssetSwController.class, 3),
                Arguments.of(AssetOaController.class, 3),
                Arguments.of(UserController.class, 5),
                Arguments.of(CompanyController.class, 4)
        );
    }

    @ParameterizedTest(name = "{0} 에 @Auditable 이 {1}개 이상")
    @MethodSource("controllersWithMinimumAuditedMethods")
    @DisplayName("도메인 컨트롤러의 변경 엔드포인트에 @Auditable 이 붙어 있고 빈은 AOP 프록시다")
    void controllersAreAudited(Class<?> controllerClass, int minimum) {
        long annotated = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Auditable.class))
                .count();
        assertThat(annotated).as("%s @Auditable 수", controllerClass.getSimpleName())
                .isGreaterThanOrEqualTo(minimum);

        Object bean = applicationContext.getBean(controllerClass);
        assertThat(AopUtils.isAopProxy(bean))
                .as("%s 는 어스펙트가 감쌀 수 있도록 AOP 프록시여야 한다", controllerClass.getSimpleName())
                .isTrue();
    }

    @Test
    @DisplayName("create/update/changeStatus 는 모든 도메인에서 빠짐없이 감사 대상이다")
    void coreMutationsAreAuditedEverywhere() {
        Class<?>[] controllers = {
                IncidentController.class, ServiceRequestController.class, ChangeController.class,
                InspectionController.class, AssetHwController.class, AssetSwController.class, AssetOaController.class
        };
        for (Class<?> c : controllers) {
            for (String name : new String[]{"create", "update", "changeStatus"}) {
                Method m = Arrays.stream(c.getDeclaredMethods())
                        .filter(x -> x.getName().equals(name)).findFirst().orElse(null);
                assertThat(m).as("%s.%s 존재", c.getSimpleName(), name).isNotNull();
                assertThat(m.isAnnotationPresent(Auditable.class))
                        .as("%s.%s 에 @Auditable", c.getSimpleName(), name).isTrue();
            }
        }
    }
}
