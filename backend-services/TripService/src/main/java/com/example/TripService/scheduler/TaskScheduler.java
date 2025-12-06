package com.example.TripService.scheduler;

import com.example.TripService.entity.Trip;
import com.example.TripService.repository.TripRepository;
import com.example.kafkaevents.events.*;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// This Scheduler manager pickup and dropoff events
@Component("triptaskscheduler")
public class TaskScheduler {

  @Autowired private RedisTemplate<String, Object> redisTemplate;

  @Autowired private KafkaTemplate<String, Object> kafkaTemplate;

  @Autowired private TripRepository tripRepository;
  private static final Logger logger = LoggerFactory.getLogger(TaskScheduler.class);

  @Scheduled(fixedDelay = 10000) // rus every 10 seconds
  // TODO I need to set the delay time
  public void manageTrips() {

    ScanOptions options =
        ScanOptions.scanOptions().match("driver-service:driver:*:route").count(100).build();

    try (Cursor<byte[]> cursor =
        (Cursor<byte[]>)
            redisTemplate.executeWithStickyConnection(conn -> conn.keyCommands().scan(options))) {

      while (cursor != null && cursor.hasNext()) {
        String key = new String(cursor.next());
        DriverDataRedis value = (DriverDataRedis) redisTemplate.opsForValue().get(key);
        // Business logic
        Integer driverid = Integer.parseInt(key.split(":")[2]);

        if (value.getCurrentLocation().length() >= 12
            && value.getCurrentLocation().substring(0, 12).equals("METROSTATION")) {
          ArrayList<Trip> scheduledTrips =
              tripRepository.getScheduledTripsForDriver(driverid, value.getCurrentLocation());
          // THis is to get all the trips that are shcheduled for the driver when we reaches some
          // metro stations for pickup.
          for (Trip trip : scheduledTrips) {
            kafkaTemplate.send("notification-service", new DriverArrived(trip.getRiderId()));
            logger.info("Driver arrived event sent for riderId: " + trip.getRiderId());
          }
        } else if (value.getCurrentLocation().equals(value.getDestination())) {
          // Get all the trups that are active for this driver
          ArrayList<Trip> ongoingTrips = tripRepository.getActiveTripsForDriver(driverid);
          for (Trip trip : ongoingTrips) {
            kafkaTemplate.send(
                "notification-service",
                new DestinationReachedEvent(trip.getRiderId(), driverid, trip.getTripId()));
            trip.setStatus("COMPLETED");
            tripRepository.save(trip);
            kafkaTemplate.send(
                "rider-service", new TripCompleted(trip.getRiderId(), trip.getArrivalId()));
            logger.info("Destination reached event sent for riderId: " + trip.getRiderId());
          }
          redisTemplate.delete(key);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
