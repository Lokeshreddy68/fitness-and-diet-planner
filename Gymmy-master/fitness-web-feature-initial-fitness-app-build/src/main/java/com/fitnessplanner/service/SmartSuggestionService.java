package com.fitnessplanner.service;

import com.fitnessplanner.dto.DietPlanDto;
import com.fitnessplanner.dto.WorkoutPlanDto;
import com.fitnessplanner.model.*;
import com.fitnessplanner.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class SmartSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(SmartSuggestionService.class);

    private final UserRepository userRepository;
    private final PlanHistoryService planHistoryService;
    private final WorkoutService workoutService; // To get WorkoutPlanDto details
    private final DietService dietService;     // To get DietPlanDto details
    private final WorkoutLogRepository workoutLogRepository;
    private final MealLogRepository mealLogRepository;
    private final DietProfileRepository dietProfileRepository; // To get target calories
    private final SuggestionRepository suggestionRepository; // Added

    // Thresholds for suggestions (can be configurable)
    private static final double MIN_WORKOUT_ADHERENCE_THRESHOLD = 0.7; // 70%
    private static final double CALORIE_COMPLIANCE_LOWER_BOUND = 0.85; // 85% of target
    private static final double CALORIE_COMPLIANCE_UPPER_BOUND = 1.15; // 115% of target
    private static final int ANALYSIS_PERIOD_DAYS = 30;


    @Autowired
    public SmartSuggestionService(UserRepository userRepository,
                                PlanHistoryService planHistoryService,
                                WorkoutService workoutService,
                                DietService dietService,
                                WorkoutLogRepository workoutLogRepository,
                                MealLogRepository mealLogRepository,
                                DietProfileRepository dietProfileRepository,
                                SuggestionRepository suggestionRepository) { // Added
        this.userRepository = userRepository;
        this.planHistoryService = planHistoryService;
        this.workoutService = workoutService;
        this.dietService = dietService;
        this.workoutLogRepository = workoutLogRepository;
        this.mealLogRepository = mealLogRepository;
        this.dietProfileRepository = dietProfileRepository;
        this.suggestionRepository = suggestionRepository; // Initialize
    }

    /**
     * Scheduled task to analyze user performance and generate suggestions.
     * Runs daily at a specific time (e.g., 2 AM).
     */
    @Scheduled(cron = "0 0 2 * * ?") // Example: Run daily at 2 AM
    // For testing, you might use @Scheduled(fixedRate = 600000) // every 10 minutes
    @Transactional(readOnly = true) // Most of this will be reading data
    public void analyzeAllUsersAndSuggest() {
        logger.info("Starting scheduled task: Analyze User Performance and Generate Suggestions");
        List<User> users = userRepository.findAll(); // In a real app, might process active users or in batches

        for (User user : users) {
            logger.info("Analyzing performance for user: {}", user.getUsername());
            try {
                generateSuggestionsForUser(user);
            } catch (Exception e) {
                logger.error("Error generating suggestions for user {}: {}", user.getUsername(), e.getMessage(), e);
            }
        }
        logger.info("Finished scheduled task: Analyze User Performance and Generate Suggestions");
    }

    public void generateSuggestionsForUser(User user) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(ANALYSIS_PERIOD_DAYS);

        // --- Workout Analysis ---
        Optional<UserPlanHistory> currentWorkoutPlanHistoryOpt = planHistoryService.getCurrentActivePlanByType(user.getUsername(), UserPlanHistory.PlanType.WORKOUT);
        if (currentWorkoutPlanHistoryOpt.isPresent()) {
            UserPlanHistory currentWorkoutPlanEntry = currentWorkoutPlanHistoryOpt.get();
            // Only analyze if the plan has been active for a significant portion of the analysis period
            if (ChronoUnit.DAYS.between(currentWorkoutPlanEntry.getStartDate(), endDate) >= (ANALYSIS_PERIOD_DAYS * 0.75)) {
                Optional<WorkoutPlanDto> workoutPlanDtoOpt = workoutService.getWorkoutPlanById(currentWorkoutPlanEntry.getPlanId());
                if (workoutPlanDtoOpt.isPresent()) {
                    analyzeWorkoutPerformance(user, workoutPlanDtoOpt.get(), currentWorkoutPlanEntry.getStartDate(), endDate);
                }
            }
        } else {
            logger.info("User {} has no active workout plan. Skipping workout analysis.", user.getUsername());
        }

        // --- Diet Analysis ---
        Optional<UserPlanHistory> currentDietPlanHistoryOpt = planHistoryService.getCurrentActivePlanByType(user.getUsername(), UserPlanHistory.PlanType.DIET);
        if (currentDietPlanHistoryOpt.isPresent()) {
             UserPlanHistory currentDietPlanEntry = currentDietPlanHistoryOpt.get();
            if (ChronoUnit.DAYS.between(currentDietPlanEntry.getStartDate(), endDate) >= (ANALYSIS_PERIOD_DAYS * 0.75)) {
                Optional<DietPlanDto> dietPlanDtoOpt = dietService.getDietPlanById(currentDietPlanEntry.getPlanId());
                 Optional<DietProfile> dietProfileOpt = dietProfileRepository.findByUser(user);

                if (dietPlanDtoOpt.isPresent() && dietProfileOpt.isPresent()) {
                    analyzeDietPerformance(user, dietPlanDtoOpt.get(), dietProfileOpt.get(), currentDietPlanEntry.getStartDate(), endDate);
                }
            }
        } else {
             logger.info("User {} has no active diet plan. Skipping diet analysis.", user.getUsername());
        }
    }

    private void analyzeWorkoutPerformance(User user, WorkoutPlanDto plan, LocalDate planStartDate, LocalDate analysisEndDate) {
        LocalDate analysisStartDate = planStartDate.isAfter(analysisEndDate.minusDays(ANALYSIS_PERIOD_DAYS)) ? planStartDate : analysisEndDate.minusDays(ANALYSIS_PERIOD_DAYS);
        if (analysisStartDate.isAfter(analysisEndDate)) return; // Not enough data

        List<WorkoutLog> userLogs = workoutLogRepository.findByUserAndWorkoutDateBetweenOrderByWorkoutDateDescWorkoutTimeDesc(user, analysisStartDate, analysisEndDate);

        int plannedWorkouts = 0;
        int completedWorkouts = 0;

        // Iterate through the days in the analysis period for this plan
        for (LocalDate date = analysisStartDate; !date.isAfter(analysisEndDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            String currentDayName = dayOfWeek.toString(); // MONDAY, TUESDAY, etc.

            Optional<com.fitnessplanner.dto.DailyWorkoutDto> plannedDailyWorkoutOpt = plan.getWeeklySchedule().stream()
                    .filter(dw -> dw.getDay().equalsIgnoreCase(currentDayName) && dw.getExercises() != null && !dw.getExercises().isEmpty())
                    .findFirst();

            if (plannedDailyWorkoutOpt.isPresent()) {
                plannedWorkouts++;
                final LocalDate currentDateForLambda = date; // Effectively final variable
                boolean workoutLoggedForDay = userLogs.stream()
                        .anyMatch(log -> log.getWorkoutDate().equals(currentDateForLambda) && // Use the effectively final variable
                                plan.getPlanId().equals(log.getWorkoutPlanId()) &&
                                currentDayName.equalsIgnoreCase(log.getDailyWorkoutDayName()));
                if (workoutLoggedForDay) {
                    completedWorkouts++;
                }
            }
        }

        if (plannedWorkouts == 0) {
            logger.info("No planned workouts found for user {} in plan {} during the analysis period.", user.getUsername(), plan.getPlanName());
            return;
        }

        double adherence = (double) completedWorkouts / plannedWorkouts;
        logger.info("User {} workout adherence for plan '{}': {:.2f}% ({} out of {} planned workouts)",
                user.getUsername(), plan.getPlanName(), adherence * 100, completedWorkouts, plannedWorkouts);

        if (adherence < MIN_WORKOUT_ADHERENCE_THRESHOLD) {
            createSuggestion(user, "Low Workout Adherence",
                    String.format("Your workout adherence for '%s' is %.0f%%. Consider a plan with fewer workout days or different exercises. Or, try to stick to the current plan more consistently.",
                            plan.getPlanName(), adherence * 100),
                    Suggestion.SuggestionType.WORKOUT_PLAN_CHANGE, Suggestion.Severity.MEDIUM);
        } else {
             createSuggestion(user, "Good Workout Adherence!",
                    String.format("Great job with %.0f%% adherence for workout plan '%s'! Keep it up.", adherence * 100, plan.getPlanName()),
                    Suggestion.SuggestionType.MOTIVATION, Suggestion.Severity.LOW);
        }
        // Further analysis: e.g., if adherence is high, suggest increasing intensity or a more advanced plan.
    }


    private void analyzeDietPerformance(User user, DietPlanDto plan, DietProfile profile, LocalDate planStartDate, LocalDate analysisEndDate) {
        LocalDate analysisStartDate = planStartDate.isAfter(analysisEndDate.minusDays(ANALYSIS_PERIOD_DAYS)) ? planStartDate : analysisEndDate.minusDays(ANALYSIS_PERIOD_DAYS);
        if (analysisStartDate.isAfter(analysisEndDate)) return;

        List<MealLog> userMealLogs = mealLogRepository.findByUserAndLogDateBetweenOrderByLogDateDescLogTimeDesc(user, analysisStartDate, analysisEndDate); // Corrected method name for MealLog

        if (userMealLogs.isEmpty()) {
            logger.info("No meal logs found for user {} during the analysis period.", user.getUsername());
             createSuggestion(user, "Track Your Diet",
                    String.format("We noticed you haven't logged many meals recently. Consistent tracking helps in achieving your goals with plan '%s'.", plan.getPlanName()),
                    Suggestion.SuggestionType.DIET_TRACKING_REMINDER, Suggestion.Severity.MEDIUM);
            return;
        }

        long daysWithLogging = userMealLogs.stream().map(MealLog::getLogDate).distinct().count();
        if (daysWithLogging < (ANALYSIS_PERIOD_DAYS * 0.5)) { // Not enough consistent logging
             logger.info("User {} has logged meals on only {} days in the period. Insufficient data for full diet analysis.", user.getUsername(), daysWithLogging);
             createSuggestion(user, "Improve Diet Tracking Consistency",
                    String.format("For better insights on your diet plan '%s', try to log your meals more consistently. You've logged on %d out of the last %d days.",
                                    plan.getPlanName(), daysWithLogging, ChronoUnit.DAYS.between(analysisStartDate, analysisEndDate) +1),
                    Suggestion.SuggestionType.DIET_TRACKING_REMINDER, Suggestion.Severity.MEDIUM);
            return;
        }


        double totalActualCalories = 0;
        int daysCounted = 0;
        // Sum calories for each day in the analysis period where logs exist
        for (LocalDate date = analysisStartDate; !date.isAfter(analysisEndDate); date = date.plusDays(1)) {
            final LocalDate currentDateForLambda = date; // Effectively final variable for the lambda
            double dailyCalories = userMealLogs.stream()
                    .filter(log -> log.getLogDate().equals(currentDateForLambda) && log.getCalories() != null)
                    .mapToInt(MealLog::getCalories)
                    .sum();
            if (dailyCalories > 0) { // Only count days where something was logged
                totalActualCalories += dailyCalories;
                daysCounted++;
            }
        }

        if (daysCounted == 0) {
            logger.info("No days with calorie logs found for user {} in period.", user.getUsername());
            return;
        }

        double averageDailyActualCalories = totalActualCalories / daysCounted;
        Integer targetCalories = profile.getTargetCalories() != null ? profile.getTargetCalories() : plan.getEstimatedDailyCalories();

        if (targetCalories == null || targetCalories == 0) {
            logger.warn("Target calories not set for user {} or plan {}. Cannot perform diet compliance analysis.", user.getUsername(), plan.getPlanName());
            return;
        }

        double calorieComplianceRatio = averageDailyActualCalories / targetCalories;
        logger.info("User {} average daily calories: {:.0f} (Target: {}). Compliance ratio: {:.2f}",
                user.getUsername(), averageDailyActualCalories, targetCalories, calorieComplianceRatio);

        if (calorieComplianceRatio < CALORIE_COMPLIANCE_LOWER_BOUND) {
            createSuggestion(user, "Calorie Intake Low",
                    String.format("Your average calorie intake (%.0f kcal) is significantly below your target (%.0f kcal) for plan '%s'. Consider increasing portion sizes or adding nutrient-dense snacks.",
                            averageDailyActualCalories, targetCalories, plan.getPlanName()),
                    Suggestion.SuggestionType.DIET_ADJUSTMENT, Suggestion.Severity.MEDIUM);
        } else if (calorieComplianceRatio > CALORIE_COMPLIANCE_UPPER_BOUND) {
            createSuggestion(user, "Calorie Intake High",
                     String.format("Your average calorie intake (%.0f kcal) is significantly above your target (%.0f kcal) for plan '%s'. Review portion sizes or consider lower-calorie food choices.",
                            averageDailyActualCalories, targetCalories, plan.getPlanName()),
                    Suggestion.SuggestionType.DIET_ADJUSTMENT, Suggestion.Severity.HIGH);
        } else {
             createSuggestion(user, "Good Calorie Compliance!",
                    String.format("Your average calorie intake (%.0f kcal) is well-aligned with your target (%.0f kcal) for plan '%s'. Keep up the great work!",
                            averageDailyActualCalories, targetCalories, plan.getPlanName()),
                    Suggestion.SuggestionType.MOTIVATION, Suggestion.Severity.LOW);
        }
        // Further analysis: Macronutrient compliance, meal timing consistency, etc.
    }

    @Transactional // Make this transactional as it now writes to the DB
    protected void createSuggestion(User user, String title, String message, Suggestion.SuggestionType type, Suggestion.Severity severity) {
        Suggestion newSuggestion = new Suggestion(user, title, message, type, severity, LocalDate.now());
        try {
            suggestionRepository.save(newSuggestion);
            logger.info("Saved SUGGESTION for {}: [{}] {} (Severity: {})", user.getUsername(), type, title, severity);
        } catch (Exception e) {
            logger.error("Failed to save suggestion for user {}: {}", user.getUsername(), title, e);
        }
    }
}
