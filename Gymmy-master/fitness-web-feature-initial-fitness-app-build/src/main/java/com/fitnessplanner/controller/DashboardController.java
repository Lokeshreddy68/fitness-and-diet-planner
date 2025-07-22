package com.fitnessplanner.controller;

import com.fitnessplanner.dto.DailyWorkoutDto;
import com.fitnessplanner.dto.DietPlanDto;
import com.fitnessplanner.dto.SampleMealDto;
import com.fitnessplanner.dto.WorkoutPlanDto;
import com.fitnessplanner.model.User;
import com.fitnessplanner.service.DietService;
import com.fitnessplanner.service.UserService;
import com.fitnessplanner.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final WorkoutService workoutService;
    private final DietService dietService;

    @Autowired
    public DashboardController(UserService userService, WorkoutService workoutService, DietService dietService) {
        this.userService = userService;
        this.workoutService = workoutService;
        this.dietService = dietService;
    }

    @GetMapping
    public String showDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            // Should not happen if user is authenticated, but good practice
            return "redirect:/login?error=UserNotFound";
        }
        User currentUser = userOptional.get();
        model.addAttribute("currentUser", currentUser);

        // Fetch current workout plan and today's workout
        Optional<WorkoutPlanDto> currentWorkoutPlanOpt = workoutService.getCurrentWorkoutPlanForUser(username);
        model.addAttribute("currentWorkoutPlan", currentWorkoutPlanOpt.orElse(null));
        currentWorkoutPlanOpt.ifPresent(plan -> {
            Optional<DailyWorkoutDto> todaysWorkoutOpt = getTodaysWorkout(plan);
            model.addAttribute("todaysWorkout", todaysWorkoutOpt.orElse(null));
        });
        if (currentWorkoutPlanOpt.isEmpty()) {
             model.addAttribute("todaysWorkout", null); // Ensure it's null if no plan
        }


        // Fetch current diet plan and today's meal examples (or just the plan)
        Optional<DietPlanDto> currentDietPlanOpt = dietService.getCurrentDietPlanForUser(username);
        model.addAttribute("currentDietPlan", currentDietPlanOpt.orElse(null));
        currentDietPlanOpt.ifPresent(plan -> {
            // For simplicity, we're not matching specific meals to "today" yet,
            // but we can show a few sample meals or the general plan info.
            // If the diet plan has sample meals, pass them to the model.
            if (plan.getSampleMeals() != null && !plan.getSampleMeals().isEmpty()) {
                model.addAttribute("sampleMeals", plan.getSampleMeals());
            } else {
                model.addAttribute("sampleMeals", Collections.emptyList());
            }
        });
         if (currentDietPlanOpt.isEmpty()) {
             model.addAttribute("sampleMeals", Collections.emptyList());
        }


        // Add messages from redirects if they exist
        // (These were added in previous controllers, useful for dashboard feedback)
        // Model will automatically contain flash attributes

        return "dashboard"; // Renders dashboard.html
    }

    private Optional<DailyWorkoutDto> getTodaysWorkout(WorkoutPlanDto workoutPlan) {
        if (workoutPlan == null || workoutPlan.getWeeklySchedule() == null || workoutPlan.getWeeklySchedule().isEmpty()) {
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();
        DayOfWeek currentDayOfWeek = today.getDayOfWeek();
        String currentDayName = currentDayOfWeek.toString(); // MONDAY, TUESDAY, etc.

        // Find a workout for today by matching the day name (case-insensitive)
        return workoutPlan.getWeeklySchedule().stream()
                .filter(dailyWorkout -> dailyWorkout.getDay() != null &&
                                         dailyWorkout.getDay().equalsIgnoreCase(currentDayName))
                .findFirst();
    }
}
