package com.fitnessplanner.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "diet_profiles")
public class DietProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String dietType; // e.g., "balanced", "low_carb", "vegetarian", "vegan"

    @Column(nullable = false) // You might reconsider nullable=false if Integer can be null and that's valid
    private Integer mealFrequency;

    @Column(nullable = false)
    private String primaryGoal; // e.g., "weight_loss", "weight_gain", "maintenance", "muscle_building_support"

    @ElementCollection(fetch = FetchType.EAGER) // Store as a collection of strings
    @CollectionTable(name = "diet_allergies", joinColumns = @JoinColumn(name = "diet_profile_id"))
    @Column(name = "allergy")
    private List<String> allergies; // e.g., "gluten", "dairy", "nuts"

    private boolean intermittentFasting; // true or false

    // Optional: target daily calories, macronutrient preferences (can be calculated or set by advanced users)
    private Integer targetCalories;

    @Column(nullable = false)
    private LocalDate createdDate;

    @Column
    private LocalDate updatedDate;

    @Column
    private String currentDietPlanId; // To store the ID of the matched diet plan

    // Constructors
    public DietProfile() {
        this.createdDate = LocalDate.now();
    }

    public DietProfile(User user, String dietType, int mealFrequency, String primaryGoal, List<String> allergies, boolean intermittentFasting) {
        this.user = user;
        this.dietType = dietType;
        this.mealFrequency = mealFrequency;
        this.primaryGoal = primaryGoal;
        this.allergies = allergies;
        this.intermittentFasting = intermittentFasting;
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

    public String getDietType() {
        return dietType;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }

    public Integer getMealFrequency() {
        return mealFrequency;
    }

    public void setMealFrequency(Integer mealFrequency) {
        this.mealFrequency = mealFrequency;
    }

    public String getPrimaryGoal() {
        return primaryGoal;
    }

    public void setPrimaryGoal(String primaryGoal) {
        this.primaryGoal = primaryGoal;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public boolean isIntermittentFasting() {
        return intermittentFasting;
    }

    public void setIntermittentFasting(boolean intermittentFasting) {
        this.intermittentFasting = intermittentFasting;
    }

    public Integer getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(Integer targetCalories) {
        this.targetCalories = targetCalories;
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

    public String getCurrentDietPlanId() {
        return currentDietPlanId;
    }

    public void setCurrentDietPlanId(String currentDietPlanId) {
        this.currentDietPlanId = currentDietPlanId;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "DietProfile{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", dietType='" + dietType + '\'' +
                ", mealFrequency=" + mealFrequency +
                ", primaryGoal='" + primaryGoal + '\'' +
                ", allergies=" + allergies +
                ", intermittentFasting=" + intermittentFasting +
                ", targetCalories=" + targetCalories +
                ", currentDietPlanId='" + currentDietPlanId + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}
