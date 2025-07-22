package com.fitnessplanner.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnessplanner.dto.ChartDataDto;
import com.fitnessplanner.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);
    private static final int DEFAULT_WEEKS_FOR_ANALYTICS = 4;


    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper; // For converting DTO to JSON string for JavaScript

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String showAnalyticsPage(Model model, Principal principal,
                                    @RequestParam(name = "weeks", required = false, defaultValue = "4") int weeks) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        if (weeks <= 0 || weeks > 12) { // Cap weeks for performance/display reasons
            weeks = DEFAULT_WEEKS_FOR_ANALYTICS;
        }

        try {
            ChartDataDto workoutCountData = analyticsService.getWeeklyWorkoutCountChartData(username, weeks);
            ChartDataDto avgCaloriesData = analyticsService.getWeeklyAverageCaloriesChartData(username, weeks);

            model.addAttribute("workoutCountDataJson", objectMapper.writeValueAsString(workoutCountData));
            model.addAttribute("avgCaloriesDataJson", objectMapper.writeValueAsString(avgCaloriesData));
            model.addAttribute("selectedWeeks", weeks);

        } catch (JsonProcessingException e) {
            logger.error("Error serializing chart data to JSON for user {}: {}", username, e.getMessage());
            model.addAttribute("errorMessage", "Could not load chart data.");
            // Send empty data to prevent JS errors on template
            try {
                model.addAttribute("workoutCountDataJson", objectMapper.writeValueAsString(new ChartDataDto()));
                model.addAttribute("avgCaloriesDataJson", objectMapper.writeValueAsString(new ChartDataDto()));
            } catch (JsonProcessingException ignored) {}
        } catch (RuntimeException e) {
            logger.error("Error fetching analytics data for user {}: {}", username, e.getMessage());
            model.addAttribute("errorMessage", "Could not load analytics data: " + e.getMessage());
             try {
                model.addAttribute("workoutCountDataJson", objectMapper.writeValueAsString(new ChartDataDto()));
                model.addAttribute("avgCaloriesDataJson", objectMapper.writeValueAsString(new ChartDataDto()));
            } catch (JsonProcessingException ignored) {}
        }

        model.addAttribute("pageTitle", "Progress Analytics");
        return "analytics"; // Renders analytics.html
    }

    // We could also have REST endpoints like /api/analytics/workout-counts
    // if we wanted to load data via AJAX calls from JavaScript,
    // but for now, embedding as JSON string in the model is simpler for Thymeleaf.
}
