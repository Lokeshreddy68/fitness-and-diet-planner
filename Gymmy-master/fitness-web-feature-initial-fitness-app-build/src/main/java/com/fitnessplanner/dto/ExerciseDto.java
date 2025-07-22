package com.fitnessplanner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // Include only non-null fields in JSON output
public class ExerciseDto {

    private String name;
    private Integer sets; // Using Integer to allow null if not applicable (e.g. for cardio duration)
    private String reps; // String to accommodate ranges like "8-12" or "AMRAP"
    private Integer restSeconds;
    private Integer durationSeconds; // For exercises like Plank
    private Integer durationMinutes; // For cardio exercises

    // Constructors
    public ExerciseDto() {
    }

    public ExerciseDto(String name, Integer sets, String reps, Integer restSeconds, Integer durationSeconds, Integer durationMinutes) {
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.restSeconds = restSeconds;
        this.durationSeconds = durationSeconds;
        this.durationMinutes = durationMinutes;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSets() {
        return sets;
    }

    public void setSets(Integer sets) {
        this.sets = sets;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public Integer getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(Integer restSeconds) {
        this.restSeconds = restSeconds;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    // toString
    @Override
    public String toString() {
        return "ExerciseDto{" +
                "name='" + name + '\'' +
                (sets != null ? ", sets=" + sets : "") +
                (reps != null ? ", reps='" + reps + '\'' : "") +
                (restSeconds != null ? ", restSeconds=" + restSeconds : "") +
                (durationSeconds != null ? ", durationSeconds=" + durationSeconds : "") +
                (durationMinutes != null ? ", durationMinutes=" + durationMinutes : "") +
                '}';
    }
}
