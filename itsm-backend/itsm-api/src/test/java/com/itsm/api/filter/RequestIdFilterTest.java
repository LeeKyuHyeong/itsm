package com.itsm.api.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestIdFilterTest {

    private RequestIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new RequestIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("X-Request-Id 헤더가 없으면 UUID를 생성하여 응답 헤더에 설정한다")
    void generatesUuidWhenHeaderAbsent() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String responseId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertThat(responseId).isNotNull();
        UUID parsed = UUID.fromString(responseId);
        assertThat(parsed).isNotNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("X-Request-Id 헤더가 있으면 그대로 사용한다 (분산 추적 호환)")
    void usesHeaderWhenPresent() throws Exception {
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "upstream-trace-123");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo("upstream-trace-123");
    }

    @Test
    @DisplayName("X-Request-Id 헤더가 공백이면 UUID를 새로 생성한다")
    void generatesUuidWhenHeaderBlank() throws Exception {
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "   ");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String responseId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertThat(responseId).isNotBlank();
        assertThat(responseId).isNotEqualTo("   ");
    }

    @Test
    @DisplayName("필터 실행 중 MDC에 requestId가 설정되고, 종료 후 정리된다")
    void mdcSetDuringFilterAndClearedAfter() throws Exception {
        AtomicReference<String> capturedMdc = new AtomicReference<>();
        FilterChain chain = (req, resp) -> capturedMdc.set(MDC.get(RequestIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(capturedMdc.get()).isNotNull();
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("필터 체인에서 예외가 발생해도 MDC가 정리된다")
    void mdcClearedOnException() {
        FilterChain chain = (req, resp) -> {
            throw new RuntimeException("simulated");
        };

        try {
            filter.doFilter(request, response, chain);
        } catch (Exception e) {
            // expected
        }

        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }
}
