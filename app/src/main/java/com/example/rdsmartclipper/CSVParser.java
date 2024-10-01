package com.example.rdsmartclipper;

import java.util.ArrayList;
import java.util.List;

//voltage, current, RPM, temp, roll, pitch, yaw, acceleration, time

/**
 * CSVParser class
 * Parses CSV data into a list of DataPoint objects
 */
public class CSVParser {
    /**
     * Parses CSV data into a list of DataPoint objects
     * @param data String of CSV data to be parsed
     * @return List of DataPoint objects
     */
    public List<DataPoint> parseCSV(String data) {
        // Establish data points
        List<DataPoint> dataPoints = new ArrayList<>();
        String[] rows = data.split("\n");

        // Parse each row
        for (String row : rows) {
            String[] columns = row.split(",");
            if (columns.length == 5) {
                try {
                    float voltage = Float.parseFloat(columns[0]);
                    float temperature = Float.parseFloat(columns[1]);
                    float rpm = Float.parseFloat(columns[2]);
                    float current = Float.parseFloat(columns[3]);
                    float time = Float.parseFloat(columns[4]);
                    dataPoints.add(new DataPoint(voltage, temperature, rpm, current, time));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing row: " + row + " - " + e.getMessage());
                }
            } else {
                System.err.println("Invalid row format: " + row);
            }
        }
        return dataPoints;
    }
}
