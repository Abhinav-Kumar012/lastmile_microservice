package com.example.NotificationService.service;

import com.example.kafkaevents.events.NotifyPartiesEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

@Service
public class NotificationService {

    private final Map<Integer, SseEmitter> driverEmitters = new ConcurrentHashMap<>();
    private final Map<Integer, SseEmitter> riderEmitters = new ConcurrentHashMap<>();

    public SseEmitter addEmitter(String role, Integer id) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        if ("driver".equalsIgnoreCase(role)) {
            driverEmitters.put(id, emitter);
        } else {
            riderEmitters.put(id, emitter);
        }

        emitter.onCompletion(() -> removeEmitter(role, id));
        emitter.onTimeout(() -> removeEmitter(role, id));
        emitter.onError(e -> removeEmitter(role, id));

        return emitter;
    }

    private void removeEmitter(String role, Integer id) {
        if ("driver".equalsIgnoreCase(role)) {
            driverEmitters.remove(id);
        } else {
            riderEmitters.remove(id);
        }
    }

    public void sendNotification(String role, Integer id, Object data) {
        Map<Integer, SseEmitter> map =
                "driver".equalsIgnoreCase(role) ? driverEmitters : riderEmitters;

        SseEmitter emitter = map.get(id);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(data));
            } catch (IOException e) {
                map.remove(id);
            }
        }
    }
    @KafkaListener(topics = "notification-service")
    public void listen(NotifyPartiesEvent event, Acknowledgment ack)
    {
        sendNotification("rider", event.getRiderId(), "Driver with ID " + event.getDriverId() + " has been assigned to you.");
        sendNotification("driver", event.getDriverId(), "You have been assigned to Rider with ID " + event.getRiderId() + ".");
        ack.acknowledge();
    }
}

