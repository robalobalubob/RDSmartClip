package com.example.rdsmartclipper;

//voltage, current, RPM, temp, roll, pitch, yaw, acceleration, time

/**
 * DataPoint class
 * Contains all data points for the given time
 * TODO: Properly format class with getters and setters and private the individual points
 */
public class DataPoint {
    public final float voltage;
    public final float temperature;
    public final float rpm;
    public final float current;
    public final float time;
    public float roll;
    public float pitch;
    public float yaw;
    public float acceleration;

    /**
     * Constructor for DataPoint without all data
     *
     * @param voltage     voltage data
     * @param temperature temperature data
     * @param rpm         rpm data
     * @param current     current data
     * @param time        The time for the data point
     */
    public DataPoint(float voltage, float temperature, float rpm, float current, float time) {
        this.voltage = voltage;
        this.temperature = temperature;
        this.rpm = rpm;
        this.current = current;
        this.time = time;
    }

    /**
     * Constructor for DataPoint with all data
     *
     * @param voltage      voltage data
     * @param temperature  temperature data
     * @param rpm          rpm data
     * @param current      current data
     * @param roll         roll data
     * @param pitch        pitch data
     * @param yaw          yaw data
     * @param acceleration acceleration data
     * @param time         The time for the data point
     */
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
