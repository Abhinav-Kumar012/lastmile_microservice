package com.example.DriverService.repository;

import com.example.DriverService.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route,Integer> {
    Route findByRouteId(Integer route_id);

}
