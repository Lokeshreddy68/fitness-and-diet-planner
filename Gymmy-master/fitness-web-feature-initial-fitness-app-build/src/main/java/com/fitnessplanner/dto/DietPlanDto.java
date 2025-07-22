package com.fitnessplanner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DietPlanDto {

    private String planId;
    private String planName;
    private String targetDietType;
    private String targetGoal;
    private String description;
    private Integer estimatedDailyCalories;
    private String macronutrientRatio; // e.g., "P:30,C:40,F:30"
    private boolean supportsFasting;
    private List<String> avoidAllergies; // List of allergies this plan is NOT suitable for
    private List<SampleMealDto> sampleMeals;

    // Constructors
    public DietPlanDto() {
    }

    public DietPlanDto(String planId, String planName, String targetDietType, String targetGoal, String description,
                       Integer estimatedDailyCalories, String macronutrientRatio, boolean supportsFasting,
                       List<String> avoidAllergies, List<SampleMealDto> sampleMeals) {
        this.planId = planId;
        this.planName = planName;
        this.targetDietType = targetDietType;
        this.targetGoal = targetGoal;
        this.description = description;
        this.estimatedDailyCalories = estimatedDailyCalories;
        this.macronutrientRatio = macronutrientRatio;
        this.supportsFasting = supportsFasting;
        this.avoidAllergies = avoidAllergies;
        this.sampleMeals = sampleMeals;
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

    public String getTargetDietType() {
        return targetDietType;
    }

    public void setTargetDietType(String targetDietType) {
        this.targetDietType = targetDietType;
    }

    public String getTargetGoal() {
        return targetGoal;
    }

    public void setTargetGoal(String targetGoal) {
        this.targetGoal = targetGoal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEstimatedDailyCalories() {
        return estimatedDailyCalories;
    }

    public void setEstimatedDailyCalories(Integer estimatedDailyCalories) {
        this.estimatedDailyCalories = estimatedDailyCalories;
    }

    public String getMacronutrientRatio() {
        return macronutrientRatio;
    }

    public void setMacronutrientRatio(String macronutrientRatio) {
        this.macronutrientRatio = macronutrientRatio;
    }

    public boolean isSupportsFasting() {
        return supportsFasting;
    }

    public void setSupportsFasting(boolean supportsFasting) {
        this.supportsFasting = supportsFasting;
    }

    public List<String> getAvoidAllergies() {
        return avoidAllergies;
    }

    public void setAvoidAllergies(List<String> avoidAllergies) {
        this.avoidAllergies = avoidAllergies;
    }

    public List<SampleMealDto> getSampleMeals() {
        return sampleMeals;
    }

    public void setSampleMeals(List<SampleMealDto> sampleMeals) {
        this.sampleMeals = sampleMeals;
    }

    // toString
    @Override
    public String toString() {
        return "DietPlanDto{" +
                "planId='" + planId + '\'' +
                ", planName='" + planName + '\'' +
                ", targetDietType='" + targetDietType + '\'' +
                ", targetGoal='" + targetGoal + '\'' +
                ", estimatedDailyCalories=" + estimatedDailyCalories +
                ", supportsFasting=" + supportsFasting +
                ", sampleMealsCount=" + (sampleMeals != null ? sampleMeals.size() : 0) +
                '}';
    }
}
