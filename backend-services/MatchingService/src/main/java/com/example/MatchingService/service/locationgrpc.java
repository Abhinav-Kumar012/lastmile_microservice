package com.example.MatchingService.service;

import com.example.locationService.grpc.StationServiceGrpc.StationServiceBlockingStub;
import com.example.locationService.grpc.stationNameAndLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class locationgrpc {
  @Autowired private StationServiceBlockingStub stub;

  public boolean isStationNearby(String stationName, String currentDriverLocation) {
    stationNameAndLocation args =
        stationNameAndLocation
            .newBuilder()
            .setDriverLocation(currentDriverLocation)
            .setStationName(stationName)
            .build();
    return this.stub.checkIfNearby(args).getNearby();
  }
}
