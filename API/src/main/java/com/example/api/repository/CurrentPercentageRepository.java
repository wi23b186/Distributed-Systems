package com.example.api.repository;

import com.example.api.model.CurrentPercentage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CurrentPercentageRepository extends JpaRepository<CurrentPercentage, Integer> {
    CurrentPercentage findTopByOrderByHourDesc();
    Optional<CurrentPercentage> findByHour(LocalDateTime hour);
}
