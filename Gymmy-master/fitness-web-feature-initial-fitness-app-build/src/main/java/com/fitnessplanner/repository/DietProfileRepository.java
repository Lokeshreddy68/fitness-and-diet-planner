package com.fitnessplanner.repository;

import com.fitnessplanner.model.DietProfile;
import com.fitnessplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DietProfileRepository extends JpaRepository<DietProfile, Long> {

    Optional<DietProfile> findByUser(User user);

    Optional<DietProfile> findByUserId(Long userId);
}
