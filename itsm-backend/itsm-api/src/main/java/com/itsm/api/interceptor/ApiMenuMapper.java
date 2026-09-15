package com.itsm.api.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * API URI + HTTP 메서드 → "권한을 물어볼 메뉴 URL(tb_menu.menu_url)" 매핑.
 * <p>
 * 2026-09-16 전수조사 P1: tb_menu.menu_url 은 프론트 라우트(/incidents)이고 인터셉터는 API URI(/api/v1/incidents)를
 * 대조하고 있어 AuthInterceptor 의 메뉴 기반 인가와 MenuAccessInterceptor 의 접근 로그가 한 번도 동작하지 않았다.
 * (fix_menu_urls.sql 에서 menu_url 을 프론트 경로로 바꾼 시점에 백엔드 대조가 죽었다.)
 * <p>
 * 규칙
 * <ul>
 *   <li>GET/HEAD/OPTIONS 는 읽기(can_read), 나머지는 쓰기(can_write) 로 판정한다.</li>
 *   <li>{@code readExempt} 항목은 읽기를 묻지 않는다 — 담당자 선택(users), 회사/부서 선택(companies), 공통코드 드롭다운,
 *       사이드바(admin/menus), 보고서 양식 조회처럼 모든 역할이 화면 어디서나 쓰는 조회. 쓰기는 여전히 관리 메뉴 권한을 본다.</li>
 *   <li>매핑에 없는 URI(알림·게시판·자산통계·인증)는 인터셉터가 관여하지 않는다 — 게시판은 게시판별 권한, 알림은 본인 것만.</li>
 * </ul>
 * 매핑을 바꿀 때는 sql/02_dml.sql 의 메뉴 시드 URL 과 맞춰야 한다 (ApiMenuMapperTest 가 검증).
 */
@Component
public class ApiMenuMapper {

    /** apiPattern(AntPath) → menuUrl. readExempt=true 면 읽기는 권한을 묻지 않는다. */
    private record Mapping(String apiPattern, String menuUrl, boolean readExempt) { }

    private static final List<Mapping> MAPPINGS = List.of(
            new Mapping("/api/v1/dashboard/**",                    "/dashboard",                 false),
            new Mapping("/api/v1/incidents/**",                    "/incidents",                 false),
            new Mapping("/api/v1/service-requests/**",             "/service-requests",          false),
            new Mapping("/api/v1/changes/**",                      "/changes",                   false),
            new Mapping("/api/v1/assets/hw/**",                    "/assets/hw",                 false),
            new Mapping("/api/v1/assets/sw/**",                    "/assets/sw",                 false),
            new Mapping("/api/v1/assets/oa/**",                    "/assets/oa",                 false),
            new Mapping("/api/v1/inspections/**",                  "/inspections",               false),
            new Mapping("/api/v1/reports/**",                      "/reports",                   false),
            new Mapping("/api/v1/report-forms/**",                 "/reports",                   true),
            new Mapping("/api/v1/admin/menus/**",                  "/admin/menus",               true),
            new Mapping("/api/v1/admin/batch-jobs/**",             "/admin/batch-jobs",          false),
            new Mapping("/api/v1/admin/sla-policies/**",           "/admin/sla",                 false),
            new Mapping("/api/v1/admin/notification-policies/**",  "/admin/notification-policy", false),
            new Mapping("/api/v1/common-codes/**",                 "/admin/common-codes",        true),
            new Mapping("/api/v1/users/**",                        "/admin/accounts",            true),
            new Mapping("/api/v1/companies/**",                    "/admin/organizations",       true)
    );

    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean isWrite(String method) {
        return method == null || !READ_METHODS.contains(method.toUpperCase(Locale.ROOT));
    }

    /** 이 요청이 메뉴 권한 대상이면 그 메뉴 URL, 아니면 empty */
    public Optional<String> resolveMenuUrl(String requestUri, String method) {
        if (requestUri == null || (method != null && "OPTIONS".equalsIgnoreCase(method))) {
            return Optional.empty(); // CORS preflight 는 인증 정보가 없다 — 권한 대상이 아니다
        }
        boolean write = isWrite(method);
        for (Mapping m : MAPPINGS) {
            if (pathMatcher.match(m.apiPattern(), requestUri)) {
                if (!write && m.readExempt()) {
                    return Optional.empty();
                }
                return Optional.of(m.menuUrl());
            }
        }
        return Optional.empty();
    }

    /** 매핑이 가리키는 메뉴 URL 집합 (시드 정합성 테스트용) */
    public Set<String> governedMenuUrls() {
        Set<String> urls = new TreeSet<>();
        for (Mapping m : MAPPINGS) {
            urls.add(m.menuUrl());
        }
        return urls;
    }
}
