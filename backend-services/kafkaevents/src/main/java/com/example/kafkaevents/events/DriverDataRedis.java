package com.example.kafkaevents.events;

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
  private String vehicleNumber;
}
