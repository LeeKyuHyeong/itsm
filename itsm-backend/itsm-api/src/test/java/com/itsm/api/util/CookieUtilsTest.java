package com.itsm.api.util;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class CookieUtilsTest {

    @Test
    @DisplayName("쿠키에서 지정한 이름의 값을 추출한다")
    void extractCookie_found() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("accessToken", "jwt-token-value"));

        String value = CookieUtils.extractCookie(request, "accessToken");

        assertThat(value).isEqualTo("jwt-token-value");
    }

    @Test
    @DisplayName("쿠키가 없으면 null을 반환한다")
    void extractCookie_noCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String value = CookieUtils.extractCookie(request, "accessToken");

        assertThat(value).isNull();
    }

    @Test
    @DisplayName("해당 이름의 쿠키가 없으면 null을 반환한다")
    void extractCookie_notFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("otherCookie", "value"));

        String value = CookieUtils.extractCookie(request, "accessToken");

        assertThat(value).isNull();
    }
}
