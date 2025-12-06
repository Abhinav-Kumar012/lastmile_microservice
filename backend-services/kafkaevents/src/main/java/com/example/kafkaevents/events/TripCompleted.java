package com.example.kafkaevents.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripCompleted {
  private Integer riderId;
  private Integer arrivalId;
}
