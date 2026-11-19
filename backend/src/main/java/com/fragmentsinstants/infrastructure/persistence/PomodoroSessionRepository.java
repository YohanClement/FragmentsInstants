package com.fragmentsinstants.infrastructure.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSessionEntity, Long>{
    List<PomodoroSessionEntity> findByStartedAtBetween(Instant start, Instant end);

    List<PomodoroSessionEntity> findByTask(TaskEntity task);
    
}
