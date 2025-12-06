package com.example.DriverService.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationDTO {
  private String startinglocation;
  private String destination;
  private Integer available_seats;
  private String vehiclenumber;
}
