package com.example.TripService.service;

import com.example.TripService.dtos.MatchingEvent;
import com.example.TripService.dtos.UpdateStatusEvent;
import com.example.TripService.entity.Trip;
import com.example.TripService.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class TripService {
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private KafkaTemplate<String, UpdateStatusEvent> kafkaTemplate;

    @KafkaListener(topics="trip-service")
    public void listen(MatchingEvent matchingEvent, Acknowledgment ack)
    {
        Trip t = new Trip(matchingEvent.getRiderId(), matchingEvent.getDriverId(), matchingEvent.getArrivalId());
        tripRepository.save(t);
        kafkaTemplate.send("rider-service", new UpdateStatusEvent(matchingEvent.getArrivalId(), "SCHEDULED"));
        ack.acknowledge();
    }



}
