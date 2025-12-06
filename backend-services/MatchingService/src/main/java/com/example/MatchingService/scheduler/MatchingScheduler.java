package com.example.MatchingService.scheduler;

import com.example.MatchingService.config.RedisLockManager;
import com.example.MatchingService.service.locationgrpc;
import com.example.kafkaevents.events.DriverDataRedis;
import com.example.kafkaevents.events.MatchingEvent;
import com.example.kafkaevents.events.RiderDataRedis;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("matchingScheduler")
public class MatchingScheduler {

  private RedisLockManager lockManager;
  private RedisTemplate<String, DriverDataRedis> redisTemplateForDriver;
  private RedisTemplate<String, RiderDataRedis> redisTemplateForRider;
  private final String instanceId = UUID.randomUUID().toString(); // unique per instance
  private static final String LOCK_KEY = "locks:job-runner";
  private Integer window_size = 5;
  private locationgrpc grpcclient;
  private KafkaTemplate<String, Object> kafkaTemplate;
  private static final Logger logger = LoggerFactory.getLogger(MatchingScheduler.class);

  public MatchingScheduler(
      RedisLockManager lockManager,
      @Qualifier("driverRedisTemplate")
          RedisTemplate<String, DriverDataRedis> redisTemplateForDriver,
      @Qualifier("riderRedisTemplate") RedisTemplate<String, RiderDataRedis> redisTemplateForRider,
      locationgrpc grpcclient,
      KafkaTemplate<String, Object> kafkaTemplate) {
    this.lockManager = lockManager;
    this.redisTemplateForDriver = redisTemplateForDriver;
    this.redisTemplateForRider = redisTemplateForRider;
    this.grpcclient = grpcclient;
    this.kafkaTemplate = kafkaTemplate;
  }

  private <T> HashMap<String, T> getDataFromRedis(
      String matchkey, RedisTemplate<String, T> redisTemplate) {
    ScanOptions options = ScanOptions.scanOptions().match(matchkey).count(100).build();
    HashMap<String, T> resultMap = new HashMap<>();

    try (Cursor<byte[]> cursor =
        (Cursor<byte[]>)
            redisTemplate.executeWithStickyConnection(conn -> conn.keyCommands().scan(options))) {

      while (cursor != null && cursor.hasNext()) {
        String key = new String(cursor.next());
        T value = redisTemplate.opsForValue().get(key);
        resultMap.put(key, value);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return resultMap;
  }

  private void match(
      HashMap<String, DriverDataRedis> driverdata, HashMap<String, RiderDataRedis> riderdata) {
    for (String dkey : driverdata.keySet()) {
      DriverDataRedis driverData = (DriverDataRedis) driverdata.get(dkey);
      for (String rkey : riderdata.keySet()) {
        // Matching Logic
        RiderDataRedis riderData = (RiderDataRedis) riderdata.get(rkey);
        if (driverData.getDestination().equals(riderData.getDestination())
            && driverData.getAvailableSeats() > 0
            && Duration.between(LocalDateTime.now(), riderData.getArrivaltime()).toMinutes()
                <= window_size
            && grpcclient.isStationNearby(
                riderData.getArrivalstationname(), driverData.getCurrentLocation())) {
          driverData.setAvailableSeats(driverData.getAvailableSeats() - 1);
          Integer riderId = Integer.parseInt(rkey.split(":")[2]);
          Integer driverId = Integer.parseInt(dkey.split(":")[2]);
          kafkaTemplate.send(
              "trip-service",
              new MatchingEvent(
                  riderId,
                  driverId,
                  riderData.getArrivalId(),
                  riderData.getArrivalstationname(),
                  driverData.getVehicleNumber()));
          redisTemplateForRider.delete(rkey);
          logger.info("Matched Driver: {} and Rider: {} ", dkey, rkey);
        }
      }
      // For the Drivers with available seats, update Redis so they can be matched again
      if (driverData.getAvailableSeats() > 0) {
        redisTemplateForDriver.opsForValue().set(dkey, driverData);
      } else {
        redisTemplateForDriver.delete(dkey);
      }
    }
  }

  @Scheduled(fixedDelay = 5000) // Fix the Time Required
  public void runPeriodicJob() {
    //        System.out.println("HI from Matching Scheduler");
    HashMap<String, DriverDataRedis> driverdata;
    HashMap<String, RiderDataRedis> riderdata;
    boolean acquired = lockManager.tryAcquireLock(LOCK_KEY, instanceId, Duration.ofSeconds(10));
    if (!acquired) {
      // Some other instance is running it
      return;
    }

    try {
      driverdata = getDataFromRedis("driver-service:driver:*:route", redisTemplateForDriver);
      riderdata = getDataFromRedis("rider-service:rider:*:arrival", redisTemplateForRider);
      match(driverdata, riderdata);

    } finally {
      lockManager.releaseLock(LOCK_KEY, instanceId);
    }
  }
}
