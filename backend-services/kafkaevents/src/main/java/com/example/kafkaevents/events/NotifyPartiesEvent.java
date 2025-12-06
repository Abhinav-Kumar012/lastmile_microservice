package com.example.kafkaevents.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotifyPartiesEvent {
  private Integer riderId;
  private Integer driverId;
  private Integer tripId;
  private Integer arrivalId;
  private String arrivalstationname;
  private String vehiclenumber;
}
