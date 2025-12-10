package com.example.TripService.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trips")
@NoArgsConstructor
@Data
public class Trip {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "trip_id")
  private Integer tripId;

  @Column(name = "rider_id")
  private Integer riderId;

  @Column(name = "driver_id")
  private Integer driverId;

  @Column(name = "arrival_id")
  private Integer arrivalId;

  @Column(name = "status")
  private String status;

  @Column(name = "arrivalstationname")
  private String arrivalstationname;

  public Trip(Integer riderId, Integer driverId, Integer arrivalId, String arrivalstationname) {
    this.riderId = riderId;
    this.driverId = driverId;
    this.arrivalId = arrivalId;
    this.status = "SCHEDULED";
    this.arrivalstationname = arrivalstationname;
  }
}
