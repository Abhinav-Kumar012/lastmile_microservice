package com.example.MatchingService.config;

import com.example.locationService.grpc.StationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GrpcClientConfig {

  @Value("${grpc.server.host:127.0.0.1}")
  private String grpcServerHost;

  @Value("${grpc.server.port:9095}")
  private int grpcServerPort;

  @Bean
  public ManagedChannel stationServiceChannel() {
    return ManagedChannelBuilder.forAddress(grpcServerHost, grpcServerPort)
        .usePlaintext() // disable TLS for local/dev environments
        .build();
  }

  @Bean
  @Primary
  public StationServiceGrpc.StationServiceBlockingStub stationServiceStub(ManagedChannel channel) {
    return StationServiceGrpc.newBlockingStub(channel);
  }
}
