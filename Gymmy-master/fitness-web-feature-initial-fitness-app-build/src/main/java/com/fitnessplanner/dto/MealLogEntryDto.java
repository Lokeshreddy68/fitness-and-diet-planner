package com.fitnessplanner.dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public class MealLogEntryDto {

    private Long id; // For updates
    private String mealType; // Breakfast, Lunch, Dinner, Snack

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate logDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime logTime;

    private String foodItemOrMealDescription;
    private Integer calories;
    private Double proteinGrams;
    private Double carbohydrateGrams;
    private Double fatGrams;
    private Double fiberGrams;
    private Double servingSize;
    private String servingUnit;
    private String notes;
    private String dietPlanId; // If linked to a plan

    public MealLogEntryDto() {
        this.logDate = LocalDate.now(); // Default to today
        this.mealType = "Snack"; // Default type
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public LocalTime getLogTime() {
        return logTime;
    }

    public void setLogTime(LocalTime logTime) {
        this.logTime = logTime;
    }

    public String getFoodItemOrMealDescription() {
        return foodItemOrMealDescription;
    }

    public void setFoodItemOrMealDescription(String foodItemOrMealDescription) {
        this.foodItemOrMealDescription = foodItemOrMealDescription;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Double getProteinGrams() {
        return proteinGrams;
    }

    public void setProteinGrams(Double proteinGrams) {
        this.proteinGrams = proteinGrams;
    }

    public Double getCarbohydrateGrams() {
        return carbohydrateGrams;
    }

    public void setCarbohydrateGrams(Double carbohydrateGrams) {
        this.carbohydrateGrams = carbohydrateGrams;
    }

    public Double getFatGrams() {
        return fatGrams;
    }

    public void setFatGrams(Double fatGrams) {
        this.fatGrams = fatGrams;
    }

    public Double getFiberGrams() {
        return fiberGrams;
    }

    public void setFiberGrams(Double fiberGrams) {
        this.fiberGrams = fiberGrams;
    }

    public Double getServingSize() {
        return servingSize;
    }

    public void setServingSize(Double servingSize) {
        this.servingSize = servingSize;
    }

    public String getServingUnit() {
        return servingUnit;
    }

    public void setServingUnit(String servingUnit) {
        this.servingUnit = servingUnit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDietPlanId() {
        return dietPlanId;
    }

    public void setDietPlanId(String dietPlanId) {
        this.dietPlanId = dietPlanId;
    }
}
