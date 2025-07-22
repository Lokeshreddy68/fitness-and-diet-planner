package com.fitnessplanner.service;

import com.fitnessplanner.model.User;
import com.fitnessplanner.model.UserPlanHistory;
import com.fitnessplanner.repository.UserPlanHistoryRepository;
import com.fitnessplanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PlanHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(PlanHistoryService.class);

    private final UserPlanHistoryRepository userPlanHistoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public PlanHistoryService(UserPlanHistoryRepository userPlanHistoryRepository, UserRepository userRepository) {
        this.userPlanHistoryRepository = userPlanHistoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserPlanHistory startNewPlan(String username, String planId, String planName, UserPlanHistory.PlanType planType, LocalDate startDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // End any currently active plan of the same type for this user
        endActivePlan(user, planType, startDate.minusDays(1), "Changed to new plan: " + planName);

        UserPlanHistory newPlanEntry = new UserPlanHistory(user, planId, planName, planType, startDate);
        logger.info("Starting new {} plan '{}' for user {} on {}", planType, planName, username, startDate);
        return userPlanHistoryRepository.save(newPlanEntry);
    }

    @Transactional
    public void endActivePlan(User user, UserPlanHistory.PlanType planType, LocalDate endDate, String reason) {
        List<UserPlanHistory> activePlans = userPlanHistoryRepository.findActivePlansByUserAndType(user, planType);
        for (UserPlanHistory activePlan : activePlans) {
            if (activePlan.getEndDate() == null) { // Double check, though query should handle this
                activePlan.setEndDate(endDate);
                activePlan.setReasonForEnding(reason);
                userPlanHistoryRepository.save(activePlan);
                logger.info("Ended {} plan '{}' for user {} on {} due to: {}",
                        planType, activePlan.getPlanName(), user.getUsername(), endDate, reason);
            }
        }
    }

    @Transactional
    public void endSpecificPlan(String username, String planId, UserPlanHistory.PlanType planType, LocalDate endDate, String reason) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Optional<UserPlanHistory> planToEndOpt = userPlanHistoryRepository.findByUserAndPlanIdAndPlanTypeAndEndDateIsNull(user, planId, planType);

        if (planToEndOpt.isPresent()) {
            UserPlanHistory planToEnd = planToEndOpt.get();
            planToEnd.setEndDate(endDate);
            planToEnd.setReasonForEnding(reason);
            userPlanHistoryRepository.save(planToEnd);
            logger.info("Specifically ended {} plan '{}' ({}) for user {} on {} due to: {}",
                        planType, planToEnd.getPlanName(), planId, username, endDate, reason);
        } else {
            logger.warn("Could not find active {} plan with ID '{}' for user {} to end.", planType, planId, username);
        }
    }


    public List<UserPlanHistory> getPlanHistoryForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return userPlanHistoryRepository.findByUserOrderByStartDateDesc(user);
    }

    public List<UserPlanHistory> getPlanHistoryForUserByType(String username, UserPlanHistory.PlanType planType) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return userPlanHistoryRepository.findByUserAndPlanTypeOrderByStartDateDesc(user, planType);
    }

    public Optional<UserPlanHistory> getCurrentActivePlanByType(String username, UserPlanHistory.PlanType planType) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        List<UserPlanHistory> activePlans = userPlanHistoryRepository.findActivePlansByUserAndType(user, planType);
        if (activePlans.isEmpty()) {
            return Optional.empty();
        }
        // Assuming only one plan of a type can be active, return the first (should be latest by query)
        return Optional.of(activePlans.get(0));
    }
}
