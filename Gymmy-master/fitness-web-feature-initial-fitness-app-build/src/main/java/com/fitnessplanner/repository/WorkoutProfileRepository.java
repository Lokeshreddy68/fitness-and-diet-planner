package com.fitnessplanner.repository;

import com.fitnessplanner.model.User;
import com.fitnessplanner.model.WorkoutProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkoutProfileRepository extends JpaRepository<WorkoutProfile, Long> {

    Optional<WorkoutProfile> findByUser(User user);

    Optional<WorkoutProfile> findByUserId(Long userId);
}
