package com.example.kafkaevents.events;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiderDataRedis {
  private Integer arrivalId;
  private LocalDateTime arrivaltime;
  private String destination;
  private String arrivalstationname;
}
