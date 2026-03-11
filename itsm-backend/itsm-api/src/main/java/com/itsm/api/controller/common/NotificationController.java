package com.itsm.api.controller.common;

import com.itsm.api.dto.common.NotificationResponse;
import com.itsm.api.service.common.NotificationService;
import com.itsm.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ApiResponse.success(notificationService.getMyNotifications(userId));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ApiResponse.success(notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/{notiId}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long notiId, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        notificationService.markAsRead(notiId, userId);
        return ApiResponse.success();
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        notificationService.markAllAsRead(userId);
        return ApiResponse.success();
    }

    private Long getCurrentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
