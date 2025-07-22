package com.fitnessplanner.service;

import com.fitnessplanner.dto.DailyWorkoutDto;
import com.fitnessplanner.dto.WorkoutPlanDto;
import com.fitnessplanner.model.MealLog;
import com.fitnessplanner.model.User;
import com.fitnessplanner.model.WorkoutLog;
import com.fitnessplanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final UserRepository userRepository;
    private final WorkoutService workoutService;
    private final DietTrackerService dietTrackerService;
    private final WorkoutTrackerService workoutTrackerService;

    @Autowired
    public ChatbotService(UserRepository userRepository, WorkoutService workoutService,
                          DietTrackerService dietTrackerService, WorkoutTrackerService workoutTrackerService) {
        this.userRepository = userRepository;
        this.workoutService = workoutService;
        this.dietTrackerService = dietTrackerService;
        this.workoutTrackerService = workoutTrackerService;
    }

    public String processQuery(String username, String query) {
        if (query == null || query.trim().isEmpty()) {
            return "Please ask a question.";
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found for chatbot query: " + username));

        String lowerQuery = query.toLowerCase();

        // --- Next Workout ---
        if (lowerQuery.contains("next workout") || lowerQuery.contains("today's workout") || lowerQuery.contains("what workout today")) {
            return getNextWorkoutResponse(user);
        }

        // --- Diet Consistency / Calorie Intake ---
        if (lowerQuery.contains("diet consistency") || lowerQuery.contains("how was my diet")) {
            if (lowerQuery.contains("last week")) {
                return getDietConsistencyResponse(user, LocalDate.now().minusWeeks(1), 7);
            } else if (lowerQuery.contains("yesterday")) {
                return getDietConsistencyResponse(user, LocalDate.now().minusDays(1), 1);
            }
            return getDietConsistencyResponse(user, LocalDate.now().minusDays(7), 7); // Default to last 7 days
        }

        if (lowerQuery.contains("calorie intake") || lowerQuery.contains("calories i ate")) {
            if (lowerQuery.contains("yesterday")) {
                return getCalorieIntakeResponse(user, LocalDate.now().minusDays(1));
            } else if (lowerQuery.contains("today")) {
                return getCalorieIntakeResponse(user, LocalDate.now());
            }
            // Try to parse a date if provided, e.g., "calories on YYYY-MM-DD" - more complex
            return "Please specify a day for calorie intake (e.g., 'calories yesterday' or 'calories today').";
        }

        // --- Workout History ---
        if (lowerQuery.contains("last workout") || lowerQuery.contains("recent workout")) {
            return getLastWorkoutResponse(user);
        }

        // --- General Greetings/Fallback ---
        if (lowerQuery.matches(".*(hello|hi|hey).*")) {
            return "Hello! How can I help you with your fitness plan today?";
        }


        return "I'm sorry, I didn't understand that. You can ask me about your next workout, diet consistency, or calorie intake.";
    }

    private String getNextWorkoutResponse(User user) {
        Optional<WorkoutPlanDto> currentPlanOpt = workoutService.getCurrentWorkoutPlanForUser(user.getUsername());
        if (currentPlanOpt.isEmpty()) {
            return "You don't seem to have an active workout plan. Please set up your workout profile first.";
        }

        WorkoutPlanDto currentPlan = currentPlanOpt.get();
        LocalDate today = LocalDate.now();
        DayOfWeek currentDayOfWeek = today.getDayOfWeek();
        String currentDayName = currentDayOfWeek.toString();

        Optional<DailyWorkoutDto> todaysWorkoutOpt = currentPlan.getWeeklySchedule().stream()
                .filter(dw -> dw.getDay().equalsIgnoreCase(currentDayName))
                .findFirst();

        if (todaysWorkoutOpt.isEmpty()) {
            return String.format("According to your plan '%s', there's no specific workout scheduled for today, %s. It might be a rest day.",
                                 currentPlan.getPlanName(), currentDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }

        DailyWorkoutDto todaysWorkout = todaysWorkoutOpt.get();
        StringBuilder response = new StringBuilder(String.format("Today is %s. Your workout is '%s' from plan '%s'.",
                currentDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH), todaysWorkout.getWorkoutType(), currentPlan.getPlanName()));

        if (todaysWorkout.getExercises() != null && !todaysWorkout.getExercises().isEmpty()) {
            response.append(" It includes: ");
            String exercises = todaysWorkout.getExercises().stream()
                    .map(e -> {
                        String details = e.getName();
                        if (e.getSets() != null) details += " (" + e.getSets() + " sets";
                        if (e.getReps() != null) details += ", " + e.getReps() + " reps";
                        if (e.getDurationMinutes() != null) details += ", " + e.getDurationMinutes() + " min";
                        if (e.getDurationSeconds() != null) details += ", " + e.getDurationSeconds() + " sec";
                        if (e.getSets() != null) details += ")"; // Close parenthesis if sets were added
                        return details;
                    })
                    .collect(Collectors.joining("; "));
            response.append(exercises).append(".");
        } else if (todaysWorkout.getDescription() != null && !todaysWorkout.getDescription().isEmpty()){
            response.append(" Details: ").append(todaysWorkout.getDescription());
        }
        return response.toString();
    }

    private String getDietConsistencyResponse(User user, LocalDate startDate, int days) {
        LocalDate endDate = startDate.plusDays(days - 1);
        List<MealLog> mealLogs = dietTrackerService.getMealLogsForUserBetweenDates(user.getUsername(), startDate, endDate);

        if (mealLogs.isEmpty()) {
            return String.format("You haven't logged any meals between %s and %s.",
                                 startDate.toString(), endDate.toString());
        }

        long distinctLogDays = mealLogs.stream().map(MealLog::getLogDate).distinct().count();
        double consistencyPercentage = ((double) distinctLogDays / days) * 100;

        String period;
        if (days == 1 && startDate.equals(LocalDate.now().minusDays(1))) period = "yesterday";
        else if (days == 7 && startDate.equals(LocalDate.now().minusWeeks(1))) period = "last week";
        else period = String.format("between %s and %s", startDate.toString(), endDate.toString());

        return String.format("For %s, you logged meals on %d out of %d days (%.0f%% consistency).",
                             period, distinctLogDays, days, consistencyPercentage);
    }

    private String getCalorieIntakeResponse(User user, LocalDate date) {
        Integer totalCalories = dietTrackerService.getDailyCaloriesForUser(user.getUsername(), date);
        String dayDescription = date.equals(LocalDate.now()) ? "today" : (date.equals(LocalDate.now().minusDays(1)) ? "yesterday" : "on " + date.toString());

        if (totalCalories == null || totalCalories == 0) {
            List<MealLog> logsOnDate = dietTrackerService.getMealLogsForUserOnDate(user.getUsername(), date);
            if(logsOnDate.isEmpty()){
                 return String.format("You haven't logged any meals for %s.", dayDescription);
            } else {
                 return String.format("You logged meals for %s, but calorie data seems incomplete. Total recorded: %d kcal.", dayDescription, totalCalories);
            }
        }
        return String.format("Your total calorie intake for %s was %d kcal.", dayDescription, totalCalories);
    }

    private String getLastWorkoutResponse(User user) {
        List<WorkoutLog> recentLogs = workoutTrackerService.getWorkoutLogsForUser(user.getUsername());
        if (recentLogs.isEmpty()) {
            return "You haven't logged any workouts yet.";
        }
        // Get logs from the most recent date
        LocalDate lastWorkoutDate = recentLogs.get(0).getWorkoutDate();
        List<WorkoutLog> lastSessionLogs = recentLogs.stream()
            .filter(log -> log.getWorkoutDate().equals(lastWorkoutDate))
            .collect(Collectors.toList());

        StringBuilder response = new StringBuilder(String.format("Your last workout session on %s included: ", lastWorkoutDate.toString()));
        String exercises = lastSessionLogs.stream()
            .map(log -> {
                String details = log.getExerciseName();
                if (log.getSetsPerformed() != null) details += " (" + log.getSetsPerformed() + " sets";
                if (log.getRepsPerformed() != null && !log.getRepsPerformed().isEmpty()) details += ", " + log.getRepsPerformed() + " reps";
                if (log.getWeightUsed() != null && log.getWeightUsed() > 0) details += ", " + log.getWeightUsed() + log.getWeightUnit();
                if (log.getDurationPerformedSeconds() != null) details += ", " + log.getDurationPerformedSeconds() + "s";
                 if (log.getSetsPerformed() != null) details += ")";
                return details;
            })
            .collect(Collectors.joining("; "));
        response.append(exercises).append(".");
        return response.toString();
    }

}
