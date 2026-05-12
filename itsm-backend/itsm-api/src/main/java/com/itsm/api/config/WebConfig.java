package com.itsm.api.config;

import com.itsm.api.interceptor.AuthInterceptor;
import com.itsm.api.interceptor.MenuAccessInterceptor;
import com.itsm.api.interceptor.PasswordExpiryInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    private final AuthInterceptor authInterceptor;
    private final MenuAccessInterceptor menuAccessInterceptor;
    private final PasswordExpiryInterceptor passwordExpiryInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toArray(String[]::new))
                .allowedMethods(Arrays.stream(allowedMethods.split(",")).map(String::trim).toArray(String[]::new))
                .allowedHeaders("Content-Type", "Accept", "X-Requested-With")
                .exposedHeaders("Content-Disposition")
                .allowCredentials(allowCredentials)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                );

        registry.addInterceptor(passwordExpiryInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                );

        registry.addInterceptor(menuAccessInterceptor)
                .addPathPatterns("/api/**");
    }
}
