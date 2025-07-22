package com.fitnessplanner.service;

import com.fitnessplanner.dto.DietPlanDto;
import com.fitnessplanner.dto.WorkoutPlanDto;
import com.fitnessplanner.model.DietProfile;
import com.fitnessplanner.model.UserPlanHistory;
import com.fitnessplanner.model.WorkoutProfile;
import com.fitnessplanner.repository.DietProfileRepository;
import com.fitnessplanner.repository.WorkoutProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * This service handles plan changes explicitly requested by the user,
 * bypassing the usual matching logic.
 */
@Service
public class UserRequestedPlanChangeService {

    private static final Logger logger = LoggerFactory.getLogger(UserRequestedPlanChangeService.class);

    private final WorkoutProfileRepository workoutProfileRepository;
    private final DietProfileRepository dietProfileRepository;
    private final PlanHistoryService planHistoryService;
    private final WorkoutService workoutService; // To get plan details
    private final DietService dietService;     // To get plan details

    @Autowired
    public UserRequestedPlanChangeService(WorkoutProfileRepository workoutProfileRepository,
                                       DietProfileRepository dietProfileRepository,
                                       PlanHistoryService planHistoryService,
                                       WorkoutService workoutService,
                                       DietService dietService) {
        this.workoutProfileRepository = workoutProfileRepository;
        this.dietProfileRepository = dietProfileRepository;
        this.planHistoryService = planHistoryService;
        this.workoutService = workoutService;
        this.dietService = dietService;
    }

    @Transactional
    public boolean changeUserWorkoutPlan(String username, String newPlanId) {
        Optional<WorkoutProfile> profileOpt = workoutService.getWorkoutProfileByUsername(username);
        Optional<WorkoutPlanDto> newPlanOpt = workoutService.getWorkoutPlanById(newPlanId);

        if (profileOpt.isEmpty() || newPlanOpt.isEmpty()) {
            logger.warn("Cannot change workout plan for user {}. Profile or new plan {} not found.", username, newPlanId);
            return false;
        }

        WorkoutProfile profile = profileOpt.get();
        WorkoutPlanDto newPlan = newPlanOpt.get();
        String oldPlanId = profile.getCurrentWorkoutPlanId();

        if (newPlanId.equals(oldPlanId)) {
            logger.info("User {} requested to change to the same workout plan {}. No change made.", username, newPlanId);
            return true; // No change needed, but not an error
        }

        // End the old plan history entry if it exists and is different
        // The startNewPlan in PlanHistoryService will handle ending the generic active plan.
        // If oldPlanId is null, startNewPlan will just start the new one.

        planHistoryService.startNewPlan(username, newPlanId, newPlan.getPlanName(), UserPlanHistory.PlanType.WORKOUT, LocalDate.now());
        profile.setCurrentWorkoutPlanId(newPlanId);
        workoutProfileRepository.save(profile);

        logger.info("User {} successfully changed workout plan from '{}' to '{}' ({}).", username, oldPlanId, newPlanId, newPlan.getPlanName());
        return true;
    }

    @Transactional
    public boolean discontinueUserWorkoutPlan(String username) {
        Optional<WorkoutProfile> profileOpt = workoutService.getWorkoutProfileByUsername(username);
        if (profileOpt.isEmpty()) {
            logger.warn("Cannot discontinue workout plan for user {}. Profile not found.", username);
            return false;
        }

        WorkoutProfile profile = profileOpt.get();
        String currentPlanId = profile.getCurrentWorkoutPlanId();

        if (currentPlanId == null) {
            logger.info("User {} has no active workout plan to discontinue.", username);
            return true; // No plan to discontinue
        }

        Optional<WorkoutPlanDto> planDetailsOpt = workoutService.getWorkoutPlanById(currentPlanId);
        String planName = planDetailsOpt.map(WorkoutPlanDto::getPlanName).orElse("Unknown Plan");

        planHistoryService.endSpecificPlan(username, currentPlanId, UserPlanHistory.PlanType.WORKOUT, LocalDate.now(), "Discontinued by user.");
        profile.setCurrentWorkoutPlanId(null);
        workoutProfileRepository.save(profile);

        logger.info("User {} discontinued workout plan '{}' ({}).", username, planName, currentPlanId);
        return true;
    }


    @Transactional
    public boolean changeUserDietPlan(String username, String newPlanId) {
        Optional<DietProfile> profileOpt = dietService.getDietProfileByUsername(username);
        Optional<DietPlanDto> newPlanOpt = dietService.getDietPlanById(newPlanId);

        if (profileOpt.isEmpty() || newPlanOpt.isEmpty()) {
            logger.warn("Cannot change diet plan for user {}. Profile or new plan {} not found.", username, newPlanId);
            return false;
        }

        DietProfile profile = profileOpt.get();
        DietPlanDto newPlan = newPlanOpt.get();
        String oldPlanId = profile.getCurrentDietPlanId();

        if (newPlanId.equals(oldPlanId)) {
            logger.info("User {} requested to change to the same diet plan {}. No change made.", username, newPlanId);
            return true;
        }

        planHistoryService.startNewPlan(username, newPlanId, newPlan.getPlanName(), UserPlanHistory.PlanType.DIET, LocalDate.now());
        profile.setCurrentDietPlanId(newPlanId);
        // Update target calories if user hasn't set one, or if the new plan's estimate is different and user's is default
        if (profile.getTargetCalories() == null || profile.getTargetCalories() == 0 ||
            (oldPlanId != null && dietService.getDietPlanById(oldPlanId).map(DietPlanDto::getEstimatedDailyCalories).orElse(0).equals(profile.getTargetCalories()))) {
            profile.setTargetCalories(newPlan.getEstimatedDailyCalories());
        }
        dietProfileRepository.save(profile);

        logger.info("User {} successfully changed diet plan from '{}' to '{}' ({}).", username, oldPlanId, newPlanId, newPlan.getPlanName());
        return true;
    }

    @Transactional
    public boolean discontinueUserDietPlan(String username) {
        Optional<DietProfile> profileOpt = dietService.getDietProfileByUsername(username);
        if (profileOpt.isEmpty()) {
            logger.warn("Cannot discontinue diet plan for user {}. Profile not found.", username);
            return false;
        }

        DietProfile profile = profileOpt.get();
        String currentPlanId = profile.getCurrentDietPlanId();

        if (currentPlanId == null) {
            logger.info("User {} has no active diet plan to discontinue.", username);
            return true;
        }

        Optional<DietPlanDto> planDetailsOpt = dietService.getDietPlanById(currentPlanId);
        String planName = planDetailsOpt.map(DietPlanDto::getPlanName).orElse("Unknown Plan");

        planHistoryService.endSpecificPlan(username, currentPlanId, UserPlanHistory.PlanType.DIET, LocalDate.now(), "Discontinued by user.");
        profile.setCurrentDietPlanId(null);
        // Optionally, we could nullify targetCalories if it was set by the plan, or leave it.
        // For now, leave it as is, user can change it in their diet profile form.
        dietProfileRepository.save(profile);

        logger.info("User {} discontinued diet plan '{}' ({}).", username, planName, currentPlanId);
        return true;
    }
}
