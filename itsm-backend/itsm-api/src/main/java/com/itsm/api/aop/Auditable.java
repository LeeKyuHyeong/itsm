package com.itsm.api.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 감사 로그(tb_audit_log) 자동 적재 대상 표시. AuditLogAspect 가 정상 반환 후 기록한다.
 * <p>
 * 2026-09-16 전수조사 P5 이전에는 이 어노테이션을 붙인 곳이 0개라 어스펙트가 한 번도 실행되지 않았다.
 * 도메인 컨트롤러의 변경 엔드포인트(create/update/changeStatus 등)에 붙이며, AuditableWiringTest 가 누락을 잡는다.
 * <p>
 * targetId 추출 순서: {@link #targetIdProperty()} 가 지정되면 반환값(ApiResponse 는 벗김)의 그 getter →
 * 메서드의 첫 Long 인자(@PathVariable 패턴) → 반환값에서 targetType 과 이름이 맞는 *Id getter.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String actionType();
    String targetType();
    /** 반환 DTO 에서 targetId 로 쓸 프로퍼티명 (예: "deptId"). 비우면 targetType 기반 휴리스틱. */
    String targetIdProperty() default "";
}
