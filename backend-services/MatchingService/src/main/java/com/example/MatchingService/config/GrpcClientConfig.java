package com.example.MatchingService.config;

import com.example.locationService.grpc.StationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for gRPC client setup. Creates a ManagedChannel and a
 * StationServiceBlockingStub bean that can be injected anywhere in the Spring context.
 */
@Configuration
public class GrpcClientConfig {

  // 🔹 Inject values from application.properties
  @Value("${grpc.server.host:127.0.0.1}") // Default to localhost
  private String grpcServerHost;

  @Value("${grpc.server.port:9095}") // Default to 9091
  private int grpcServerPort;

  /** Create and configure the gRPC channel. Use plaintext() if the server does not use TLS. */
  @Bean
  public ManagedChannel stationServiceChannel() {
    return ManagedChannelBuilder.forAddress(grpcServerHost, grpcServerPort)
        .usePlaintext() // disable TLS for local/dev environments
        .build();
  }

  /**
   * Create a blocking stub for the StationService gRPC interface. This will be injected into your
   * client service.
   */
  @Bean
  @Primary
  public StationServiceGrpc.StationServiceBlockingStub stationServiceStub(ManagedChannel channel) {
    return StationServiceGrpc.newBlockingStub(channel);
  }
}
