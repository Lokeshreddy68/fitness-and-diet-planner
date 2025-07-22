package com.fitnessplanner.service;

import com.fitnessplanner.model.MealLog;
import com.fitnessplanner.model.User;
import com.fitnessplanner.repository.MealLogRepository;
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
public class DietTrackerService {

    private static final Logger logger = LoggerFactory.getLogger(DietTrackerService.class);

    private final MealLogRepository mealLogRepository;
    private final UserRepository userRepository;

    @Autowired
    public DietTrackerService(MealLogRepository mealLogRepository, UserRepository userRepository) {
        this.mealLogRepository = mealLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MealLog saveMealLog(MealLog mealLog, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        mealLog.setUser(user);

        if (mealLog.getLogDate() == null) {
            mealLog.setLogDate(LocalDate.now());
        }

        logger.info("Saving meal log for user {}: {}", username, mealLog.getFoodItemOrMealDescription());
        return mealLogRepository.save(mealLog);
    }

    @Transactional
    public List<MealLog> saveMultipleMealLogs(List<MealLog> mealLogs, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        for (MealLog log : mealLogs) {
            log.setUser(user);
            if (log.getLogDate() == null) {
                log.setLogDate(LocalDate.now());
            }
        }
        logger.info("Saving batch of {} meal logs for user {}", mealLogs.size(), username);
        return mealLogRepository.saveAll(mealLogs);
    }

    public Optional<MealLog> getMealLogById(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return mealLogRepository.findById(id)
                .filter(log -> log.getUser().equals(user));
    }

    public List<MealLog> getMealLogsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return mealLogRepository.findByUserOrderByLogDateDescLogTimeDesc(user);
    }

    public List<MealLog> getMealLogsForUserOnDate(String username, LocalDate date) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return mealLogRepository.findByUserAndLogDateOrderByLogTimeDesc(user, date);
    }

    public List<MealLog> getMealLogsForUserBetweenDates(String username, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return mealLogRepository.findByUserAndLogDateBetweenOrderByLogDateDescLogTimeDesc(user, startDate, endDate);
    }

    @Transactional
    public void deleteMealLog(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        MealLog log = mealLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meal log not found with id: " + id));

        if (!log.getUser().equals(user)) {
            throw new SecurityException("User " + username + " is not authorized to delete meal log " + id);
        }
        logger.info("Deleting meal log with id {} for user {}", id, username);
        mealLogRepository.deleteById(id);
    }

    // --- Methods for Daily Summaries ---
    public Integer getDailyCaloriesForUser(String username, LocalDate date) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return mealLogRepository.sumCaloriesByUserAndLogDate(user, date).orElse(0);
    }

    public Double getDailyProteinForUser(String username, LocalDate date) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return mealLogRepository.sumProteinByUserAndLogDate(user, date).orElse(0.0);
    }

     public Double getDailyCarbohydratesForUser(String username, LocalDate date) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return mealLogRepository.sumCarbohydratesByUserAndLogDate(user, date).orElse(0.0);
    }

     public Double getDailyFatForUser(String username, LocalDate date) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return mealLogRepository.sumFatByUserAndLogDate(user, date).orElse(0.0);
    }
}
