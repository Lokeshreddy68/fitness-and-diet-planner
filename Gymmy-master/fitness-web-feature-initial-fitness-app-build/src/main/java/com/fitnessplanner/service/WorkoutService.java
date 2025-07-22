package com.fitnessplanner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnessplanner.dto.WorkoutPlanDto;
import com.fitnessplanner.model.User;
import com.fitnessplanner.model.WorkoutProfile;
import com.fitnessplanner.repository.UserRepository;
import com.fitnessplanner.repository.WorkoutProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fitnessplanner.model.UserPlanHistory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutService.class);
    private List<WorkoutPlanDto> allWorkoutPlans;

    private final WorkoutProfileRepository workoutProfileRepository;
    private final UserRepository userRepository; // To fetch User objects
    private final ObjectMapper objectMapper; // For JSON parsing
    private final PlanHistoryService planHistoryService; // Added for plan history

    @Autowired
    public WorkoutService(WorkoutProfileRepository workoutProfileRepository,
                          UserRepository userRepository,
                          ObjectMapper objectMapper,
                          PlanHistoryService planHistoryService) { // Added PlanHistoryService
        this.workoutProfileRepository = workoutProfileRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.planHistoryService = planHistoryService; // Initialize PlanHistoryService
    }

    @PostConstruct
    public void loadWorkoutPlans() {
        try (InputStream inputStream = new ClassPathResource("data/workouts.json").getInputStream()) {
            allWorkoutPlans = objectMapper.readValue(inputStream, new TypeReference<List<WorkoutPlanDto>>() {});
            logger.info("Successfully loaded {} workout plans from JSON.", allWorkoutPlans.size());
        } catch (IOException e) {
            logger.error("Failed to load workout plans from workouts.json", e);
            allWorkoutPlans = Collections.emptyList(); // Initialize to empty list on failure
        }
    }

    public List<WorkoutPlanDto> getAllWorkoutPlans() {
        return allWorkoutPlans;
    }

    public Optional<WorkoutPlanDto> getWorkoutPlanById(String planId) {
        if (planId == null || allWorkoutPlans == null) {
            return Optional.empty();
        }
        return allWorkoutPlans.stream()
                .filter(plan -> planId.equals(plan.getPlanId()))
                .findFirst();
    }

    @Transactional
    public WorkoutProfile saveOrUpdateWorkoutProfile(WorkoutProfile profile, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String oldPlanId = null;
        Optional<WorkoutProfile> existingProfileOpt = workoutProfileRepository.findByUser(user);

        WorkoutProfile profileToSave;
        if (existingProfileOpt.isPresent()) {
            profileToSave = existingProfileOpt.get();
            oldPlanId = profileToSave.getCurrentWorkoutPlanId(); // Get old plan ID before updating
            // Update existing profile fields
            profileToSave.setAge(profile.getAge());
            profileToSave.setWeight(profile.getWeight());
            profileToSave.setFitnessGoal(profile.getFitnessGoal());
            profileToSave.setActivityLevel(profile.getActivityLevel());
            profileToSave.setUpdatedDate(LocalDate.now());
        } else {
            // Create new profile
            profile.setUser(user);
            profile.setCreatedDate(LocalDate.now());
            profileToSave = profile;
        }

        matchAndAssignPlan(profileToSave, user.getUsername(), oldPlanId);
        return workoutProfileRepository.save(profileToSave);
    }

    private void matchAndAssignPlan(WorkoutProfile profile, String username, String oldPlanId) {
        if (allWorkoutPlans == null || allWorkoutPlans.isEmpty()) {
            logger.warn("No workout plans loaded. Cannot assign a plan to user {}", username);
            if (oldPlanId != null) { // If there was an old plan, end it
                Optional<WorkoutPlanDto> oldPlanDetails = getWorkoutPlanById(oldPlanId);
                planHistoryService.endSpecificPlan(username, oldPlanId, UserPlanHistory.PlanType.WORKOUT, LocalDate.now(), "No new plan matched, profile updated.");
            }
            profile.setCurrentWorkoutPlanId(null);
            return;
        }

        List<WorkoutPlanDto> matchedPlans = allWorkoutPlans.stream()
                .filter(plan -> plan.getTargetGoal().equalsIgnoreCase(profile.getFitnessGoal()))
                .filter(plan -> plan.getTargetActivityLevel().stream()
                        .anyMatch(level -> level.equalsIgnoreCase(profile.getActivityLevel())))
                .collect(Collectors.toList());

        if (!matchedPlans.isEmpty()) {
            WorkoutPlanDto bestMatch = matchedPlans.get(0);
            String newPlanId = bestMatch.getPlanId();

            if (oldPlanId == null || !oldPlanId.equals(newPlanId)) {
                // If there was an old plan and it's different, it's already ended by startNewPlan.
                // If there was no old plan, or if the new plan is different, start the new one.
                planHistoryService.startNewPlan(username, newPlanId, bestMatch.getPlanName(), UserPlanHistory.PlanType.WORKOUT, LocalDate.now());
                profile.setCurrentWorkoutPlanId(newPlanId);
                logger.info("Assigned and started new workout plan '{}' ({}) for user {}", bestMatch.getPlanName(), newPlanId, username);
            } else {
                // Plan is the same as before, no change in history needed, just ensure ID is set.
                profile.setCurrentWorkoutPlanId(newPlanId);
                logger.info("User {} re-matched with the same workout plan: {} ({})", username, bestMatch.getPlanName(), newPlanId);
            }
        } else {
            logger.warn("No suitable workout plan found for user {} with goal {} and activity level {}",
                    username, profile.getFitnessGoal(), profile.getActivityLevel());
            if (oldPlanId != null) {
                // If there was an old plan and no new one is found, end the old plan.
                Optional<WorkoutPlanDto> oldPlanDetails = getWorkoutPlanById(oldPlanId);
                 planHistoryService.endSpecificPlan(username, oldPlanId, UserPlanHistory.PlanType.WORKOUT, LocalDate.now(), "No new suitable plan found after profile update.");
            }
            profile.setCurrentWorkoutPlanId(null); // No suitable plan found or assigned
        }
    }

    public Optional<WorkoutProfile> getWorkoutProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return workoutProfileRepository.findByUser(user);
    }

    public Optional<WorkoutPlanDto> getCurrentWorkoutPlanForUser(String username) {
        return getWorkoutProfileByUsername(username)
                .flatMap(profile -> Optional.ofNullable(profile.getCurrentWorkoutPlanId()))
                .flatMap(this::getWorkoutPlanById);
    }
}
