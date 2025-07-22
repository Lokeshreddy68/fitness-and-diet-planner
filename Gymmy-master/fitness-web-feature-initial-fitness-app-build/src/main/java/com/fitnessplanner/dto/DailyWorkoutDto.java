package com.fitnessplanner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyWorkoutDto {

    private String day; // e.g., "Monday", "Day 1"
    private String workoutType; // e.g., "Full Body Strength", "Cardio", "Rest"
    private String description; // Optional description for the day's workout
    private List<ExerciseDto> exercises;

    // Constructors
    public DailyWorkoutDto() {
    }

    public DailyWorkoutDto(String day, String workoutType, String description, List<ExerciseDto> exercises) {
        this.day = day;
        this.workoutType = workoutType;
        this.description = description;
        this.exercises = exercises;
    }

    // Getters and Setters
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ExerciseDto> getExercises() {
        return exercises;
    }

    public void setExercises(List<ExerciseDto> exercises) {
        this.exercises = exercises;
    }

    // toString
    @Override
    public String toString() {
        return "DailyWorkoutDto{" +
                "day='" + day + '\'' +
                ", workoutType='" + workoutType + '\'' +
                (description != null ? ", description='" + description + '\'' : "") +
                ", exercises=" + exercises +
                '}';
    }
}
