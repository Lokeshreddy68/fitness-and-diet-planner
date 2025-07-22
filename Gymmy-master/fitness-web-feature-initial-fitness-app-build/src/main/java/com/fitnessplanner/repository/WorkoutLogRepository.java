package com.fitnessplanner.repository;

import com.fitnessplanner.model.User;
import com.fitnessplanner.model.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    List<WorkoutLog> findByUserOrderByWorkoutDateDescWorkoutTimeDesc(User user);

    List<WorkoutLog> findByUserAndWorkoutDateOrderByWorkoutTimeDesc(User user, LocalDate date);

    List<WorkoutLog> findByUserAndWorkoutDateBetweenOrderByWorkoutDateDescWorkoutTimeDesc(User user, LocalDate startDate, LocalDate endDate);

    // For analytics or specific exercise progress
    List<WorkoutLog> findByUserAndExerciseNameOrderByWorkoutDateDesc(User user, String exerciseName);
}
