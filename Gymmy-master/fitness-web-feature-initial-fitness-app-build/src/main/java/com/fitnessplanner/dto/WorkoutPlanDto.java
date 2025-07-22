package com.fitnessplanner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutPlanDto {

    private String planId;
    private String planName;
    private String targetGoal; // e.g., "weight_loss", "muscle_gain"
    private List<String> targetActivityLevel; // e.g., ["sedentary", "lightly_active"]
    private String description;
    private Integer durationWeeks;
    private List<DailyWorkoutDto> weeklySchedule;

    // Constructors
    public WorkoutPlanDto() {
    }

    public WorkoutPlanDto(String planId, String planName, String targetGoal, List<String> targetActivityLevel, String description, Integer durationWeeks, List<DailyWorkoutDto> weeklySchedule) {
        this.planId = planId;
        this.planName = planName;
        this.targetGoal = targetGoal;
        this.targetActivityLevel = targetActivityLevel;
        this.description = description;
        this.durationWeeks = durationWeeks;
        this.weeklySchedule = weeklySchedule;
    }

    // Getters and Setters
    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getTargetGoal() {
        return targetGoal;
    }

    public void setTargetGoal(String targetGoal) {
        this.targetGoal = targetGoal;
    }

    public List<String> getTargetActivityLevel() {
        return targetActivityLevel;
    }

    public void setTargetActivityLevel(List<String> targetActivityLevel) {
        this.targetActivityLevel = targetActivityLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(Integer durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public List<DailyWorkoutDto> getWeeklySchedule() {
        return weeklySchedule;
    }

    public void setWeeklySchedule(List<DailyWorkoutDto> weeklySchedule) {
        this.weeklySchedule = weeklySchedule;
    }

    // toString
    @Override
    public String toString() {
        return "WorkoutPlanDto{" +
                "planId='" + planId + '\'' +
                ", planName='" + planName + '\'' +
                ", targetGoal='" + targetGoal + '\'' +
                ", targetActivityLevel=" + targetActivityLevel +
                ", description='" + description + '\'' +
                ", durationWeeks=" + durationWeeks +
                ", weeklyScheduleCount=" + (weeklySchedule != null ? weeklySchedule.size() : 0) +
                '}';
    }
}
