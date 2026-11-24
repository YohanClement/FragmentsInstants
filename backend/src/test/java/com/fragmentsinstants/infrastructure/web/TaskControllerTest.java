package com.fragmentsinstants.infrastructure.web;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fragmentsinstants.domain.Priority;
import com.fragmentsinstants.infrastructure.persistence.TaskEntity;
import com.fragmentsinstants.infrastructure.persistence.TaskRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest (properties = "spring.datasource.url=jdbc:sqlite:target/task-controller-test.db?foreign_keys=on")
@AutoConfigureMockMvc
@Transactional 
class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    Long taskId;

    @BeforeEach 
    void createTaskEntity(){
        TaskEntity taskEntity = new TaskEntity("TaskB");
        taskEntity.setEstimatedMinutes(30);
        taskEntity.setPriority(Priority.CRITIQUE);
        taskRepository.save(taskEntity);
        taskId = taskEntity.getId();
    }

    @Test
    void testPostTasks() throws Exception{
        mockMvc.perform(post("/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
                "title": "TaskA",
                "priority": "NORMALE",
                "estimatedMinutes": 30            
            }
        """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("TaskA"))
        .andExpect(jsonPath("$.tags[0].name").value("Autre"));
    }
    
    @Test
    void testGetTaskByID() throws Exception{
        mockMvc.perform(get("/tasks/"+ taskId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estimatedMinutes").value(30))
        .andExpect(jsonPath("$.realMinutes").value(0))
        .andExpect(jsonPath("$.remainingMinutes").value(30));
    }

    @Test
    void testGetTasksFilterAll() throws Exception{
        mockMvc.perform(get("/tasks").param("filter", "all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.title == 'TaskB')]").exists());
    }

}