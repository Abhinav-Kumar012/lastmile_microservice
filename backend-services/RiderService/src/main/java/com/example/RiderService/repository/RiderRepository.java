package com.example.RiderService.repository;

import com.example.RiderService.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiderRepository extends JpaRepository<Ride, Integer> {}
