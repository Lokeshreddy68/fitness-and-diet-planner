package com.fitnessplanner.repository;

import com.fitnessplanner.model.MealLog;
import com.fitnessplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    List<MealLog> findByUserOrderByLogDateDescLogTimeDesc(User user);

    List<MealLog> findByUserAndLogDateOrderByLogTimeDesc(User user, LocalDate date);

    List<MealLog> findByUserAndLogDateBetweenOrderByLogDateDescLogTimeDesc(User user, LocalDate startDate, LocalDate endDate);

    // For calculating daily totals (example)
    @Query("SELECT SUM(m.calories) FROM MealLog m WHERE m.user = :user AND m.logDate = :date")
    Optional<Integer> sumCaloriesByUserAndLogDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT SUM(m.proteinGrams) FROM MealLog m WHERE m.user = :user AND m.logDate = :date")
    Optional<Double> sumProteinByUserAndLogDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT SUM(m.carbohydrateGrams) FROM MealLog m WHERE m.user = :user AND m.logDate = :date")
    Optional<Double> sumCarbohydratesByUserAndLogDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT SUM(m.fatGrams) FROM MealLog m WHERE m.user = :user AND m.logDate = :date")
    Optional<Double> sumFatByUserAndLogDate(@Param("user") User user, @Param("date") LocalDate date);

}
