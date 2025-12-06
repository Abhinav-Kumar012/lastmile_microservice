package com.example.kafkaevents.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class TripConfirmedEvent {
  private Integer riderId;
  private Integer driverId;
}
