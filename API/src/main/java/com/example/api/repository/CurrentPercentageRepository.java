package com.example.api.repository;

import com.example.api.model.CurrentPercentage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrentPercentageRepository extends JpaRepository<CurrentPercentage, Integer> {
    CurrentPercentage findTopByOrderByHourDesc();
}
