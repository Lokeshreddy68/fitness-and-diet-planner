package com.fitnessplanner.service;

import com.fitnessplanner.model.User;
import com.fitnessplanner.model.WorkoutLog;
import com.fitnessplanner.repository.UserRepository;
import com.fitnessplanner.repository.WorkoutLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class WorkoutTrackerService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutTrackerService.class);

    private final WorkoutLogRepository workoutLogRepository;
    private final UserRepository userRepository;

    @Autowired
    public WorkoutTrackerService(WorkoutLogRepository workoutLogRepository, UserRepository userRepository) {
        this.workoutLogRepository = workoutLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkoutLog saveWorkoutLog(WorkoutLog workoutLog, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        workoutLog.setUser(user);

        // Ensure workoutDate is set, default to today if not already
        if (workoutLog.getWorkoutDate() == null) {
            workoutLog.setWorkoutDate(LocalDate.now());
        }

        logger.info("Saving workout log for user {}: {}", username, workoutLog.getExerciseName());
        return workoutLogRepository.save(workoutLog);
    }

    @Transactional
    public List<WorkoutLog> saveMultipleWorkoutLogs(List<WorkoutLog> workoutLogs, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        for (WorkoutLog log : workoutLogs) {
            log.setUser(user);
            if (log.getWorkoutDate() == null) {
                log.setWorkoutDate(LocalDate.now());
            }
        }
        logger.info("Saving batch of {} workout logs for user {}", workoutLogs.size(), username);
        return workoutLogRepository.saveAll(workoutLogs);
    }


    public Optional<WorkoutLog> getWorkoutLogById(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return workoutLogRepository.findById(id)
                .filter(log -> log.getUser().equals(user)); // Ensure the log belongs to the user
    }

    public List<WorkoutLog> getWorkoutLogsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return workoutLogRepository.findByUserOrderByWorkoutDateDescWorkoutTimeDesc(user);
    }

    public List<WorkoutLog> getWorkoutLogsForUserOnDate(String username, LocalDate date) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return workoutLogRepository.findByUserAndWorkoutDateOrderByWorkoutTimeDesc(user, date);
    }

    public List<WorkoutLog> getWorkoutLogsForUserBetweenDates(String username, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return workoutLogRepository.findByUserAndWorkoutDateBetweenOrderByWorkoutDateDescWorkoutTimeDesc(user, startDate, endDate);
    }

    @Transactional
    public void deleteWorkoutLog(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        WorkoutLog log = workoutLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout log not found with id: " + id));

        if (!log.getUser().equals(user)) {
            throw new SecurityException("User " + username + " is not authorized to delete log " + id);
        }
        logger.info("Deleting workout log with id {} for user {}", id, username);
        workoutLogRepository.deleteById(id);
    }

    // Additional methods can be added for updating, specific queries for analytics, etc.
}
