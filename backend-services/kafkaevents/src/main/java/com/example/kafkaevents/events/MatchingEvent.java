package com.example.kafkaevents.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchingEvent {
  private Integer riderId;
  private Integer driverId;
  private Integer arrivalId;
  private String arrivalstationname;
  private String vehiclenumber;
}
