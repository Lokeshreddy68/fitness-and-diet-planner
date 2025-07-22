package com.fitnessplanner.dto;

import java.util.ArrayList;
import java.util.List;

public class WorkoutLogFormDto {

    private List<WorkoutLogEntryDto> logEntries;

    public WorkoutLogFormDto() {
        this.logEntries = new ArrayList<>();
        // Initialize with one empty entry for the form to display
        // this.logEntries.add(new WorkoutLogEntryDto());
    }

    public WorkoutLogFormDto(List<WorkoutLogEntryDto> logEntries) {
        this.logEntries = logEntries;
    }

    public List<WorkoutLogEntryDto> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<WorkoutLogEntryDto> logEntries) {
        this.logEntries = logEntries;
    }

    public void addLogEntry(WorkoutLogEntryDto entryDto) {
        if (this.logEntries == null) {
            this.logEntries = new ArrayList<>();
        }
        this.logEntries.add(entryDto);
    }
}
