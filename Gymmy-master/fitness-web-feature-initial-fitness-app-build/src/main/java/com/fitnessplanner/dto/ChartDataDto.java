package com.fitnessplanner.dto;

import java.util.List;
import java.util.Map;

/**
 * A generic DTO to hold data for chart rendering.
 * It can be adapted for various chart types.
 */
public class ChartDataDto {

    private List<String> labels; // e.g., ["Week 1", "Week 2", "Week 3", "Week 4"] or dates
    private List<DatasetDto> datasets;

    public ChartDataDto() {
    }

    public ChartDataDto(List<String> labels, List<DatasetDto> datasets) {
        this.labels = labels;
        this.datasets = datasets;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<DatasetDto> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DatasetDto> datasets) {
        this.datasets = datasets;
    }

    /**
     * Represents a single dataset in a chart (e.g., a line in a line chart, bars in a bar chart).
     */
    public static class DatasetDto {
        private String label; // Name of the dataset (e.g., "Workout Count", "Avg Calories")
        private List<Number> data; // Numerical data points
        private String borderColor; // Optional: for styling
        private String backgroundColor; // Optional: for styling
        private boolean fill = false; // Optional: for area charts under lines

        public DatasetDto() {
        }

        public DatasetDto(String label, List<Number> data) {
            this.label = label;
            this.data = data;
        }

        // Getters and Setters
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<Number> getData() {
            return data;
        }

        public void setData(List<Number> data) {
            this.data = data;
        }

        public String getBorderColor() {
            return borderColor;
        }

        public void setBorderColor(String borderColor) {
            this.borderColor = borderColor;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public boolean isFill() {
            return fill;
        }

        public void setFill(boolean fill) {
            this.fill = fill;
        }
    }
}
