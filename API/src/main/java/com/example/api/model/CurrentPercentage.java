package com.example.api.model;


import jakarta.persistence.Id;

import java.time.LocalDateTime;


//@Entity
public class CurrentPercentage {

    @Id
    private LocalDateTime hour;
    private double communityDepleted;
    private double gridPortion;

    // Getters and Setters
    public LocalDateTime getHour() {
        return hour;
    }

    public void setHour(LocalDateTime hour) {
        this.hour = hour;
    }

    public double getCommunityDepleted() {
        return communityDepleted;
    }

    public void setCommunityDepleted(double communityDepleted) {
        this.communityDepleted = communityDepleted;
    }

    public double getGridPortion() {
        return gridPortion;
    }

    public void setGridPortion(double gridPortion) {
        this.gridPortion = gridPortion;
    }
}
