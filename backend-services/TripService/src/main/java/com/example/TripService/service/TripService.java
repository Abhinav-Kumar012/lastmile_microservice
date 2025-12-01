package com.example.TripService.service;


import com.example.TripService.entity.Trip;
import com.example.TripService.repository.TripRepository;
import com.example.kafkaevents.events.MatchingEvent;
import com.example.kafkaevents.events.NotifyPartiesEvent;
import com.example.kafkaevents.events.UpdateStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(TripService.class);
    @KafkaListener(topics="trip-service")
    public void listen(MatchingEvent matchingEvent, Acknowledgment ack)
    {
        System.out.println("Received Matching Event: " + matchingEvent);
        Trip t = new Trip(matchingEvent.getRiderId(), matchingEvent.getDriverId(), matchingEvent.getArrivalId(), matchingEvent.getArrivalstationname());
        Trip databaseTrip = tripRepository.save(t);
        kafkaTemplate.send("rider-service", new UpdateStatusEvent(matchingEvent.getArrivalId(), "SCHEDULED"));
        kafkaTemplate.send("notification-service",new NotifyPartiesEvent(databaseTrip.getRiderId(),databaseTrip.getDriverId(),databaseTrip.getTripId(), matchingEvent.getArrivalId(), matchingEvent.getArrivalstationname(), matchingEvent.getVehiclenumber()));
        log.info("Trip with ID: {} Scheduled" , databaseTrip.getTripId());
        ack.acknowledge();
    }



}
