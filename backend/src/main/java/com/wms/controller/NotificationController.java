package com.wms.controller;

import com.wms.common.Result;
import com.wms.entity.SysNotification;
import com.wms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知消息Controller
 */
@RestController
@RequestMapping("/api/sys/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取未读通知列表
     */
    @GetMapping("/unread")
    public Result<List<SysNotification>> getUnreadNotifications(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(notificationService.getUnreadNotifications(limit));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    public Result<Map<String, Integer>> getUnreadCount() {
        Map<String, Integer> result = new HashMap<>();
        result.put("count", notificationService.getUnreadCount());
        return Result.success(result);
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read/all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }
}
