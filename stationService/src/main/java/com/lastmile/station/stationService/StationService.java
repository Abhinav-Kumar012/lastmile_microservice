package com.lastmile.station.stationService;

import com.lastmile.grpc.NeabyAreas;
import com.lastmile.grpc.StationName;
import com.lastmile.grpc.StationServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class StationService extends StationServiceGrpc.StationServiceImplBase {

    private final RedisTemplate<String, String> redisObj;

    @Autowired
    public StationService(RedisTemplate<String, String> redisObj) {
        this.redisObj = redisObj;
    }

    @Override
    public void getNearbystations(StationName request, StreamObserver<NeabyAreas> responseObserver) {
        String stationName = request.getName();
        var nearbyPlacesList = this.redisObj.opsForList().range(stationName,0,-1);
        NeabyAreas areas = NeabyAreas.newBuilder()
                .addAllNearbyPlaces(nearbyPlacesList)
                .build();
        responseObserver.onNext(areas);
        responseObserver.onCompleted();
    }
}
