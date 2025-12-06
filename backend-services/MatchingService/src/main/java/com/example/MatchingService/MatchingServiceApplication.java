package com.example.MatchingService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@ImportGrpcClients(basePackages = {"com.example.locationService.grpc"})
@SpringBootApplication
@EnableScheduling
public class MatchingServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(MatchingServiceApplication.class, args);
  }
}
