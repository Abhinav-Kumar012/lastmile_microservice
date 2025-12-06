package com.example.TripService.controller;

import com.example.TripService.dtos.RideActiveConfirmationDTO;
import com.example.TripService.entity.Trip;
import com.example.TripService.repository.TripRepository;
import com.example.kafkaevents.events.TripConfirmedEvent;
import com.example.kafkaevents.events.UpdateStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TripController {
  // send event to kafka to rider service topic so change the status of ride of a specific arribalid
  @Autowired private TripRepository tripRepository;
  @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
  private static final Logger logger = LoggerFactory.getLogger(TripController.class);

  @PostMapping("/confirmTrip")
  public ResponseEntity<?> confirmTrip(
      @RequestBody RideActiveConfirmationDTO rideActiveConfirmationDTO) {
    Integer tripId = rideActiveConfirmationDTO.getTripId();
    Trip trip = tripRepository.findById(tripId).orElse(null);
    if (trip != null) {
      trip.setStatus("ACTIVE");
      tripRepository.save(trip);
      kafkaTemplate.send(
          "rider-service",
          new UpdateStatusEvent(rideActiveConfirmationDTO.getArrivalId(), "ACTIVE"));
      logger.info("Trip with id {} has been confirmed and status updated to ACTIVE", tripId);
      kafkaTemplate.send(
          "notification-service", new TripConfirmedEvent(trip.getRiderId(), trip.getDriverId()));
      return ResponseEntity.ok("Trip confirmed");
    }
    return ResponseEntity.status(404).body("Trip not found");
  }
}
