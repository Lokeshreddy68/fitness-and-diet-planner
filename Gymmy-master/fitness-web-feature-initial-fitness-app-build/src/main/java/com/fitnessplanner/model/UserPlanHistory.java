package com.fitnessplanner.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_plan_history")
public class UserPlanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String planId; // The ID of the workout or diet plan (e.g., "WL_BEGINNER_001", "BAL_WL_001")

    @Column(nullable = false)
    private String planName; // The name of the plan

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType; // Indicates if it's a WORKOUT or DIET plan

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate; // Null if the plan is currently active or was never formally ended

    private String reasonForEnding; // Optional: e.g., "completed", "changed_by_user", "system_suggested_change"

    // Constructors
    public UserPlanHistory() {
    }

    public UserPlanHistory(User user, String planId, String planName, PlanType planType, LocalDate startDate) {
        this.user = user;
        this.planId = planId;
        this.planName = planName;
        this.planType = planType;
        this.startDate = startDate;
    }

    // Enum for Plan Type
    public enum PlanType {
        WORKOUT, DIET
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

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReasonForEnding() {
        return reasonForEnding;
    }

    public void setReasonForEnding(String reasonForEnding) {
        this.reasonForEnding = reasonForEnding;
    }

    @Override
    public String toString() {
        return "UserPlanHistory{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", planId='" + planId + '\'' +
                ", planName='" + planName + '\'' +
                ", planType=" + planType +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
