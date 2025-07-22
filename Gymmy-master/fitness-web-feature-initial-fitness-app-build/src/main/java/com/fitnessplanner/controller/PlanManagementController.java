package com.fitnessplanner.controller;

import com.fitnessplanner.dto.DietPlanDto;
import com.fitnessplanner.dto.WorkoutPlanDto;
import com.fitnessplanner.service.DietService;
import com.fitnessplanner.service.UserRequestedPlanChangeService;
import com.fitnessplanner.service.WorkoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/manage-plans")
public class PlanManagementController {

    private static final Logger logger = LoggerFactory.getLogger(PlanManagementController.class);

    private final UserRequestedPlanChangeService userRequestedPlanChangeService;
    private final WorkoutService workoutService;
    private final DietService dietService;

    @Autowired
    public PlanManagementController(UserRequestedPlanChangeService userRequestedPlanChangeService,
                                    WorkoutService workoutService, DietService dietService) {
        this.userRequestedPlanChangeService = userRequestedPlanChangeService;
        this.workoutService = workoutService;
        this.dietService = dietService;
    }

    // --- Workout Plan Management ---

    @GetMapping("/workout/change")
    public String showChangeWorkoutPlanOptions(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        List<WorkoutPlanDto> availablePlans = workoutService.getAllWorkoutPlans();
        model.addAttribute("availablePlans", availablePlans);
        model.addAttribute("currentPlan", workoutService.getCurrentWorkoutPlanForUser(principal.getName()).orElse(null));
        model.addAttribute("planType", "workout");
        return "change-plan-options"; // One template for both workout and diet
    }

    @PostMapping("/workout/change")
    public String processChangeWorkoutPlan(@RequestParam("selectedPlanId") String selectedPlanId,
                                           Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        boolean success = userRequestedPlanChangeService.changeUserWorkoutPlan(principal.getName(), selectedPlanId);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Workout plan changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change workout plan. The new plan might not be valid or an error occurred.");
        }
        return "redirect:/workout/my-plan";
    }

    @PostMapping("/workout/discontinue")
    public String processDiscontinueWorkoutPlan(Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        boolean success = userRequestedPlanChangeService.discontinueUserWorkoutPlan(principal.getName());
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Workout plan discontinued successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to discontinue workout plan.");
        }
        return "redirect:/dashboard"; // Or workout profile page
    }

    // --- Diet Plan Management ---

    @GetMapping("/diet/change")
    public String showChangeDietPlanOptions(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        List<DietPlanDto> availablePlans = dietService.getAllDietPlans();
        model.addAttribute("availablePlans", availablePlans); // Note: template needs to handle different DTO types if generic
        model.addAttribute("currentPlan", dietService.getCurrentDietPlanForUser(principal.getName()).orElse(null));
        model.addAttribute("planType", "diet");
        return "change-plan-options";
    }

    @PostMapping("/diet/change")
    public String processChangeDietPlan(@RequestParam("selectedPlanId") String selectedPlanId,
                                        Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        boolean success = userRequestedPlanChangeService.changeUserDietPlan(principal.getName(), selectedPlanId);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Diet plan changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change diet plan. The new plan might not be valid or an error occurred.");
        }
        return "redirect:/diet/my-plan";
    }

    @PostMapping("/diet/discontinue")
    public String processDiscontinueDietPlan(Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        boolean success = userRequestedPlanChangeService.discontinueUserDietPlan(principal.getName());
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Diet plan discontinued successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to discontinue diet plan.");
        }
        return "redirect:/dashboard"; // Or diet profile page
    }
}
