package com.example.api.controller;

import com.example.api.model.CurrentPercentage;
import com.example.api.model.EnergyUsage;
import com.example.api.service.EnergyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    @Autowired
    private EnergyService energyService;

    @GetMapping("/current")
    public ResponseEntity<?> getCurrent() {
        CurrentPercentage cp = energyService.getCurrentPercentage();
        if (cp != null) {
            return ResponseEntity.ok(cp);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }
    @PostMapping("/usage")
    public ResponseEntity<Void> receiveEnergyUsage(@RequestBody EnergyUsage usage) {
        energyService.addEnergyUsage(usage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
        //make feedback to the user
        //return ResponseEntity.ok("Usage received");
        //return ResponseEntity.status(HttpStatus.CREATED).body("Usage received");
    }


    @GetMapping("/historical")
    public ResponseEntity<List<EnergyUsage>> getHistorical(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return ResponseEntity.ok(energyService.getHistoricalUsage(start, end));
    }
}
