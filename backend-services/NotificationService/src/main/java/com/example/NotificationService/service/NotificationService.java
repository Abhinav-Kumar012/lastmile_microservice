package com.example.NotificationService.service;

import com.example.kafkaevents.events.DestinationReachedEvent;
import com.example.kafkaevents.events.DriverArrived;
import com.example.kafkaevents.events.NotifyPartiesEvent;
import com.example.kafkaevents.events.TripConfirmedEvent;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@KafkaListener(topics = "notification-service")
public class NotificationService {

  private final Map<Integer, SseEmitter> driverEmitters = new ConcurrentHashMap<>();
  private final Map<Integer, SseEmitter> riderEmitters = new ConcurrentHashMap<>();
  private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

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
    Map<Integer, SseEmitter> map = "driver".equalsIgnoreCase(role) ? driverEmitters : riderEmitters;

    SseEmitter emitter = map.get(id);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name("notification").data(data));
      } catch (IOException e) {
        map.remove(id);
      }
    }
  }

  @KafkaHandler
  public void NotifyPartiesThatAreMatched(NotifyPartiesEvent event, Acknowledgment ack) {
    sendNotification(
        "rider",
        event.getRiderId(),
        "Driver with ID "
            + event.getDriverId()
            + " has been assigned to you.Your Trip id is :"
            + event.getTripId()
            + " and your arrival id is :"
            + event.getArrivalId()
            + " Vehicle Number: "
            + event.getVehiclenumber());
    sendNotification(
        "driver",
        event.getDriverId(),
        "You have been assigned to Rider with ID "
            + event.getRiderId()
            + "."
            + " Go to Metro Station: "
            + event.getArrivalstationname());
    logger.info(
        "Notifications regarding matching sent to Rider ID: {} and Driver ID: {}",
        event.getRiderId(),
        event.getDriverId());
    ack.acknowledge();
  }

  @KafkaHandler
  public void DriverArrivedForPickup(DriverArrived event, Acknowledgment ack) {
    sendNotification("rider", event.getRiderId(), "Your driver has arrived for pickup.");
    logger.info("Notification sent to Rider ID: {} about driver arrival.", event.getRiderId());
    ack.acknowledge();
  }

  @KafkaHandler
  public void RideStartedNotification(TripConfirmedEvent event, Acknowledgment ack) {
    sendNotification("rider", event.getRiderId(), "Your trip has started. Enjoy your ride!");
    sendNotification("driver", event.getDriverId(), "The trip has started. Drive safely!");
    logger.info(
        "Trip Confirmation Send to Rider ID: {} and Driver ID: {}",
        event.getRiderId(),
        event.getDriverId());
    ack.acknowledge();
  }

  @KafkaHandler
  public void DestinationReachedNotification(DestinationReachedEvent event, Acknowledgment ack) {
    sendNotification(
        "rider", event.getRiderId(), "You have reached your destination. Please rate your driver.");
    sendNotification(
        "driver",
        event.getDriverId(),
        "The rider has reached the destination for Trip ID: " + event.getTripId() + ".");
    logger.info(
        "Destination reached notifications sent to Rider ID: {} and Driver ID: {}",
        event.getRiderId(),
        event.getDriverId());
    ack.acknowledge();
    // TODO:Update the notification messages
  }
}
