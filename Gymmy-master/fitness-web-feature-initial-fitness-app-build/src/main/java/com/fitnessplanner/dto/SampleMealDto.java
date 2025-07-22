package com.fitnessplanner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleMealDto {

    private String mealName;
    private String description; // Optional description of the meal
    private List<String> exampleFoods;

    // Constructors
    public SampleMealDto() {
    }

    public SampleMealDto(String mealName, String description, List<String> exampleFoods) {
        this.mealName = mealName;
        this.description = description;
        this.exampleFoods = exampleFoods;
    }

    // Getters and Setters
    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getExampleFoods() {
        return exampleFoods;
    }

    public void setExampleFoods(List<String> exampleFoods) {
        this.exampleFoods = exampleFoods;
    }

    // toString
    @Override
    public String toString() {
        return "SampleMealDto{" +
                "mealName='" + mealName + '\'' +
                (description != null ? ", description='" + description + '\'' : "") +
                ", exampleFoods=" + exampleFoods +
                '}';
    }
}
