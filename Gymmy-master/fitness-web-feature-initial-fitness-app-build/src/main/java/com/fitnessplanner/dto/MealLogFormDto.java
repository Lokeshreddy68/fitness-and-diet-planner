package com.fitnessplanner.dto;

import java.util.ArrayList;
import java.util.List;

public class MealLogFormDto {

    private List<MealLogEntryDto> logEntries;

    public MealLogFormDto() {
        this.logEntries = new ArrayList<>();
    }

    public MealLogFormDto(List<MealLogEntryDto> logEntries) {
        this.logEntries = logEntries;
    }

    public List<MealLogEntryDto> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<MealLogEntryDto> logEntries) {
        this.logEntries = logEntries;
    }

    public void addLogEntry(MealLogEntryDto entryDto) {
        if (this.logEntries == null) {
            this.logEntries = new ArrayList<>();
        }
        this.logEntries.add(entryDto);
    }
}
