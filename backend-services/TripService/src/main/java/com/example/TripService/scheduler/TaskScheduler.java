package com.example.TripService.scheduler;


import com.example.TripService.entity.Trip;
import com.example.TripService.repository.TripRepository;
import com.example.kafkaevents.events.DriverArrived;
import com.example.kafkaevents.events.DriverDataRedis;
import com.example.kafkaevents.events.TripCompleted;
import com.example.kafkaevents.events.UpdateStatusEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
//This Scheduler manager pickup and dropoff events
@Component("triptaskscheduler")
public class TaskScheduler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    @Autowired
    private TripRepository tripRepository;

    @Scheduled(fixedDelay = 10000)//rus every 10 seconds
    //TODO I need to set the delay time
    public void manageTrips() {

        ScanOptions options = ScanOptions.scanOptions()
                .match("driver-service:driver:*:route")
                .count(100)
                .build();

        try (Cursor<byte[]> cursor = (Cursor<byte[]>) redisTemplate.executeWithStickyConnection(
                conn -> conn.keyCommands().scan(options))) {

            while (cursor != null && cursor.hasNext()) {
                String key = new String(cursor.next());
                DriverDataRedis value = (DriverDataRedis) redisTemplate.opsForValue().get(key);
                // Business logic
                Integer driverid = Integer.parseInt(key.split(":")[2]);
                ArrayList<Trip> ongoingTrips = tripRepository.getOngoingTripsForDriver(driverid, value.getCurrentLocation());
                if(value.getCurrentLocation().substring(0,12).equals("METROSTATION"))
                {
                    for(Trip trip : ongoingTrips)
                    {
                        kafkaTemplate.send("notification-service",new DriverArrived(trip.getRiderId()));
                    }
                }
                else if(value.getCurrentLocation().substring(0).equals(value.getDestination()))
                {
                    for(Trip trip : ongoingTrips)
                    {
                        kafkaTemplate.send("notification-service",new DriverArrived(trip.getRiderId()));
                        trip.setStatus("COMPLETED");
                        tripRepository.save(trip);
                        kafkaTemplate.send("rider-service", new TripCompleted(trip.getRiderId()));

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

