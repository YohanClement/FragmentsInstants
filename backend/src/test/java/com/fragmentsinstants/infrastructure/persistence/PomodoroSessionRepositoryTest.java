package com.fragmentsinstants.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.fragmentsinstants.domain.CompletionStatus;
import com.fragmentsinstants.domain.Mode;

@DataJpaTest (properties = "spring.datasource.url=jdbc:sqlite:target/spike-test.db?foreign_keys=on")
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
public class PomodoroSessionRepositoryTest {
    @Autowired
    private PomodoroSessionRepository pomodoroRepository;

    @Autowired
    private TaskRepository taskRepository;
    
    @Test
    void testFindByStartedAtBetween() {
        PomodoroSessionEntity pomodoro1 = new PomodoroSessionEntity(Mode.FOCUS, Instant.now(), Instant.now().plusSeconds(604800), 604800, CompletionStatus.TERMINEE);
        PomodoroSessionEntity pomodoro2 = new PomodoroSessionEntity(Mode.FOCUS, Instant.now().minusSeconds(604800), Instant.now(), 3600, CompletionStatus.INTERROMPUE);

        pomodoroRepository.save(pomodoro1);
        pomodoroRepository.save(pomodoro2);

        List<PomodoroSessionEntity> result = pomodoroRepository.findByStartedAtBetween(Instant.now().minusSeconds(60), Instant.now().plusSeconds(1209600));
        
        assertThat(result).contains(pomodoro1);
        assertThat(result).doesNotContain(pomodoro2);
    }

    @Test
    void testFindByTask() {
        PomodoroSessionEntity pomodoro1 = new PomodoroSessionEntity(Mode.PAUSE_COURTE, Instant.now(), Instant.now(), 60, CompletionStatus.INTERROMPUE);
        PomodoroSessionEntity pomodoro2 = new PomodoroSessionEntity(Mode.PAUSE_LONGUE, Instant.now(), Instant.now(), 120, CompletionStatus.INTERROMPUE);
        TaskEntity task1 = new TaskEntity("task1");
        TaskEntity task2 = new TaskEntity("task2");

        pomodoro1.setTask(task1);
        pomodoro2.setTask(task2);

        taskRepository.save(task1);
        taskRepository.save(task2);

        pomodoroRepository.save(pomodoro1);
        pomodoroRepository.save(pomodoro2);

        List<PomodoroSessionEntity> result = pomodoroRepository.findByTask(task1);

        assertThat(result).contains(pomodoro1);
        assertThat(result).doesNotContain(pomodoro2);
    }
}
