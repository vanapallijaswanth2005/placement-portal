package com.example.placementportal.controller;

import com.example.placementportal.entity.Notification;
import com.example.placementportal.service.SseNotificationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final SseNotificationService sseNotificationService;

    public NotificationController(SseNotificationService sseNotificationService) {
        this.sseNotificationService = sseNotificationService;
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return sseNotificationService.subscribe(username);
    }

    @GetMapping
    public List<Notification> getNotifications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return sseNotificationService.getNotificationsForUser(username);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        sseNotificationService.markAsRead(id, username);
    }
}
