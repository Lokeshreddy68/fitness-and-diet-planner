package com.fitnessplanner.repository;

import com.fitnessplanner.model.User;
import com.fitnessplanner.model.UserPlanHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPlanHistoryRepository extends JpaRepository<UserPlanHistory, Long> {

    List<UserPlanHistory> findByUserOrderByStartDateDesc(User user);

    List<UserPlanHistory> findByUserAndPlanTypeOrderByStartDateDesc(User user, UserPlanHistory.PlanType planType);

    // Find the current (active) plan for a user of a specific type
    // An active plan is one that has a start date and no end date.
    @Query("SELECT uph FROM UserPlanHistory uph WHERE uph.user = :user AND uph.planType = :planType AND uph.endDate IS NULL ORDER BY uph.startDate DESC")
    List<UserPlanHistory> findActivePlansByUserAndType(@Param("user") User user, @Param("planType") UserPlanHistory.PlanType planType);

    // Find if a specific plan is currently active for a user
    Optional<UserPlanHistory> findByUserAndPlanIdAndPlanTypeAndEndDateIsNull(User user, String planId, UserPlanHistory.PlanType planType);

    // Find latest plan for a user of a specific type, regardless of active status
    Optional<UserPlanHistory> findTopByUserAndPlanTypeOrderByStartDateDesc(User user, UserPlanHistory.PlanType planType);
}
