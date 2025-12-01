package com.example.DriverService.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name="routes")
@Data
@NoArgsConstructor
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="route_id")
    private Integer routeId;

    @Column(name="driver_id")
    private Integer driverId;

    @Column(name="starting_location")
    private String startinglocation;

    @Column(name="destination")
    private String destination;
//    @Column(name="available_seats")
//    private Integer available_seats;
//    @Column(name="current_location")
//    private String current_location;
    @Column(name="vehicle_number")
    private  String vehicle_number;
    public Route(Integer driver_id, String startinglocation, String destination,String vehicle_number) {
        this.driverId = driver_id;
        this.startinglocation = startinglocation;
        this.destination = destination;
//        this.available_seats = available_seats;
//        this.current_location = startinglocation;
        this.vehicle_number = vehicle_number;
    }
}
