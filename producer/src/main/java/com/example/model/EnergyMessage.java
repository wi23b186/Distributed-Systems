package com.example.model;

public class EnergyMessage {
    public String type;
    public String association;
    public double kwh;
    public String datetime;

    public EnergyMessage(String type, String association, double kwh, String datetime) {
        this.type = type;
        this.association = association;
        this.kwh = kwh;
        this.datetime = datetime;
    }
}
