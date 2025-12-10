package com.example.DriverService.controller;

import com.example.DriverService.dtos.RegistrationDTO;
import com.example.DriverService.dtos.UpdateLocationDTO;
import com.example.DriverService.entity.Route;
import com.example.DriverService.repository.RouteRepository;
import com.example.kafkaevents.events.DriverDataRedis;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {
  @Autowired private RouteRepository routeRepository;
  @Autowired private RedisTemplate<String, Object> redisTemplate;
  private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

  @PostMapping("/register-route")
  public ResponseEntity<Map<String, Integer>> registerRoute(
      @RequestBody RegistrationDTO registrationDTO) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Integer driverid = (Integer) auth.getDetails();
    // We need to save save this route in the db and in the redis persist driver id as key and start
    // location as value so that When the driver location updates we just update that enitty in
    // rediss and we must also update currentlocation in db in case the entry from redis evicts
    Route r =
        new Route(
            driverid,
            registrationDTO.getStartinglocation(),
            registrationDTO.getDestination(),
            registrationDTO.getVehiclenumber());
    Route savedroute = routeRepository.save(r);
    // add the logic for adding the entry to the redis too
    String redisKey = "driver-service:driver:" + driverid + ":route";
    redisTemplate
        .opsForValue()
        .set(
            redisKey,
            new DriverDataRedis(
                registrationDTO.getStartinglocation(),
                registrationDTO.getAvailable_seats(),
                registrationDTO.getDestination(),
                registrationDTO.getVehiclenumber()));
    Map<String, Integer> map = new HashMap<>();
    logger.info(
        "Route registered for driver id: {} with route_id:{} ", driverid, savedroute.getRouteId());
    map.put("route_id", savedroute.getRouteId());
    return ResponseEntity.ok(map);
  }

  @PatchMapping("/update-location")
  public ResponseEntity<?> updateLocation(@RequestBody UpdateLocationDTO updateLocationDTO) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Integer driverid = (Integer) auth.getDetails();

    Route r = routeRepository.findByRouteId(updateLocationDTO.getRoute_id());
    routeRepository.save(r); // updated in db
    String redisKey = "driver-service:driver:" + driverid + ":route";
    DriverDataRedis data = (DriverDataRedis) redisTemplate.opsForValue().get(redisKey);
    if (data != null) {
      data.setCurrentLocation(updateLocationDTO.getLocation());
      redisTemplate.opsForValue().set(redisKey, data);
    }
    // update the location in redis using the driver id
    logger.info(
        "Location updated for driver id: {} for route_id:{} to: {} ",
        driverid,
        updateLocationDTO.getRoute_id(),
        updateLocationDTO.getLocation());
    return ResponseEntity.status(HttpStatus.OK).body("Location Updated Successfully");
  }
}
