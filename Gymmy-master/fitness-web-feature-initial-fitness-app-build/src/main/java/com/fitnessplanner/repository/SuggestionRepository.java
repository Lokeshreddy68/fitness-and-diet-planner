package com.fitnessplanner.repository;

import com.fitnessplanner.model.Suggestion;
import com.fitnessplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    List<Suggestion> findByUserOrderByCreatedDateDesc(User user);

    List<Suggestion> findByUserAndAcknowledgedOrderByCreatedDateDesc(User user, boolean acknowledged);

    List<Suggestion> findByUserAndCreatedDateAfterOrderByCreatedDateDesc(User user, LocalDate date);

    // Could add more specific finders, e.g., by type or severity
}
