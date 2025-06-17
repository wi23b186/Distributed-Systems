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
        // Determine the current hour, truncating minutes/seconds/nanos
        LocalDateTime currentHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        // Generate a random community vs. grid percentage split
        double communityPool = ThreadLocalRandom.current().nextDouble(0, 100);
        double gridPortion = 100 - communityPool;

        // Save the current energy usage percentages
        currentPercentageRepository.save(new CurrentPercentage(
                currentHour,
                round(communityPool),
                round(gridPortion)
        ));

        // Generate historical data for each hour over the past 3 days
        LocalDateTime end = currentHour;
        LocalDateTime start = end.minusDays(3).withHour(0);

        // Loop through each hour from start to end
        for (LocalDateTime timestamp = start; !timestamp.isAfter(end); timestamp = timestamp.plusHours(1)) {

            // Generate random values for community production and usage
            double communityProduced = round(ThreadLocalRandom.current().nextDouble(100.0, 300.0));
            double hourKwh = generateKwh(timestamp.getHour());
            double totalUsed = round(hourKwh * 300);

            // Determine consumption vs. grid usage
            double communityUsed = Math.min(totalUsed, communityProduced);
            double gridUsed = totalUsed - communityUsed;

            // Persist the hourly historical record
            energyHistoryRepository.save(new EnergyHistory(
                    timestamp,
                    round(communityProduced),
                    round(communityUsed),
                    round(gridUsed)
            ));
        }
    }

    /**
     * Rounds a double to two decimal places.
     *
     * @param value the raw value to round
     * @return the value rounded to 2 decimal places
     */
    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Generates a randomized kWh factor based on the hour of day.
     *<p>
     * Peak hours (6–9, 17–21): 0.3 – 1.0 kWh;
     * daytime (10–16): 0.1 – 0.4 kWh;
     * night: 0.01 – 0.1 kWh.
     *
     * @param hour the hour of the day (0–23)
     * @return a kWh value rounded to 2 decimal places
     */
    private static double generateKwh(int hour) {
        // peak hours (6–9, 17–21): 0.3 – 1.0
        if ((hour >= 6 && hour <= 9) || (hour >= 17 && hour <= 21)) {
            return round(ThreadLocalRandom.current().nextDouble(0.3, 1.0));
        }
        // daytime (10–16): 0.1 – 0.4
        else if (hour >= 10 && hour <= 16) {
            return round(ThreadLocalRandom.current().nextDouble(0.1, 0.4));
        }
        // night (all other hours): 0.01 – 0.1
        else {
            return round(ThreadLocalRandom.current().nextDouble(0.01, 0.1));
        }
    }
}