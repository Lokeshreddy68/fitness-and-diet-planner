package com.fitnessplanner.controller;

import com.fitnessplanner.model.DietProfile;
import com.fitnessplanner.service.DietService;
import com.fitnessplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/diet")
public class DietController {

    private final DietService dietService;
    private final UserService userService; // If needed
    private static final Logger logger = LoggerFactory.getLogger(DietController.class);

    @Autowired
    public DietController(DietService dietService, UserService userService) {
        this.dietService = dietService;
        this.userService = userService;
    }

    private void addFormOptions(Model model) {
        List<String> dietTypes = Arrays.asList("balanced", "low_carb", "high_protein", "vegetarian", "vegan", "keto", "paleo");
        List<String> primaryGoals = Arrays.asList("weight_loss", "weight_gain", "maintenance", "muscle_building_support", "improved_health");
        // Common allergies - this could be more extensive or user-defined in a real app
        List<String> commonAllergies = Arrays.asList("none", "gluten", "dairy", "nuts", "soy", "shellfish", "eggs");
        List<Integer> mealFrequencies = Arrays.asList(1, 2, 3, 4, 5, 6);

        model.addAttribute("dietTypes", dietTypes);
        model.addAttribute("primaryGoals", primaryGoals);
        model.addAttribute("commonAllergies", commonAllergies);
        model.addAttribute("mealFrequencies", mealFrequencies);
    }

    @GetMapping("/profile")
    public String showDietProfileForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        DietProfile dietProfile = dietService.getDietProfileByUsername(username)
                .orElse(new DietProfile()); // Create new if not exists

        model.addAttribute("dietProfile", dietProfile);
        addFormOptions(model);
        return "diet-form"; // Renders diet-form.html
    }

    @PostMapping("/profile")
    public String saveDietProfile(@ModelAttribute("dietProfile") DietProfile dietProfile,
                                  BindingResult bindingResult, // For JSR 303 validation
                                  Model model,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();

        // Basic Manual Validation
        if (dietProfile.getDietType() == null || dietProfile.getDietType().trim().isEmpty()) {
            bindingResult.rejectValue("dietType", "dietProfile.dietType", "Diet type is required.");
        }
        if (dietProfile.getMealFrequency() == null || dietProfile.getMealFrequency() <= 0) {
            bindingResult.rejectValue("mealFrequency", "dietProfile.mealFrequency", "Meal frequency must be a positive number.");
        }
        if (dietProfile.getPrimaryGoal() == null || dietProfile.getPrimaryGoal().trim().isEmpty()) {
            bindingResult.rejectValue("primaryGoal", "dietProfile.primaryGoal", "Primary goal is required.");
        }
        List<String> currentAllergies = dietProfile.getAllergies();
        if (currentAllergies != null) {
            if (currentAllergies.contains("none")) {
                // If "none" is selected, make it the only allergy in a new mutable list.
                dietProfile.setAllergies(new ArrayList<>(List.of("none")));
            } else {
                // If "none" is not selected, ensure the list is mutable if it's not already.
                // Spring usually binds multi-select to an ArrayList, so this might be redundant
                // but doesn't hurt.
                if (!(currentAllergies instanceof ArrayList)) {
                    dietProfile.setAllergies(new ArrayList<>(currentAllergies));
                }
            }
        } else {
            // If no allergies were selected at all from the form, the list might be null.
            // Set it to an empty mutable list.
            dietProfile.setAllergies(new ArrayList<>());
        }


        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            model.addAttribute("dietProfile", dietProfile);
            return "diet-form";
        }

        try {
            DietProfile savedProfile = dietService.saveOrUpdateDietProfile(dietProfile, username);
            redirectAttributes.addFlashAttribute("successMessage", "Diet profile saved successfully!");
            if (savedProfile.getCurrentDietPlanId() != null) {
                redirectAttributes.addFlashAttribute("planAssignedMessage", "A diet plan has been assigned based on your profile.");
            } else {
                redirectAttributes.addFlashAttribute("planNotAssignedMessage", "Your profile is saved, but we couldn't find a perfectly matching diet plan right now.");
            }
            return "redirect:/dashboard"; // Or to a page showing the diet plan
        } catch (Exception e) {
            addFormOptions(model);
            model.addAttribute("dietProfile", dietProfile);
            String specificErrorMessage = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred.";
            model.addAttribute("errorMessage", "Error saving diet profile: " + specificErrorMessage);
            logger.error("Full error details while saving diet profile for user {}: ", principal.getName(), e); // Log the full exception 'e'
            return "diet-form";
        }
    }

    @GetMapping("/my-plan")
    public String viewMyDietPlan(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        model.addAttribute("dietProfile", dietService.getDietProfileByUsername(username).orElse(null));
        model.addAttribute("currentDietPlan", dietService.getCurrentDietPlanForUser(username).orElse(null));
        return "view-diet-plan"; // Renders view-diet-plan.html (create this template)
    }
}
