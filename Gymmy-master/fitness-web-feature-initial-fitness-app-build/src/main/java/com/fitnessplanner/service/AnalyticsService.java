package com.fitnessplanner.service;

import com.fitnessplanner.dto.ChartDataDto;
import com.fitnessplanner.model.MealLog;
import com.fitnessplanner.model.User;
import com.fitnessplanner.model.WorkoutLog;
import com.fitnessplanner.repository.MealLogRepository;
import com.fitnessplanner.repository.UserRepository;
import com.fitnessplanner.repository.WorkoutLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private static final int DEFAULT_WEEKS_OF_DATA = 4; // Analyze last 4 weeks

    private final UserRepository userRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final MealLogRepository mealLogRepository;

    @Autowired
    public AnalyticsService(UserRepository userRepository, WorkoutLogRepository workoutLogRepository, MealLogRepository mealLogRepository) {
        this.userRepository = userRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.mealLogRepository = mealLogRepository;
    }

    /**
     * Generates chart data for weekly workout counts for the user.
     * @param username The user's username.
     * @param weeks The number of past weeks to include.
     * @return ChartDataDto for workout counts.
     */
    public ChartDataDto getWeeklyWorkoutCountChartData(String username, int weeks) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (weeks <= 0) weeks = DEFAULT_WEEKS_OF_DATA;

        LocalDate today = LocalDate.now();
        Map<String, Integer> weeklyWorkoutCounts = new TreeMap<>(); // TreeMap to keep weeks sorted
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < weeks; i++) {
            LocalDate weekEndDate = today.minusWeeks(i);
            LocalDate weekStartDate = weekEndDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            // Adjust if the plan started mid-way through the oldest week to not show "full" week
            // For simplicity now, we take full weeks.

            List<WorkoutLog> logsInWeek = workoutLogRepository.findByUserAndWorkoutDateBetweenOrderByWorkoutDateDescWorkoutTimeDesc(user, weekStartDate, weekEndDate);
            long distinctWorkoutDays = logsInWeek.stream().map(WorkoutLog::getWorkoutDate).distinct().count();

            // Using "YYYY-Www" format for labels e.g. "2023-W40"
            String weekLabel = weekStartDate.getYear() + "-W" + weekStartDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
            labels.add(0, weekLabel); // Add to beginning to have oldest week first
            weeklyWorkoutCounts.put(weekLabel, (int) distinctWorkoutDays);
        }

        List<Number> dataPoints = new ArrayList<>();
        for (String label : labels) { // Iterate in sorted order of labels
            dataPoints.add(weeklyWorkoutCounts.getOrDefault(label, 0));
        }

        ChartDataDto.DatasetDto dataset = new ChartDataDto.DatasetDto("Workouts Logged (Days)", dataPoints);
        dataset.setBackgroundColor("rgba(75, 192, 192, 0.2)");
        dataset.setBorderColor("rgba(75, 192, 192, 1)");

        return new ChartDataDto(labels, List.of(dataset));
    }


    /**
     * Generates chart data for weekly average daily calorie intake.
     * @param username The user's username.
     * @param weeks The number of past weeks to include.
     * @return ChartDataDto for calorie intake.
     */
    public ChartDataDto getWeeklyAverageCaloriesChartData(String username, int weeks) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (weeks <= 0) weeks = DEFAULT_WEEKS_OF_DATA;

        LocalDate today = LocalDate.now();
        Map<String, Double> weeklyAverageCalories = new TreeMap<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < weeks; i++) {
            LocalDate weekEndDate = today.minusWeeks(i);
            LocalDate weekStartDate = weekEndDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            List<MealLog> logsInWeek = mealLogRepository.findByUserAndLogDateBetweenOrderByLogDateDescLogTimeDesc(user, weekStartDate, weekEndDate);

            Map<LocalDate, Integer> dailyCaloriesInWeek = logsInWeek.stream()
                    .filter(log -> log.getCalories() != null)
                    .collect(Collectors.groupingBy(
                            MealLog::getLogDate,
                            Collectors.summingInt(MealLog::getCalories)
                    ));

            double averageDailyCals;
            if (dailyCaloriesInWeek.isEmpty()) {
                averageDailyCals = 0.0;
            } else {
                averageDailyCals = dailyCaloriesInWeek.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
            }

            String weekLabel = weekStartDate.getYear() + "-W" + weekStartDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
            labels.add(0, weekLabel); // Add to beginning
            weeklyAverageCalories.put(weekLabel, averageDailyCals);
        }

        List<Number> dataPoints = new ArrayList<>();
         for (String label : labels) { // Iterate in sorted order of labels
            dataPoints.add(weeklyAverageCalories.getOrDefault(label, 0.0));
        }


        ChartDataDto.DatasetDto dataset = new ChartDataDto.DatasetDto("Avg Daily Calories (kcal)", dataPoints);
        dataset.setBackgroundColor("rgba(255, 99, 132, 0.2)");
        dataset.setBorderColor("rgba(255, 99, 132, 1)");

        return new ChartDataDto(labels, List.of(dataset));
    }

    // Potentially more analytics methods:
    // - Weight progress (if weight is logged regularly, perhaps via a new "WeightLog" entity)
    // - Exercise progression (e.g., max weight lifted for a specific exercise over time)
    // - Macronutrient distribution trends
}
