package com.fitnessplanner.controller;

import com.fitnessplanner.dto.DailyWorkoutDto;
import com.fitnessplanner.dto.ExerciseDto;
import com.fitnessplanner.dto.WorkoutLogEntryDto;
import com.fitnessplanner.dto.WorkoutLogFormDto;
import com.fitnessplanner.dto.WorkoutPlanDto;
import com.fitnessplanner.model.WorkoutLog;
import com.fitnessplanner.service.WorkoutService;
import com.fitnessplanner.service.WorkoutTrackerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workout/tracker")
public class WorkoutTrackerController {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutTrackerController.class);

    private final WorkoutTrackerService workoutTrackerService;
    private final WorkoutService workoutService; // To get today's planned workout

    @Autowired
    public WorkoutTrackerController(WorkoutTrackerService workoutTrackerService, WorkoutService workoutService) {
        this.workoutTrackerService = workoutTrackerService;
        this.workoutService = workoutService;
    }

    private Optional<DailyWorkoutDto> getTodaysPlannedWorkout(String username) {
        Optional<WorkoutPlanDto> currentWorkoutPlanOpt = workoutService.getCurrentWorkoutPlanForUser(username);
        if (currentWorkoutPlanOpt.isPresent()) {
            WorkoutPlanDto plan = currentWorkoutPlanOpt.get();
            LocalDate today = LocalDate.now();
            DayOfWeek currentDayOfWeek = today.getDayOfWeek();
            String currentDayName = currentDayOfWeek.toString();

            return plan.getWeeklySchedule().stream()
                    .filter(dailyWorkout -> dailyWorkout.getDay() != null &&
                            dailyWorkout.getDay().equalsIgnoreCase(currentDayName))
                    .findFirst();
        }
        return Optional.empty();
    }

    @GetMapping
    public String showWorkoutTrackerForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        WorkoutLogFormDto formDto = new WorkoutLogFormDto();
        Optional<DailyWorkoutDto> todaysPlannedWorkoutOpt = getTodaysPlannedWorkout(principal.getName());

        if (todaysPlannedWorkoutOpt.isPresent()) {
            DailyWorkoutDto todaysPlannedWorkout = todaysPlannedWorkoutOpt.get();
            model.addAttribute("todaysPlannedWorkoutName", todaysPlannedWorkout.getWorkoutType());
            if (todaysPlannedWorkout.getExercises() != null && !todaysPlannedWorkout.getExercises().isEmpty()) {
                for (ExerciseDto plannedExercise : todaysPlannedWorkout.getExercises()) {
                    WorkoutLogEntryDto entryDto = new WorkoutLogEntryDto();
                    entryDto.setExerciseName(plannedExercise.getName());
                    // Pre-fill reps/sets from plan if needed, but user should confirm actuals
                    // entryDto.setSetsPerformed(plannedExercise.getSets());
                    // entryDto.setRepsPerformed(plannedExercise.getReps());
                    entryDto.setWorkoutPlanId(workoutService.getCurrentWorkoutPlanForUser(principal.getName()).map(WorkoutPlanDto::getPlanId).orElse(null));
                    entryDto.setDailyWorkoutDayName(todaysPlannedWorkout.getDay());
                    formDto.addLogEntry(entryDto);
                }
            } else { // Rest day or cardio with no specific exercises listed in plan
                 WorkoutLogEntryDto entryDto = new WorkoutLogEntryDto();
                 entryDto.setExerciseName(todaysPlannedWorkout.getWorkoutType()); // e.g. "Rest" or "30 min Cardio"
                 if (todaysPlannedWorkout.getExercises() != null && !todaysPlannedWorkout.getExercises().isEmpty()){
                    ExerciseDto firstExercise = todaysPlannedWorkout.getExercises().get(0);
                    if(firstExercise.getDurationMinutes() != null) {
                         entryDto.setDurationPerformedSeconds(firstExercise.getDurationMinutes() * 60);
                    }
                 }
                 entryDto.setWorkoutPlanId(workoutService.getCurrentWorkoutPlanForUser(principal.getName()).map(WorkoutPlanDto::getPlanId).orElse(null));
                 entryDto.setDailyWorkoutDayName(todaysPlannedWorkout.getDay());
                 formDto.addLogEntry(entryDto);
            }
        } else {
            // If no planned workout, provide a blank entry for ad-hoc logging
            formDto.addLogEntry(new WorkoutLogEntryDto());
            model.addAttribute("todaysPlannedWorkoutName", "Ad-hoc Workout");
        }

        model.addAttribute("workoutLogForm", formDto);
        model.addAttribute("weightUnits", List.of("kg", "lbs", "bodyweight"));
        model.addAttribute("today", LocalDate.now());
        return "workout-tracker";
    }

    @PostMapping
    public String saveWorkoutLogs(@ModelAttribute("workoutLogForm") WorkoutLogFormDto workoutLogForm,
                                  BindingResult bindingResult,
                                  Model model, // <--- ADD THIS PARAMETER
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        if (bindingResult.hasErrors()) {
            // Handle errors, add attributes back to model, return to form
            logger.warn("Binding errors in workout log form: {}", bindingResult.getAllErrors());
            // Need to repopulate model attributes like in GET if returning to form
            model.addAttribute("weightUnits", List.of("kg", "lbs", "bodyweight"));
            model.addAttribute("today", LocalDate.now());
            Optional<DailyWorkoutDto> todaysPlannedWorkoutOpt = getTodaysPlannedWorkout(principal.getName());
             todaysPlannedWorkoutOpt.ifPresent(dw -> model.addAttribute("todaysPlannedWorkoutName", dw.getWorkoutType()));
            if(!todaysPlannedWorkoutOpt.isPresent()){
                 model.addAttribute("todaysPlannedWorkoutName", "Ad-hoc Workout");
            }
            return "workout-tracker";
        }

        List<WorkoutLog> logsToSave = new ArrayList<>();
        for (WorkoutLogEntryDto entryDto : workoutLogForm.getLogEntries()) {
            // Basic check to not save empty entries if user doesn't fill all pre-populated ones
            if (entryDto.getExerciseName() == null || entryDto.getExerciseName().trim().isEmpty()) {
                continue;
            }
            WorkoutLog log = new WorkoutLog();
            log.setExerciseName(entryDto.getExerciseName());
            log.setWorkoutDate(entryDto.getWorkoutDate() != null ? entryDto.getWorkoutDate() : LocalDate.now());
            log.setWorkoutTime(entryDto.getWorkoutTime());
            log.setSetsPerformed(entryDto.getSetsPerformed());
            log.setRepsPerformed(entryDto.getRepsPerformed());
            log.setWeightUsed(entryDto.getWeightUsed());
            log.setWeightUnit(entryDto.getWeightUnit());
            log.setDurationPerformedSeconds(entryDto.getDurationPerformedSeconds());
            log.setRestTimeSeconds(entryDto.getRestTimeSeconds());
            log.setNotes(entryDto.getNotes());
            log.setWorkoutPlanId(entryDto.getWorkoutPlanId());
            log.setDailyWorkoutDayName(entryDto.getDailyWorkoutDayName());
            logsToSave.add(log);
        }

        if (!logsToSave.isEmpty()) {
            try {
                workoutTrackerService.saveMultipleWorkoutLogs(logsToSave, principal.getName());
                redirectAttributes.addFlashAttribute("successMessage", "Workout logged successfully!");
            } catch (Exception e) {
                logger.error("Error saving workout logs for user {}", principal.getName(), e);
                redirectAttributes.addFlashAttribute("errorMessage", "Error saving workout: " + e.getMessage());
                // It might be better to return to the form with errors here
                // For now, redirecting to dashboard.
            }
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "No workout entries were provided to log.");
        }

        return "redirect:/dashboard"; // Or to a workout history page
    }

    @GetMapping("/history")
    public String showWorkoutHistory(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        List<WorkoutLog> history = workoutTrackerService.getWorkoutLogsForUser(principal.getName());
        model.addAttribute("workoutHistory", history);
        return "workout-history"; // Create this template
    }

    // TODO: Add @PostMapping for deleting logs from history page if needed
}
