package com.fragmentsinstants.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fragmentsinstants.domain.TaskStatus;
//import com.fragmentsinstants.domain.Priority;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    //List<TaskEntity> findByTitle(String title);

    //List<TaskEntity> findByStatus(TaskStatus status);
    
    List<TaskEntity> findByStatusNot(TaskStatus status);

    //List<TaskEntity> findByPriority(Priority priority);
    
    //List<TaskEntity> findByDueDate(LocalDate dueDate);

    //List<TaskEntity> findByDueDateBetween(LocalDate start, LocalDate end);
    
    List<TaskEntity> findByDueDateLessThanEqualAndStatusNot(LocalDate dueDate, TaskStatus status);

    List<TaskEntity> findByCompletedAtBetween(Instant start, Instant end);

}
