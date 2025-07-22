package com.fitnessplanner.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "workout_profiles")
public class WorkoutProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private double weight; // in kg or lbs, be consistent

    @Column(nullable = false)
    private String fitnessGoal; // e.g., "weight_loss", "muscle_gain", "general_fitness"

    @Column(nullable = false)
    private String activityLevel; // e.g., "sedentary", "lightly_active", "moderately_active", "very_active"

    // Potentially other fields: height, gender, preferred workout types, available equipment etc.

    @Column(nullable = false)
    private LocalDate createdDate;

    @Column
    private LocalDate updatedDate;

    @Column
    private String currentWorkoutPlanId; // To store the ID of the matched plan

    // Constructors
    public WorkoutProfile() {
        this.createdDate = LocalDate.now();
    }

    public WorkoutProfile(User user, int age, double weight, String fitnessGoal, String activityLevel) {
        this.user = user;
        this.age = age;
        this.weight = weight;
        this.fitnessGoal = fitnessGoal;
        this.activityLevel = activityLevel;
        this.createdDate = LocalDate.now();
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(String fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getCurrentWorkoutPlanId() {
        return currentWorkoutPlanId;
    }

    public void setCurrentWorkoutPlanId(String currentWorkoutPlanId) {
        this.currentWorkoutPlanId = currentWorkoutPlanId;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "WorkoutProfile{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", age=" + age +
                ", weight=" + weight +
                ", fitnessGoal='" + fitnessGoal + '\'' +
                ", activityLevel='" + activityLevel + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}
