package com.example.RiderService.controller;

import com.example.RiderService.dtos.ArrivalRegistrationDTO;
import com.example.RiderService.dtos.StatusRequestDTO;
import com.example.RiderService.entity.Ride;
import com.example.RiderService.service.RiderService;
import com.example.kafkaevents.events.RiderDataRedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RiderRestController {
  @Autowired private RiderService riderService;

  @Autowired
  //    @Qualifier("redisTemplate")
  private RedisTemplate<String, Object> redisTemplate;

  private static final Logger logger = LoggerFactory.getLogger(RiderRestController.class);

  @PostMapping("/register-arrival")
  public ResponseEntity<?> RegisterArrival(
      @RequestBody ArrivalRegistrationDTO arrivalRegistrationDTO) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Integer riderid = (Integer) auth.getDetails(); // bring from security context(jwt se lao)
    Ride r =
        new Ride(
            riderid,
            arrivalRegistrationDTO.getArrivaltime(),
            arrivalRegistrationDTO.getDestination(),
            arrivalRegistrationDTO.getArrivalstationname());
    // Logic to save the Ride entity to the database would go here
    Ride databaserideobject = riderService.saveRide(r);
    String redisKey = "rider-service:rider:" + riderid + ":arrival";
    redisTemplate
        .opsForValue()
        .set(
            redisKey,
            new RiderDataRedis(
                databaserideobject.getArrivalId(),
                databaserideobject.getArrivaltime(),
                databaserideobject.getDestination(),
                databaserideobject.getArrivalstationname()));
    logger.info("arrival Registered for rider id: {}", riderid);
    return ResponseEntity.ok().body("Arrival registered successfully");
  }

  @GetMapping("/ride-status")
  public ResponseEntity<?> getRideStatus(@RequestBody StatusRequestDTO statusRequestDTO) {
    Ride r = riderService.getRideById(statusRequestDTO.getRideId());
    if (r == null) {
      return ResponseEntity.status(404).body("Ride not found");
    }
    return ResponseEntity.ok().body(r.getStatus());
  }
}
