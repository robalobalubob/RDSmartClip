package com.example.rdsmartclipper;

//voltage, current, RPM, temp, roll, pitch, yaw, acceleration, time

public class DataPoint {
    public float voltage;
    public float temperature;
    public float rpm;
    public float current;
    public float time;
    public float roll;
    public float pitch;
    public float yaw;
    public float acceleration;

    public DataPoint(float voltage, float temperature, float rpm, float current, float time) {
        this.voltage = voltage;
        this.temperature = temperature;
        this.rpm = rpm;
        this.current = current;
        this.time = time;
    }

    public DataPoint(float voltage, float temperature, float rpm, float current, float roll, float pitch, float yaw, float acceleration, float time) {
        this.voltage = voltage;
        this.temperature = temperature;
        this.rpm = rpm;
        this.current = current;
        this.time = time;
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
        this.acceleration = acceleration;
    }
}
