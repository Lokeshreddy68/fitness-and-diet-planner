package com.fitnessplanner.controller;

import com.fitnessplanner.model.WorkoutProfile;
import com.fitnessplanner.service.UserService;
import com.fitnessplanner.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/workout")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final UserService userService; // If needed to fetch full User object

    @Autowired
    public WorkoutController(WorkoutService workoutService, UserService userService) {
        this.workoutService = workoutService;
        this.userService = userService;
    }

    // Provide options for form dropdowns
    private void addFormOptions(Model model) {
        List<String> fitnessGoals = Arrays.asList("weight_loss", "muscle_gain", "general_fitness", "endurance_improvement", "flexibility");
        List<String> activityLevels = Arrays.asList("sedentary", "lightly_active", "moderately_active", "very_active", "extremely_active");
        model.addAttribute("fitnessGoals", fitnessGoals);
        model.addAttribute("activityLevels", activityLevels);
    }

    @GetMapping("/profile")
    public String showWorkoutProfileForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        WorkoutProfile workoutProfile = workoutService.getWorkoutProfileByUsername(username)
                .orElse(new WorkoutProfile()); // Create a new one if not exists

        model.addAttribute("workoutProfile", workoutProfile);
        addFormOptions(model);
        return "workout-form"; // Renders workout-form.html
    }

    @PostMapping("/profile")
    public String saveWorkoutProfile(@ModelAttribute("workoutProfile") WorkoutProfile workoutProfile,
                                     BindingResult bindingResult, // For JSR 303 validation if added later
                                     Model model,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();

        // Basic manual validation (can be enhanced with @Valid and JSR 303 annotations on WorkoutProfile)
        if (workoutProfile.getAge() <= 0 || workoutProfile.getAge() > 120) {
            bindingResult.rejectValue("age", "workoutProfile.age", "Age must be a valid number.");
        }
        if (workoutProfile.getWeight() <= 0) {
            bindingResult.rejectValue("weight", "workoutProfile.weight", "Weight must be a positive number.");
        }
        if (workoutProfile.getFitnessGoal() == null || workoutProfile.getFitnessGoal().trim().isEmpty()) {
            bindingResult.rejectValue("fitnessGoal", "workoutProfile.fitnessGoal", "Fitness goal is required.");
        }
        if (workoutProfile.getActivityLevel() == null || workoutProfile.getActivityLevel().trim().isEmpty()) {
            bindingResult.rejectValue("activityLevel", "workoutProfile.activityLevel", "Activity level is required.");
        }

        if (bindingResult.hasErrors()) {
            addFormOptions(model); // Add dropdown options back to the model
            model.addAttribute("workoutProfile", workoutProfile); // Send profile back to pre-fill form
            return "workout-form"; // Return to form page with errors
        }

        try {
            WorkoutProfile savedProfile = workoutService.saveOrUpdateWorkoutProfile(workoutProfile, username);
            redirectAttributes.addFlashAttribute("successMessage", "Workout profile saved successfully!");
            if (savedProfile.getCurrentWorkoutPlanId() != null) {
                redirectAttributes.addFlashAttribute("planAssignedMessage", "A workout plan has been assigned based on your profile.");
            } else {
                redirectAttributes.addFlashAttribute("planNotAssignedMessage", "Your profile is saved, but we couldn't find a perfectly matching plan right now. We'll keep looking!");
            }
            return "redirect:/dashboard"; // Or redirect to a page showing the assigned plan
        } catch (Exception e) {
            // Log exception e.g. e.printStackTrace();
            addFormOptions(model);
            model.addAttribute("workoutProfile", workoutProfile);
            model.addAttribute("errorMessage", "Error saving workout profile: " + e.getMessage());
            return "workout-form";
        }
    }

    // Example: A page to view the current assigned workout plan (can be integrated into dashboard)
    @GetMapping("/my-plan")
    public String viewMyWorkoutPlan(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        model.addAttribute("workoutProfile", workoutService.getWorkoutProfileByUsername(username).orElse(null));
        model.addAttribute("currentWorkoutPlan", workoutService.getCurrentWorkoutPlanForUser(username).orElse(null));
        return "view-workout-plan"; // Renders view-workout-plan.html (create this template)
    }
}
