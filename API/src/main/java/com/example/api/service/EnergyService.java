package com.example.api.service;

import com.example.api.model.CurrentPercentage;
import com.example.api.model.EnergyUsage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnergyService {

    private final List<EnergyUsage> receivedUsages = new ArrayList<>();

    public void addEnergyUsage(EnergyUsage usage) {
        receivedUsages.add(usage);
    }

    public CurrentPercentage getCurrentPercentage() {
        CurrentPercentage cp = new CurrentPercentage();
        cp.setHour(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0));
        //make this random only two decimal places


        //two decimal places
        cp.setCommunityDepleted(Math.round(Math.random() * 100) );

        //make this random
        cp.setGridPortion(Math.round(Math.random() * 100));
        return cp;
    }

    public List<EnergyUsage> getHistoricalUsage(LocalDateTime start, LocalDateTime end) {
        List<EnergyUsage> result = new ArrayList<>();
        for (EnergyUsage usage : receivedUsages) {
            if (!usage.getHour().isBefore(start) && !usage.getHour().isAfter(end)) {
                result.add(usage);
            }
        }
        return result;
    }
}
