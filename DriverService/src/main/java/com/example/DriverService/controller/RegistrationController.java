package com.example.DriverService.controller;

import com.example.DriverService.dtos.DriverDataRedis;
import com.example.DriverService.dtos.RegistrationDTO;
import com.example.DriverService.dtos.UpdateLocationDTO;
import com.example.DriverService.entity.Route;
import com.example.DriverService.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegistrationController {
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @PostMapping("/register-route")
    public ResponseEntity<Map<String,Integer>> registerRoute(@RequestBody RegistrationDTO registrationDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer driverid = (Integer) auth.getDetails();
        System.out.println("Driver ID from auth details: " + driverid);

        //We need to save save this route in the db and in the redis persist driver id as key and start location as value so that When the driver location updates we just update that enitty in rediss and we must also update currentlocation in db in case the entry from redis evicts
        Route r = new Route(driverid,registrationDTO.getStartinglocation(),registrationDTO.getDestination(),registrationDTO.getAvailable_seats(),registrationDTO.getStartinglocation());
        Route savedroute = routeRepository.save(r);
        //add the logic for adding the entry to the redis too
        String redisKey = "driver-service:driver:" + driverid + ":route";
        redisTemplate.opsForValue().set(redisKey, new DriverDataRedis(registrationDTO.getStartinglocation(),registrationDTO.getAvailable_seats(),registrationDTO.getDestination()));



        Map<String,Integer> map = new HashMap<>();
        map.put("ride_id",savedroute.getRouteId());
        return  ResponseEntity.ok(map);

    }
    @PatchMapping("/update-location")
    public ResponseEntity<?> updateLocation(@RequestBody UpdateLocationDTO updateLocationDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer driverid = (Integer) auth.getDetails();
        Route r = routeRepository.findByRouteId(updateLocationDTO.getRoute_id());
        r.setCurrent_location(updateLocationDTO.getLocation());
        routeRepository.save(r);//updated in db
        String redisKey = "driver-service:driver:" + driverid + ":route";
        DriverDataRedis  data = (DriverDataRedis) redisTemplate.opsForValue().get(redisKey);
        if(data!=null)
        {
            data.setCurrentLocation(updateLocationDTO.getLocation());
            redisTemplate.opsForValue().set(redisKey, data);
        }

        //update the location in redis using the driver id

        return ResponseEntity.status(HttpStatus.OK).body("Location Updated Successfully");
    }


}
