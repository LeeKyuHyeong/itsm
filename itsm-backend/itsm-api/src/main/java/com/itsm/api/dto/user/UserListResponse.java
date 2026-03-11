package com.itsm.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {

    private Long userId;
    private String loginId;
    private String userNm;
    private String employeeNo;
    private String email;
    private String status;
    private String deptName;
    private String companyName;
    private List<String> roles;
}
