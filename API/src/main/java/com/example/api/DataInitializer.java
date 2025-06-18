package com.example.api;

import com.example.api.model.CurrentPercentage;
import com.example.api.model.EnergyHistory;
import com.example.api.repository.CurrentPercentageRepository;
import com.example.api.repository.EnergyHistoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataInitializer {

    private final CurrentPercentageRepository currentPercentageRepository;
    private final EnergyHistoryRepository energyHistoryRepository;

    public DataInitializer(CurrentPercentageRepository currentPercentageRepository,
                            EnergyHistoryRepository energyHistoryRepository) {
        this.currentPercentageRepository = currentPercentageRepository;
        this.energyHistoryRepository = energyHistoryRepository;
    }

    @PostConstruct
    public void initData() {
        LocalDateTime currentHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        double communityPool = ThreadLocalRandom.current().nextDouble(0, 100);
        double gridPortion = 100 - communityPool;

        currentPercentageRepository.save(new CurrentPercentage(
                currentHour,
                round(communityPool),
                round(gridPortion)
        ));

        LocalDateTime end = currentHour;
        LocalDateTime start = end.minusDays(14).withHour(0);

        for (LocalDateTime timestamp = start; !timestamp.isAfter(end); timestamp = timestamp.plusHours(1)) {

            double communityProduced = round(ThreadLocalRandom.current().nextDouble(100.0, 300.0));
            double hourKwh = generateKwh(timestamp.getHour());
            double totalUsed = round(hourKwh * 300);

            double communityUsed = Math.min(totalUsed, communityProduced);
            double gridUsed = totalUsed - communityUsed;

            energyHistoryRepository.save(new EnergyHistory(
                    timestamp,
                    round(communityProduced),
                    round(communityUsed),
                    round(gridUsed)
            ));
        }
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double generateKwh(int hour) {
        if ((hour >= 6 && hour <= 8) || (hour >= 18 && hour <= 22)) {
            return ThreadLocalRandom.current().nextDouble(1.0, 2.0);
        }
        else if (hour >= 9 && hour <= 17) {
            return ThreadLocalRandom.current().nextDouble(0.1, 0.4);
        }
        else {
            return ThreadLocalRandom.current().nextDouble(0.01, 0.1);
        }
    }
}