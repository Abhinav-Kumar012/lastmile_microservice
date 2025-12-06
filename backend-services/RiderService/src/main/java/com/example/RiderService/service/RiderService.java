package com.example.RiderService.service;

import com.example.RiderService.entity.Ride;
import com.example.RiderService.repository.RiderRepository;
import com.example.kafkaevents.events.TripCompleted;
import com.example.kafkaevents.events.UpdateStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@KafkaListener(topics = "rider-service")
public class RiderService {
  @Autowired private RiderRepository riderRepository;
  private KafkaTemplate<String, Object> kafkaTemplate;
  @Autowired private RedisTemplate<String, Object> redisTemplate;

  private static final Logger logger = LoggerFactory.getLogger(RiderService.class);

  public Ride saveRide(Ride ride) {
    return riderRepository.save(ride);
  }

  public Ride getRideById(Integer ride_id) {
    return riderRepository.findById(ride_id).orElse(null);
  }

  @KafkaHandler
  public void handleTripCompletion(TripCompleted tripCompleted, Acknowledgment ack) {
    Ride ride = riderRepository.findById(tripCompleted.getArrivalId()).orElse(null);

    if (ride != null) {
      ride.setStatus("COMPLETED");
      riderRepository.save(ride);
      logger.info("Ride with ArrivalID {} marked as COMPLETED", ride.getArrivalId());
    }
    ack.acknowledge();
  }

  @KafkaHandler
  public void handleUpdateStatusEvent(UpdateStatusEvent updateStatusEvent, Acknowledgment ack) {
    Ride ride = riderRepository.findById(updateStatusEvent.getArrivalId()).orElse(null);
    if (ride != null) {
      ride.setStatus(updateStatusEvent.getStatus());
      riderRepository.save(ride);
      logger.info(
          "Ride with ArrivalID {} status updated to {}",
          ride.getArrivalId(),
          updateStatusEvent.getStatus());
    }
    ack.acknowledge();
  }
}
