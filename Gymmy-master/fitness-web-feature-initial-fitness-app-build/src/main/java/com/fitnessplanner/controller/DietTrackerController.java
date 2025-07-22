package com.fitnessplanner.controller;

import com.fitnessplanner.dto.DietPlanDto;
import com.fitnessplanner.dto.MealLogEntryDto;
import com.fitnessplanner.dto.MealLogFormDto;
import com.fitnessplanner.dto.SampleMealDto;
import com.fitnessplanner.model.MealLog;
import com.fitnessplanner.service.DietService;
import com.fitnessplanner.service.DietTrackerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/diet/tracker")
public class DietTrackerController {

    private static final Logger logger = LoggerFactory.getLogger(DietTrackerController.class);

    private final DietTrackerService dietTrackerService;
    private final DietService dietService; // To get current diet plan info

    private final List<String> MEAL_TYPES = Arrays.asList("Breakfast", "Lunch", "Dinner", "Snack", "Other");
    private final List<String> SERVING_UNITS = Arrays.asList("g", "ml", "oz", "cup", "piece", "serving");


    @Autowired
    public DietTrackerController(DietTrackerService dietTrackerService, DietService dietService) {
        this.dietTrackerService = dietTrackerService;
        this.dietService = dietService;
    }

    private void populateCommonModelAttributes(Model model) {
        model.addAttribute("mealTypes", MEAL_TYPES);
        model.addAttribute("servingUnits", SERVING_UNITS);
        model.addAttribute("today", LocalDate.now());
    }

    @GetMapping
    public String showDietTrackerForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        MealLogFormDto formDto = new MealLogFormDto();
        Optional<DietPlanDto> currentDietPlanOpt = dietService.getCurrentDietPlanForUser(principal.getName());

        if (currentDietPlanOpt.isPresent()) {
            DietPlanDto currentPlan = currentDietPlanOpt.get();
            model.addAttribute("currentDietPlanName", currentPlan.getPlanName());
            // Pre-populate with meal types from the plan, if available, or common ones
            if (currentPlan.getSampleMeals() != null && !currentPlan.getSampleMeals().isEmpty()) {
                for (SampleMealDto sampleMeal : currentPlan.getSampleMeals()) {
                    MealLogEntryDto entry = new MealLogEntryDto();
                    entry.setMealType(sampleMeal.getMealName()); // Or a more generic type if names are too specific
                    // entry.setFoodItemOrMealDescription("From plan: " + sampleMeal.getExampleFoods().get(0)); // Example
                    entry.setDietPlanId(currentPlan.getPlanId());
                    formDto.addLogEntry(entry);
                }
            }
        }
        // Ensure at least one entry for ad-hoc logging if no plan or plan has no meals
        if (formDto.getLogEntries().isEmpty()) {
            formDto.addLogEntry(new MealLogEntryDto()); // Default empty entry
        }


        model.addAttribute("mealLogForm", formDto);
        populateCommonModelAttributes(model);
        return "diet-tracker";
    }

    @PostMapping
    public String saveMealLogs(@ModelAttribute("mealLogForm") MealLogFormDto mealLogForm,
                               BindingResult bindingResult, // TODO: Add validation
                               Principal principal,
                               RedirectAttributes redirectAttributes, Model model) {
        if (principal == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            logger.warn("Binding errors in meal log form: {}", bindingResult.getAllErrors());
            populateCommonModelAttributes(model); // Repopulate for the form
            // Add currentDietPlanName if it was there in GET
            dietService.getCurrentDietPlanForUser(principal.getName())
                .ifPresent(plan -> model.addAttribute("currentDietPlanName", plan.getPlanName()));
            return "diet-tracker";
        }

        List<MealLog> logsToSave = new ArrayList<>();
        for (MealLogEntryDto entryDto : mealLogForm.getLogEntries()) {
            if (entryDto.getFoodItemOrMealDescription() == null || entryDto.getFoodItemOrMealDescription().trim().isEmpty()) {
                continue; // Skip empty entries
            }
            MealLog log = new MealLog();
            log.setMealType(entryDto.getMealType());
            log.setLogDate(entryDto.getLogDate() != null ? entryDto.getLogDate() : LocalDate.now());
            log.setLogTime(entryDto.getLogTime());
            log.setFoodItemOrMealDescription(entryDto.getFoodItemOrMealDescription());
            log.setCalories(entryDto.getCalories());
            log.setProteinGrams(entryDto.getProteinGrams());
            log.setCarbohydrateGrams(entryDto.getCarbohydrateGrams());
            log.setFatGrams(entryDto.getFatGrams());
            log.setFiberGrams(entryDto.getFiberGrams());
            log.setServingSize(entryDto.getServingSize());
            log.setServingUnit(entryDto.getServingUnit());
            log.setNotes(entryDto.getNotes());
            log.setDietPlanId(entryDto.getDietPlanId());
            logsToSave.add(log);
        }

        if (!logsToSave.isEmpty()) {
            try {
                dietTrackerService.saveMultipleMealLogs(logsToSave, principal.getName());
                redirectAttributes.addFlashAttribute("successMessage", "Meals logged successfully!");
            } catch (Exception e) {
                logger.error("Error saving meal logs for user {}", principal.getName(), e);
                redirectAttributes.addFlashAttribute("errorMessage", "Error saving meals: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "No meal entries were provided to log.");
        }

        return "redirect:/dashboard"; // Or to a diet history/summary page
    }

    @GetMapping("/history")
    public String showDietHistory(Model model, Principal principal,
                                  @RequestParam(value = "date", required = false) String dateStr) {
        if (principal == null) return "redirect:/login";

        LocalDate targetDate = LocalDate.now();
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                targetDate = LocalDate.parse(dateStr);
            } catch (Exception e) {
                logger.warn("Invalid date format for history: {}", dateStr);
                // Keep targetDate as today
            }
        }

        List<MealLog> history = dietTrackerService.getMealLogsForUserOnDate(principal.getName(), targetDate);
        model.addAttribute("mealHistory", history);
        model.addAttribute("historyDate", targetDate);

        // Calculate daily totals
        model.addAttribute("totalCalories", dietTrackerService.getDailyCaloriesForUser(principal.getName(), targetDate));
        model.addAttribute("totalProtein", dietTrackerService.getDailyProteinForUser(principal.getName(), targetDate));
        model.addAttribute("totalCarbs", dietTrackerService.getDailyCarbohydratesForUser(principal.getName(), targetDate));
        model.addAttribute("totalFat", dietTrackerService.getDailyFatForUser(principal.getName(), targetDate));

        // For date navigation
        model.addAttribute("previousDate", targetDate.minusDays(1));
        model.addAttribute("nextDate", targetDate.plusDays(1));


        return "diet-history"; // Create this template
    }
}
