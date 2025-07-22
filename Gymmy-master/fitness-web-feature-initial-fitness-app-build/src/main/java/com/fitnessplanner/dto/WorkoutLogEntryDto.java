package com.fitnessplanner.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class WorkoutLogEntryDto {

    private Long id; // For updating existing logs, if needed in future
    private String exerciseName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate workoutDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime workoutTime;

    private Integer setsPerformed;
    private String repsPerformed; // e.g., "10,9,8" or "12"
    private Double weightUsed;
    private String weightUnit; // "kg", "lbs", "bodyweight"
    private Integer durationPerformedSeconds;
    private Integer restTimeSeconds;
    private String notes;

    // To link back to the plan if this exercise was from a plan
    private String workoutPlanId;
    private String dailyWorkoutDayName;


    // Constructors
    public WorkoutLogEntryDto() {
        this.workoutDate = LocalDate.now(); // Default to today
        this.weightUnit = "kg"; // Default unit
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public LocalDate getWorkoutDate() {
        return workoutDate;
    }

    public void setWorkoutDate(LocalDate workoutDate) {
        this.workoutDate = workoutDate;
    }

    public LocalTime getWorkoutTime() {
        return workoutTime;
    }

    public void setWorkoutTime(LocalTime workoutTime) {
        this.workoutTime = workoutTime;
    }

    public Integer getSetsPerformed() {
        return setsPerformed;
    }

    public void setSetsPerformed(Integer setsPerformed) {
        this.setsPerformed = setsPerformed;
    }

    public String getRepsPerformed() {
        return repsPerformed;
    }

    public void setRepsPerformed(String repsPerformed) {
        this.repsPerformed = repsPerformed;
    }

    public Double getWeightUsed() {
        return weightUsed;
    }

    public void setWeightUsed(Double weightUsed) {
        this.weightUsed = weightUsed;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public Integer getDurationPerformedSeconds() {
        return durationPerformedSeconds;
    }

    public void setDurationPerformedSeconds(Integer durationPerformedSeconds) {
        this.durationPerformedSeconds = durationPerformedSeconds;
    }

    public Integer getRestTimeSeconds() {
        return restTimeSeconds;
    }

    public void setRestTimeSeconds(Integer restTimeSeconds) {
        this.restTimeSeconds = restTimeSeconds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getWorkoutPlanId() {
        return workoutPlanId;
    }

    public void setWorkoutPlanId(String workoutPlanId) {
        this.workoutPlanId = workoutPlanId;
    }

    public String getDailyWorkoutDayName() {
        return dailyWorkoutDayName;
    }

    public void setDailyWorkoutDayName(String dailyWorkoutDayName) {
        this.dailyWorkoutDayName = dailyWorkoutDayName;
    }
}
