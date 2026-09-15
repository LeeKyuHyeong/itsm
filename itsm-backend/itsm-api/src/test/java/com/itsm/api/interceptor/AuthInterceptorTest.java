package com.itsm.api.interceptor;

import com.itsm.api.service.MenuCacheService;
import com.itsm.core.domain.user.Menu;
import com.itsm.core.domain.user.RoleMenu;
import com.itsm.core.domain.user.UserRole;
import com.itsm.core.exception.BusinessException;
import com.itsm.core.exception.ErrorCode;
import com.itsm.core.repository.user.RoleMenuRepository;
import com.itsm.core.repository.user.UserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 2026-09-16 전수조사 P1 — 이전 테스트는 menuUrl("/api/v1/users/**") 라는 실제 시드에 없는 값으로 통과했다.
 * 실제 tb_menu.menu_url 은 프론트 라우트(/incidents, /admin/accounts …)이므로 그 값으로 검증한다.
 * 의미: GET/HEAD/OPTIONS 는 can_read, 나머지는 can_write. SUPER_ADMIN 은 무조건 통과. 매핑 밖 URL 은 통과.
 */
@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private RoleMenuRepository roleMenuRepository;

    @Mock
    private MenuCacheService menuCacheService;

    @Mock
    private UserRoleRepository userRoleRepository;

    private AuthInterceptor authInterceptor;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private final Object handler = new Object();

    @BeforeEach
    void setUp() {
        authInterceptor = new AuthInterceptor(roleMenuRepository, menuCacheService, userRoleRepository, new ApiMenuMapper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void request(String method, String uri) {
        given(request.getRequestURI()).willReturn(uri);
        // SUPER_ADMIN 우회 경로에서는 메서드를 읽지 않으므로 lenient
        org.mockito.Mockito.lenient().when(request.getMethod()).thenReturn(method);
    }

    private Menu menu(Long id, String url) {
        Menu menu = Menu.builder().menuNm("m" + id).menuUrl(url).sortOrder(1).build();
        ReflectionTestUtils.setField(menu, "menuId", id);
        return menu;
    }

    /** 사용자 1 이 역할 5 를 가지며, 역할 5 는 menuId 에 (canRead, canWrite) 권한 */
    private void roleMenu(Long menuId, String canRead, String canWrite) {
        given(userRoleRepository.findByUserIdWithRole(1L)).willReturn(List.of(new UserRole(1L, 5L, 1L)));
        given(roleMenuRepository.findByRoleId(5L)).willReturn(List.of(new RoleMenu(5L, menuId, canRead, canWrite, 1L)));
    }

    private void setAuthentication(Long userId, List<String> roles) {
        List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, "testUser", authorities));
    }

    @Test
    @DisplayName("인증 엔드포인트는 인터셉터를 건너뛴다")
    void skipsAuthEndpoints() {
        given(request.getRequestURI()).willReturn("/api/v1/auth/login");

        assertThat(authInterceptor.preHandle(request, response, handler)).isTrue();
        verify(menuCacheService, never()).getAllMenus();
    }

    @Test
    @DisplayName("인증되지 않은 요청은 UNAUTHORIZED")
    void unauthenticated_throws() {
        given(request.getRequestURI()).willReturn("/api/v1/incidents");

        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, handler))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("SUPER_ADMIN 은 메뉴 조회 없이 모든 접근이 허용된다")
    void superAdmin_bypasses() {
        request("DELETE", "/api/v1/incidents/1/assets/HW/2");
        setAuthentication(1L, List.of("ROLE_SUPER_ADMIN"));

        assertThat(authInterceptor.preHandle(request, response, handler)).isTrue();
        verify(menuCacheService, never()).getAllMenus();
    }

    @Test
    @DisplayName("GET /api/v1/incidents 는 메뉴 /incidents 의 can_read 로 허용된다 (실제 시드 URL)")
    void read_allowedByCanRead() {
        request("GET", "/api/v1/incidents");
        setAuthentication(1L, List.of("ROLE_AUDITOR"));
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu(11L, "/incidents"), menu(25L, "/admin/menus")));
        roleMenu(11L, "Y", "N");

        assertThat(authInterceptor.preHandle(request, response, handler)).isTrue();
    }

    @Test
    @DisplayName("감사자(can_write=N)의 POST /api/v1/incidents 는 ACCESS_DENIED — 읽기 전용이 실제로 강제된다")
    void write_deniedWhenCanWriteN() {
        request("POST", "/api/v1/incidents");
        setAuthentication(1L, List.of("ROLE_AUDITOR"));
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu(11L, "/incidents")));
        roleMenu(11L, "Y", "N");

        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, handler))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("고객사(변경관리 메뉴 없음)의 GET /api/v1/changes 는 ACCESS_DENIED")
    void read_deniedWhenNoRoleMenu() {
        request("GET", "/api/v1/changes");
        setAuthentication(1L, List.of("ROLE_CUSTOMER"));
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu(16L, "/changes"), menu(11L, "/incidents")));
        roleMenu(11L, "Y", "Y"); // 장애 메뉴 권한만 있음

        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, handler))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("PATCH /api/v1/incidents/12/status 처럼 하위 경로도 같은 메뉴(/incidents)로 판정한다")
    void subPath_mapsToSameMenu() {
        request("PATCH", "/api/v1/incidents/12/status");
        setAuthentication(1L, List.of("ROLE_PM"));
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu(11L, "/incidents")));
        roleMenu(11L, "Y", "Y");

        assertThat(authInterceptor.preHandle(request, response, handler)).isTrue();
    }

    @Test
    @DisplayName("GET /api/v1/users (담당자 선택용 조회) 는 계정관리 메뉴 권한 없이도 허용된다")
    void exemptRead_allowedWithoutMenu() {
        request("GET", "/api/v1/users");
        setAuthentication(1L, List.of("ROLE_PM"));

        assertThat(authInterceptor.preHandle(request, response, handler)).isTrue();
        verify(menuCacheService, never()).getAllMenus();
    }

    @Test
    @DisplayName("POST /api/v1/users 는 계정관리(/admin/accounts) can_write 가 필요하다")
    void exemptResource_writeStillGoverned() {
        request("POST", "/api/v1/users");
        setAuthentication(1L, List.of("ROLE_PM"));
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu(39L, "/admin/accounts")));
        roleMenu(11L, "Y", "Y"); // 계정관리 권한 없음

        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, handler))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("매핑에 없는 URL(알림 등)은 허용된다")
    void unmapped_allowed() {
        request("GET", "/api/v1/notifications");
        setAuthentication(1L, List.of("ROLE_EXTERNAL"));

        assertThat(authInterceptor.preHandle(request, response, handler)).isTrue();
        verify(roleMenuRepository, never()).findByRoleId(any());
    }

    @Test
    @DisplayName("매핑된 메뉴 행이 DB 에 없으면(관리자가 지움) 허용하되 조용히 넘어가지 않도록 메뉴 조회는 한다")
    void mappedButMenuRowMissing_allowed() {
        request("GET", "/api/v1/inspections");
        setAuthentication(1L, List.of("ROLE_EXTERNAL"));
        given(menuCacheService.getAllMenus()).willReturn(List.of(menu(11L, "/incidents")));

        assertThat(authInterceptor.preHandle(request, response, handler)).isTrue();
        verify(roleMenuRepository, never()).findByRoleId(any());
    }
}
