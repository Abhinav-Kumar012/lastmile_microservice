package com.example.DriverService.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverDataRedis {
    private String currentLocation;
    private Integer availableSeats;
    private String destination;
}
