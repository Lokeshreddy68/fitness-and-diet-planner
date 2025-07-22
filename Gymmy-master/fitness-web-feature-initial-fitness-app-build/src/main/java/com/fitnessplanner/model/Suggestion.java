package com.fitnessplanner.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "suggestions")
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Lob // For potentially longer messages
    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionType suggestionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private LocalDate createdDate;

    private boolean acknowledged; // Has the user seen/acknowledged this suggestion?
    private LocalDate acknowledgedDate;

    // Enum for Suggestion Type
    public enum SuggestionType {
        WORKOUT_PLAN_CHANGE,
        DIET_PLAN_CHANGE,
        WORKOUT_ADJUSTMENT, // e.g. increase weight, change reps
        DIET_ADJUSTMENT,    // e.g. increase/decrease calories, adjust macros
        MOTIVATION,
        WORKOUT_TRACKING_REMINDER,
        DIET_TRACKING_REMINDER,
        GENERAL_ADVICE
    }

    // Enum for Severity
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // Constructors
    public Suggestion() {
        this.createdDate = LocalDate.now();
        this.acknowledged = false;
    }

    public Suggestion(User user, String title, String message, SuggestionType suggestionType, Severity severity, LocalDate createdDate) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.suggestionType = suggestionType;
        this.severity = severity;
        this.createdDate = createdDate;
        this.acknowledged = false;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SuggestionType getSuggestionType() {
        return suggestionType;
    }

    public void setSuggestionType(SuggestionType suggestionType) {
        this.suggestionType = suggestionType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public LocalDate getAcknowledgedDate() {
        return acknowledgedDate;
    }

    public void setAcknowledgedDate(LocalDate acknowledgedDate) {
        this.acknowledgedDate = acknowledgedDate;
    }

    @Override
    public String toString() {
        return "Suggestion{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", title='" + title + '\'' +
                ", type=" + suggestionType +
                ", severity=" + severity +
                ", createdDate=" + createdDate +
                ", acknowledged=" + acknowledged +
                '}';
    }
}
