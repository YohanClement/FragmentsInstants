package com.fragmentsinstants.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.fragmentsinstants.domain.Priority;
import com.fragmentsinstants.domain.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;

@Entity @Table (name = "tasks")
public class TaskEntity {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @JdbcTypeCode (SqlTypes.INTEGER)
    private Long id; 
    
    @Column (name = "title", nullable = false, length = 100)
    private String title;

    @Column (name = "description", length = 1000)
    private String description;

    @Column (name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column (name = "priority", nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column (name = "due_date")
    private LocalDate dueDate;

    @Column (name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column (name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp 
    @Column (name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column (name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToMany (fetch = FetchType.LAZY)
    @JoinTable (name = "task_tag", joinColumns = @JoinColumn (name = "task_id"), inverseJoinColumns = @JoinColumn (name = "tag_id"))
    private Set<TagEntity> tags;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public TaskEntity() {
    }

    public TaskEntity(String title) {
        this.title = title;
        this.priority = Priority.NORMALE;
        this.status = TaskStatus.ACTIVE;
        this.tags = new HashSet<TagEntity>();
    }
}
