package com.example.locationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;

@ImportGrpcClients(basePackages = {"com.lastmile.grpc"})
@SpringBootApplication
public class LocationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(LocationServiceApplication.class, args);
  }
}
