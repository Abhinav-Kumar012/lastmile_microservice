package com.example.locationService.service;

import com.example.locationService.grpc.StationServiceGrpc.StationServiceImplBase;
import com.example.locationService.grpc.isNearby;
import com.example.locationService.grpc.stationNameAndLocation;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class LocationService extends StationServiceImplBase {
  @Autowired private stationServiceGrpcClient SSclient;
  private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

  @Override
  public void checkIfNearby(
      stationNameAndLocation request, StreamObserver<isNearby> responseObserver) {
    String stationNameStr = request.getStationName();
    String driverCurrLoc = request.getDriverLocation();
    ArrayList<String> nearbyLocations = SSclient.getAllNearbyAreas(stationNameStr);
    boolean ifNearby = false;
    if (nearbyLocations.contains(driverCurrLoc)) {
      ifNearby = true;
    }
    isNearby retval = isNearby.newBuilder().setNearby(ifNearby).build();
    logger.info("{} is nearby {} : {}", driverCurrLoc, stationNameStr, ifNearby);
    responseObserver.onNext(retval);
    responseObserver.onCompleted();
  }
}
