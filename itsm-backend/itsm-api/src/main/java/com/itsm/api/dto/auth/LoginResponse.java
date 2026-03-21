package com.itsm.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String loginId;
    private String userNm;
    private List<String> roles;

    public LoginResponse withoutTokens() {
        return LoginResponse.builder()
                .userId(this.userId)
                .loginId(this.loginId)
                .userNm(this.userNm)
                .roles(this.roles)
                .build();
    }
}
