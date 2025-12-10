package com.example.locationService.service;

import com.lastmile.grpc.StationName;
import com.lastmile.grpc.StationServiceGrpc.StationServiceBlockingStub;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import org.springframework.grpc.client.*;

@Service
public class stationServiceGrpcClient {

  @Autowired private StationServiceBlockingStub stub;

  public ArrayList<String> getAllNearbyAreas(String Station) {
    StationName args = StationName.newBuilder().setName(Station).build();
    return new ArrayList<>(this.stub.getNearbystations(args).getNearbyPlacesList());
  }
}
