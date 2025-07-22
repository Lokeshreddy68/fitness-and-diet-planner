package com.fitnessplanner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnessplanner.dto.DietPlanDto;
import com.fitnessplanner.model.DietProfile;
import com.fitnessplanner.model.User;
import com.fitnessplanner.repository.DietProfileRepository;
import com.fitnessplanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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
public class DietService {

    private static final Logger logger = LoggerFactory.getLogger(DietService.class);
    private List<DietPlanDto> allDietPlans;

    private final DietProfileRepository dietProfileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PlanHistoryService planHistoryService; // Added

    @Autowired
    public DietService(DietProfileRepository dietProfileRepository,
                       UserRepository userRepository,
                       ObjectMapper objectMapper,
                       PlanHistoryService planHistoryService) { // Added
        this.dietProfileRepository = dietProfileRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.planHistoryService = planHistoryService; // Initialize
    }

    @PostConstruct
    public void loadDietPlans() {
        try (InputStream inputStream = new ClassPathResource("data/diets.json").getInputStream()) {
            allDietPlans = objectMapper.readValue(inputStream, new TypeReference<List<DietPlanDto>>() {});
            logger.info("Successfully loaded {} diet plans from JSON.", allDietPlans.size());
        } catch (IOException e) {
            logger.error("Failed to load diet plans from diets.json", e);
            allDietPlans = Collections.emptyList();
        }
    }

    public List<DietPlanDto> getAllDietPlans() {
        return allDietPlans;
    }

    public Optional<DietPlanDto> getDietPlanById(String planId) {
        if (planId == null || allDietPlans == null) {
            return Optional.empty();
        }
        return allDietPlans.stream()
                .filter(plan -> planId.equals(plan.getPlanId()))
                .findFirst();
    }

    @Transactional
    public DietProfile saveOrUpdateDietProfile(DietProfile profile, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String oldPlanId = null;
        Optional<DietProfile> existingProfileOpt = dietProfileRepository.findByUser(user);
        DietProfile profileToSave;

        if (existingProfileOpt.isPresent()) {
            profileToSave = existingProfileOpt.get();
            oldPlanId = profileToSave.getCurrentDietPlanId();
            profileToSave.setDietType(profile.getDietType());
            profileToSave.setMealFrequency(profile.getMealFrequency());
            profileToSave.setPrimaryGoal(profile.getPrimaryGoal());
            profileToSave.setAllergies(profile.getAllergies());
            profileToSave.setIntermittentFasting(profile.isIntermittentFasting());
            profileToSave.setTargetCalories(profile.getTargetCalories());
            profileToSave.setUpdatedDate(LocalDate.now());
        } else {
            profile.setUser(user);
            profile.setCreatedDate(LocalDate.now());
            profileToSave = profile;
        }

        matchAndAssignDietPlan(profileToSave, user.getUsername(), oldPlanId);
        return dietProfileRepository.save(profileToSave);
    }

    private void matchAndAssignDietPlan(DietProfile profile, String username, String oldPlanId) {
        if (allDietPlans == null || allDietPlans.isEmpty()) {
            logger.warn("No diet plans loaded. Cannot assign a plan to user {}", username);
            if (oldPlanId != null) {
                planHistoryService.endSpecificPlan(username, oldPlanId, UserPlanHistory.PlanType.DIET, LocalDate.now(), "No new diet plan matched, profile updated.");
            }
            profile.setCurrentDietPlanId(null);
            return;
        }

        List<DietPlanDto> matchedPlans = allDietPlans.stream()
                // Match diet type
                .filter(plan -> plan.getTargetDietType().equalsIgnoreCase(profile.getDietType()))
                // Match primary goal
                .filter(plan -> plan.getTargetGoal().equalsIgnoreCase(profile.getPrimaryGoal()))
                // Match fasting preference
                .filter(plan -> profile.isIntermittentFasting() ? plan.isSupportsFasting() : true)
                // Filter out plans that contain user's allergies
                .filter(plan -> {
                    if (CollectionUtils.isEmpty(profile.getAllergies())) {
                        return true; // No allergies, plan is suitable
                    }
                    if (CollectionUtils.isEmpty(plan.getAvoidAllergies())) {
                        return true; // Plan has no restricted allergies
                    }
                    // Check if any of the user's allergies are in the plan's avoidAllergies list
                    return profile.getAllergies().stream()
                                  .noneMatch(userAllergy -> plan.getAvoidAllergies().stream()
                                           .anyMatch(avoid -> avoid.equalsIgnoreCase(userAllergy)));
                })
                .collect(Collectors.toList());

        if (!matchedPlans.isEmpty()) {
            DietPlanDto bestMatch = matchedPlans.get(0);
            String newPlanId = bestMatch.getPlanId();

            if (oldPlanId == null || !oldPlanId.equals(newPlanId)) {
                planHistoryService.startNewPlan(username, newPlanId, bestMatch.getPlanName(), UserPlanHistory.PlanType.DIET, LocalDate.now());
                profile.setCurrentDietPlanId(newPlanId);
                logger.info("Assigned and started new diet plan '{}' ({}) for user {}", bestMatch.getPlanName(), newPlanId, username);
            } else {
                profile.setCurrentDietPlanId(newPlanId); // Re-matched same plan
                logger.info("User {} re-matched with the same diet plan: {} ({})", username, bestMatch.getPlanName(), newPlanId);
            }

            // If user hasn't set target calories, or it's zero, use plan's estimate
            if (profile.getTargetCalories() == null || profile.getTargetCalories() == 0) {
                profile.setTargetCalories(bestMatch.getEstimatedDailyCalories());
            }
        } else {
            logger.warn("No suitable diet plan found for user {} with profile: {}",
                    username, profile.toString());
            if (oldPlanId != null) {
                planHistoryService.endSpecificPlan(username, oldPlanId, UserPlanHistory.PlanType.DIET, LocalDate.now(), "No new suitable diet plan found after profile update.");
            }
            profile.setCurrentDietPlanId(null);
        }
    }

    public Optional<DietProfile> getDietProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return dietProfileRepository.findByUser(user);
    }

    public Optional<DietPlanDto> getCurrentDietPlanForUser(String username) {
        return getDietProfileByUsername(username)
                .flatMap(profile -> Optional.ofNullable(profile.getCurrentDietPlanId()))
                .flatMap(this::getDietPlanById);
    }
}
