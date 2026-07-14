package com.example.placementportal.service;

import com.example.placementportal.entity.Notification;
import com.example.placementportal.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseNotificationService {

    private final NotificationRepository notificationRepository;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public SseEmitter subscribe(String username) {
        // Keep connection open for a long time (e.g. 1 hour)
        SseEmitter emitter = new SseEmitter(3600000L);
        emitters.put(username, emitter);

        emitter.onCompletion(() -> emitters.remove(username));
        emitter.onTimeout(() -> emitters.remove(username));
        emitter.onError((e) -> emitters.remove(username));

        // Send a dummy event to establish the connection
        try {
            emitter.send(SseEmitter.event().name("init").data("Connected"));
        } catch (IOException e) {
            emitters.remove(username);
        }

        return emitter;
    }

    public void sendNotification(String username, String message) {
        Notification notification = new Notification(username, message);
        notification = notificationRepository.save(notification);

        SseEmitter emitter = emitters.get(username);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            } catch (IOException e) {
                emitters.remove(username);
            }
        }
    }

    public List<Notification> getNotificationsForUser(String username) {
        return notificationRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    public void markAsRead(Long id, String username) {
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getUsername().equals(username)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }
}
