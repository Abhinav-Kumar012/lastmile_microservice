package com.example.RiderService.dtos;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArrivalRegistrationDTO {
  private LocalDateTime arrivaltime;
  private String destination;
  private String arrivalstationname;
}
