package com.itsm.api.interceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 2026-09-16 전수조사 P1 — tb_menu.menu_url 은 프론트 라우트(/incidents)인데 인터셉터는 API URI(/api/v1/incidents)와
 * AntPathMatcher 로 대조해 **한 번도 매칭되지 않았다** → 메뉴 기반 인가·메뉴 접근 로그가 모두 무효.
 * ApiMenuMapper 가 API URI + HTTP 메서드를 "권한을 물어볼 메뉴 URL" 로 바꾼다.
 */
class ApiMenuMapperTest {

    private final ApiMenuMapper mapper = new ApiMenuMapper();

    @ParameterizedTest(name = "{0} {1} → {2}")
    @CsvSource({
            "GET,    /api/v1/incidents,                       /incidents",
            "POST,   /api/v1/incidents,                       /incidents",
            "PATCH,  /api/v1/incidents/12/status,             /incidents",
            "GET,    /api/v1/service-requests/3,              /service-requests",
            "DELETE, /api/v1/changes/3/approvers/9,           /changes",
            "GET,    /api/v1/assets/hw,                       /assets/hw",
            "PATCH,  /api/v1/assets/sw/7,                     /assets/sw",
            "POST,   /api/v1/assets/oa,                       /assets/oa",
            "GET,    /api/v1/inspections/1/items,             /inspections",
            "GET,    /api/v1/reports,                         /reports",
            "POST,   /api/v1/reports,                         /reports",
            "GET,    /api/v1/dashboard,                       /dashboard",
            "PATCH,  /api/v1/admin/batch-jobs/1,              /admin/batch-jobs",
            "GET,    /api/v1/admin/batch-jobs,                /admin/batch-jobs",
            "POST,   /api/v1/admin/sla-policies,              /admin/sla",
            "GET,    /api/v1/admin/notification-policies,     /admin/notification-policy",
            "POST,   /api/v1/admin/menus,                     /admin/menus",
            "POST,   /api/v1/users,                           /admin/accounts",
            "PATCH,  /api/v1/users/5/status,                  /admin/accounts",
            "POST,   /api/v1/companies/1/departments,         /admin/organizations",
            "POST,   /api/v1/common-codes,                    /admin/common-codes",
            "PATCH,  /api/v1/report-forms/1,                  /reports"
    })
    void mapsGovernedRequests(String method, String uri, String expectedMenu) {
        assertThat(mapper.resolveMenuUrl(uri, method)).contains(expectedMenu);
    }

    @ParameterizedTest(name = "{0} {1} 은 메뉴 권한을 묻지 않는다")
    @CsvSource({
            // 모든 역할이 화면 어디서나 쓰는 조회 — 담당자 선택, 회사/부서 선택, 공통코드 드롭다운, 사이드바, 보고서 양식 조회
            "GET,     /api/v1/users",
            "GET,     /api/v1/users/5",
            "GET,     /api/v1/companies",
            "GET,     /api/v1/companies/1/departments",
            "GET,     /api/v1/common-codes/PRIORITY",
            "GET,     /api/v1/admin/menus",
            "GET,     /api/v1/report-forms",
            // 메뉴가 없는 자원 — 알림(본인), 게시판(게시판별 권한), 자산 통계(대시보드), 인증
            "GET,     /api/v1/notifications",
            "PATCH,   /api/v1/notifications/3/read",
            "GET,     /api/v1/boards/1/posts",
            "POST,    /api/v1/boards/1/posts",
            "GET,     /api/v1/assets/stats",
            "POST,    /api/v1/auth/logout",
            "OPTIONS, /api/v1/incidents"
    })
    void exemptRequestsAreNotGoverned(String method, String uri) {
        assertThat(mapper.resolveMenuUrl(uri, method)).isEmpty();
    }

    @Test
    @DisplayName("쓰기 판정: GET/HEAD/OPTIONS 는 읽기, 나머지는 쓰기")
    void writeDetection() {
        assertThat(mapper.isWrite("GET")).isFalse();
        assertThat(mapper.isWrite("head")).isFalse();
        assertThat(mapper.isWrite("OPTIONS")).isFalse();
        assertThat(mapper.isWrite("POST")).isTrue();
        assertThat(mapper.isWrite("PATCH")).isTrue();
        assertThat(mapper.isWrite("DELETE")).isTrue();
    }

    @Test
    @DisplayName("매핑 대상 메뉴 URL 은 전부 시드(02_dml.sql)에 존재하는 프론트 라우트여야 한다")
    void mappedMenuUrlsExistInSeed() throws Exception {
        String dml = java.nio.file.Files.readString(
                java.nio.file.Path.of("..", "..", "sql", "02_dml.sql").toAbsolutePath().normalize());
        for (String menuUrl : mapper.governedMenuUrls()) {
            assertThat(dml).as("시드에 메뉴 URL %s", menuUrl).contains("'" + menuUrl + "'");
        }
    }
}
