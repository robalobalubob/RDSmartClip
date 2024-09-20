package com.example.rdsmartclipper;

import java.util.ArrayList;
import java.util.List;

public class CSVParser {
    public List<DataPoint> parseCSV(String data) {
        List<DataPoint> dataPoints = new ArrayList<>();
        String[] rows = data.split("\n");

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
                    throw new RuntimeException(e);
                }
            }
        }
        return dataPoints;
    }
}
