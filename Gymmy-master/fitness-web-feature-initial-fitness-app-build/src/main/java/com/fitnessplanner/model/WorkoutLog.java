package com.fitnessplanner.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "workout_logs")
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String exerciseName;

    @Column(nullable = false)
    private LocalDate workoutDate;

    private LocalTime workoutTime; // Optional: time of the workout

    // Details of the performed exercise
    private Integer setsPerformed; // Number of sets actually done
    private String repsPerformed;  // Reps per set, e.g., "10,9,8" or "12" if consistent
    private Double weightUsed;     // Weight used, e.g., in kg or lbs
    private String weightUnit;     // "kg", "lbs", "bodyweight"
    private Integer durationPerformedSeconds; // For time-based exercises like plank or cardio

    private Integer restTimeSeconds; // Actual rest time taken, if tracked

    // Link to the plan if this workout was part of a plan
    private String workoutPlanId; // ID from WorkoutPlanDto
    private String dailyWorkoutDayName; // e.g., "Monday" from DailyWorkoutDto

    @Column(length = 500)
    private String notes; // User's notes about the exercise or workout

    // Constructors
    public WorkoutLog() {
        this.workoutDate = LocalDate.now();
    }

    public WorkoutLog(User user, String exerciseName, LocalDate workoutDate) {
        this.user = user;
        this.exerciseName = exerciseName;
        this.workoutDate = workoutDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "WorkoutLog{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", exerciseName='" + exerciseName + '\'' +
                ", workoutDate=" + workoutDate +
                ", setsPerformed=" + setsPerformed +
                ", repsPerformed='" + repsPerformed + '\'' +
                ", weightUsed=" + weightUsed +
                '}';
    }
}
