package com.example.api.controller;

import com.example.api.model.CurrentPercentage;
import com.example.api.model.EnergyHistory;
import com.example.api.repository.CurrentPercentageRepository;
import com.example.api.repository.EnergyHistoryRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final CurrentPercentageRepository currentPercentageRepository;
    private final EnergyHistoryRepository energyHistoryRepository;

    public EnergyController(CurrentPercentageRepository currentPercentageRepository,
                            EnergyHistoryRepository energyHistoryRepository) {
        this.currentPercentageRepository = currentPercentageRepository;
        this.energyHistoryRepository = energyHistoryRepository;
    }

    @GetMapping("/current")
    public CurrentPercentage currentPercentage() {
        return currentPercentageRepository.findTopByOrderByHourDesc();
    }

    @GetMapping("/historical")
    public EnergyHistory historyEnergy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        List<Object[]> results = energyHistoryRepository.sumHistoricalByDateRange(start, end);

        Object[] result = results.get(0);
        double communityProduced = result[0] != null ? ((Number) result[0]).doubleValue() : 0.0;
        double communityUsed     = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
        double gridUsed          = result[2] != null ? ((Number) result[2]).doubleValue() : 0.0;

        // Construct and return a new EnergyHistorical DTO with the summed values
        return new EnergyHistory(start, communityProduced, communityUsed, gridUsed);
    }
}
