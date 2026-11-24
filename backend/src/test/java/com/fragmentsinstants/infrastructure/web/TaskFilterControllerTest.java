package com.fragmentsinstants.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fragmentsinstants.domain.Priority;
import com.fragmentsinstants.domain.TaskStatus;
import com.fragmentsinstants.infrastructure.persistence.TaskEntity;
import com.fragmentsinstants.infrastructure.persistence.TaskRepository;


@SpringBootTest (properties = "spring.datasource.url=jdbc:sqlite:target/task-filter-controller-test.db?foreign_keys=on")
@AutoConfigureMockMvc 
@Transactional 
class TaskFilterControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach 
    void createTasks(){
        TaskEntity echeanceHier = new TaskEntity("TaskA");
        echeanceHier.setDueDate(LocalDate.now().minusDays(1));
        echeanceHier.setPriority(Priority.NORMALE);

        TaskEntity echeanceAujourdhui = new TaskEntity("TaskB");
        echeanceAujourdhui.setDueDate(LocalDate.now());
        echeanceAujourdhui.setPriority(Priority.HAUTE);

        TaskEntity sansEcheance = new TaskEntity("TaskC");
        sansEcheance.setPriority(Priority.NORMALE);

        TaskEntity sansEcheanceTermine = new TaskEntity("TaskD");
        sansEcheanceTermine.setStatus(TaskStatus.TERMINEE);
        sansEcheanceTermine.setCompletedAt(Instant.now());
        sansEcheanceTermine.setPriority(Priority.HAUTE);

        TaskEntity echeanceAujourdhuiTermine = new TaskEntity("TaskE");
        echeanceAujourdhuiTermine.setStatus(TaskStatus.TERMINEE);
        echeanceAujourdhuiTermine.setCompletedAt(Instant.now());
        echeanceAujourdhuiTermine.setPriority(Priority.CRITIQUE);

        TaskEntity enPause = new TaskEntity("TaskF");
        enPause.setStatus(TaskStatus.EN_PAUSE);
        
        taskRepository.save(echeanceHier);
        taskRepository.save(echeanceAujourdhui);
        taskRepository.save(sansEcheance);
        taskRepository.save(sansEcheanceTermine);
        taskRepository.save(echeanceAujourdhuiTermine);
        taskRepository.save(enPause);
    }

    @Test 
    void testFiltreToday() throws Exception{
        mockMvc.perform(get("/tasks").param("filter", "today"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.title == 'TaskA')]").exists())
        .andExpect(jsonPath("$[?(@.title == 'TaskB')]").exists())
        .andExpect(jsonPath("$[?(@.title == 'TaskD')]").doesNotExist())
        .andExpect(jsonPath("$[?(@.title == 'TaskE')]").doesNotExist())
        .andExpect(jsonPath("$[?(@.title == 'TaskC')]").doesNotExist())
        .andExpect(jsonPath("$[?(@.title == 'TaskF')]").doesNotExist());
    }

    @Test 
    void testFiltreImportant() throws Exception{
        mockMvc.perform(get("/tasks").param("filter", "important"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.title == 'TaskA')]").doesNotExist())
        .andExpect(jsonPath("$[?(@.title == 'TaskB')]").exists())
        .andExpect(jsonPath("$[?(@.title == 'TaskD')]").doesNotExist())
        .andExpect(jsonPath("$[?(@.title == 'TaskE')]").doesNotExist())
        .andExpect(jsonPath("$[?(@.title == 'TaskC')]").doesNotExist())
        .andExpect(jsonPath("$[?(@.title == 'TaskF')]").doesNotExist());
    }

    @Test 
    void testFiltreCompleted() throws Exception{
        mockMvc.perform(get("/tasks").param("filter", "completed"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.title == 'TaskA')]").doesNotExist())
        .andExpect(jsonPath("$[?(@.title == 'TaskB')]").doesNotExist())
        .andExpect(jsonPath("$[?(@.title == 'TaskD')]").exists())
        .andExpect(jsonPath("$[?(@.title == 'TaskE')]").exists())
        .andExpect(jsonPath("$[?(@.title == 'TaskC')]").doesNotExist())
        .andExpect(jsonPath("$[?(@.title == 'TaskF')]").doesNotExist());
    }
    
}
