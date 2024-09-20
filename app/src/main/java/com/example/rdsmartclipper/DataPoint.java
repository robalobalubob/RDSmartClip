package com.example.rdsmartclipper;

public class DataPoint {
    public float voltage;
    public float temperature;
    public float rpm;
    public float current;
    public float time;

    public DataPoint(float voltage, float temperature, float rpm, float current, float time) {
        this.voltage = voltage;
        this.temperature = temperature;
        this.rpm = rpm;
        this.current = current;
        this.time = time;
    }
}
