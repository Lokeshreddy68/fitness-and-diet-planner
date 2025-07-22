package com.fitnessplanner.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "meal_logs")
public class MealLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate logDate;

    private LocalTime logTime; // Optional: time of the meal

    @Column(nullable = false)
    private String mealType; // e.g., "Breakfast", "Lunch", "Dinner", "Snack"

    @Column(nullable = false, length = 500) // Food items can be a descriptive text
    private String foodItemOrMealDescription;

    private Integer calories; // Estimated calories for this log entry
    private Double proteinGrams;
    private Double carbohydrateGrams;
    private Double fatGrams;
    private Double fiberGrams; // Optional

    private Double servingSize;
    private String servingUnit; // e.g., "grams", "oz", "cup", "piece"

    // Link to the diet plan if this meal was based on it
    private String dietPlanId;

    @Column(length = 500)
    private String notes;

    // Constructors
    public MealLog() {
        this.logDate = LocalDate.now();
    }

    public MealLog(User user, LocalDate logDate, String mealType, String foodItemOrMealDescription) {
        this.user = user;
        this.logDate = logDate;
        this.mealType = mealType;
        this.foodItemOrMealDescription = foodItemOrMealDescription;
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

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
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

    public String getDietPlanId() {
        return dietPlanId;
    }

    public void setDietPlanId(String dietPlanId) {
        this.dietPlanId = dietPlanId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "MealLog{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", logDate=" + logDate +
                ", mealType='" + mealType + '\'' +
                ", foodItemOrMealDescription='" + foodItemOrMealDescription + '\'' +
                ", calories=" + calories +
                '}';
    }
}
