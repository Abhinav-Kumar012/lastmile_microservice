package com.example.RiderService.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rides")
@Data
@NoArgsConstructor
public class Ride {
  @Id
  @Column(name = "arrival_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer arrivalId;

  @Column(name = "rider_id")
  private Integer riderid;

  private LocalDateTime arrivaltime;
  private String destination;
  private String arrivalstationname;
  private String status;

  public Ride(
      Integer riderid, LocalDateTime arrivaltime, String destination, String arrivalstationname) {
    this.riderid = riderid;
    this.arrivaltime = arrivaltime;
    this.destination = destination;
    this.arrivalstationname = arrivalstationname;
    this.status = "PENDING";
  }
}
