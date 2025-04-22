package com.example.api.repository;


import com.example.api.model.EnergyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface EnergyUsageRepository extends JpaRepository<EnergyUsage, LocalDateTime> {
    List<EnergyUsage> findAllByHourBetween(LocalDateTime start, LocalDateTime end);
}
