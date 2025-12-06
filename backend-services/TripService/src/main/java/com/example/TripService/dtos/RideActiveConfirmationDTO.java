package com.example.TripService.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideActiveConfirmationDTO {
  Integer tripId;
  Integer arrivalId;
}
