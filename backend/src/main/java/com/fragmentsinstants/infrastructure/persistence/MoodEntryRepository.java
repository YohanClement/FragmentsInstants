package com.fragmentsinstants.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MoodEntryRepository extends JpaRepository<MoodEntryEntity, Long> {
    Optional<MoodEntryEntity> findByDate(LocalDate date);

    Optional<MoodEntryEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
