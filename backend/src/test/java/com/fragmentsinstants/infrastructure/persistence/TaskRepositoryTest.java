package com.fragmentsinstants.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.fragmentsinstants.domain.TaskStatus;

@DataJpaTest (properties = "spring.datasource.url=jdbc:sqlite:target/spike-test.db?foreign_keys=on")
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
public class TaskRepositoryTest {
    @Autowired 
    private TaskRepository taskRepository;

    @Test
    void testFindByCompletedAtBetween() {
        TaskEntity task1 = new TaskEntity("task1");
        TaskEntity task2 = new TaskEntity("task2");
        TaskEntity task3 = new TaskEntity("task3");
        
        task1.setCompletedAt(Instant.now());
        task3.setCompletedAt(Instant.now().minusSeconds(120));
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        List<TaskEntity> result = taskRepository.findByCompletedAtBetween(Instant.now().minusSeconds(60), Instant.now().plusSeconds(60));

        assertThat(result).contains(task1);
        assertThat(result).doesNotContain(task2);
        assertThat(result).doesNotContain(task3);
    }

    @Test
    void testFindByDueDateLessThanEqualAndStatusNot() {
        TaskEntity task1 = new TaskEntity("task1");
        TaskEntity task2 = new TaskEntity("task2");
        TaskEntity task3 = new TaskEntity("task3");
        TaskEntity task4 = new TaskEntity("task4");
        TaskEntity task5 = new TaskEntity("task5");
        task1.setDueDate(LocalDate.now().minusDays(1));
        task2.setStatus(TaskStatus.EN_PAUSE);
        task2.setDueDate(LocalDate.now());
        task3.setDueDate(LocalDate.now().plusDays(1));
        task4.setStatus(TaskStatus.TERMINEE);
        task4.setDueDate(LocalDate.now().minusDays(1));
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);

        List<TaskEntity> result = taskRepository.findByDueDateLessThanEqualAndStatusNot(LocalDate.now(), TaskStatus.TERMINEE);

        assertThat(result).contains(task2);
        assertThat(result).contains(task1);
        assertThat(result).doesNotContain(task3);
        assertThat(result).doesNotContain(task4);
        assertThat(result).doesNotContain(task5);
    }

    @Test
    void testFindByStatusNot() {
        TaskEntity task1 = new TaskEntity("task1");
        TaskEntity task2 = new TaskEntity("task2");
        task2.setStatus(TaskStatus.TERMINEE);

        taskRepository.save(task1);
        taskRepository.save(task2);

        List<TaskEntity> result = taskRepository.findByStatusNot(TaskStatus.TERMINEE);

        assertThat(result).contains(task1);
        assertThat(result).doesNotContain(task2);
        
    }
}
