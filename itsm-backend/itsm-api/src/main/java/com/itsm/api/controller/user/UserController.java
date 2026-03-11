package com.itsm.api.controller.user;

import com.itsm.api.dto.user.*;
import com.itsm.api.service.user.UserService;
import com.itsm.core.domain.user.UserHistory;
import com.itsm.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<Page<UserListResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ApiResponse.success(userService.getUsers(keyword, pageable));
    }

    @PostMapping
    public ApiResponse<UserDetailResponse> createUser(
            @Valid @RequestBody UserCreateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(userService.createUser(req, currentUserId));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserDetailResponse> getUser(@PathVariable Long userId) {
        return ApiResponse.success(userService.getUser(userId));
    }

    @PatchMapping("/{userId}")
    public ApiResponse<UserDetailResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ApiResponse.success(userService.updateUser(userId, req, currentUserId));
    }

    @PatchMapping("/{userId}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        userService.changeUserStatus(userId, req, currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/{userId}/history")
    public ApiResponse<List<UserHistory>> getUserHistory(@PathVariable Long userId) {
        return ApiResponse.success(userService.getUserHistory(userId));
    }

    @PostMapping("/{userId}/roles")
    public ApiResponse<Void> grantRole(
            @PathVariable Long userId,
            @Valid @RequestBody RoleGrantRequest req,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        userService.grantRole(userId, req, currentUserId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ApiResponse<Void> revokeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        userService.revokeRole(userId, roleId, currentUserId);
        return ApiResponse.success();
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
