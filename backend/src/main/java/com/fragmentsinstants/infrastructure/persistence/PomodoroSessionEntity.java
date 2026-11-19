package com.fragmentsinstants.infrastructure.persistence;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fragmentsinstants.domain.CompletionStatus;
import com.fragmentsinstants.domain.Mode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity @Table (name = "pomodoro_sessions")
public class PomodoroSessionEntity {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY) 
    @JdbcTypeCode (SqlTypes.INTEGER)
    private Long id;

    @Column (name = "mode", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Mode mode;

    @ManyToOne 
    @JoinColumn (name = "task_id")
    private TaskEntity task;

    @Column (name = "started_at", nullable = false)
    private Instant startedAt;

    @Column (name = "ended_at", nullable = false)
    private Instant endedAt;

    @Column (name = "actual_duration_seconds", nullable = false)
    private Integer actualDurationSeconds;

    @Column (name = "completion_status", nullable = false)
    @Enumerated (EnumType.STRING)
    private CompletionStatus completionStatus;

    @CreationTimestamp 
    @Column (name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public TaskEntity getTask() {
        return task;
    }

    public void setTask(TaskEntity task) {
        this.task = task;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public CompletionStatus getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(CompletionStatus completionStatus) {
        this.completionStatus = completionStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public PomodoroSessionEntity() {
    }

    public PomodoroSessionEntity(Mode mode, Instant startedAt, Instant endedAt, Integer actualDurationSeconds,
            CompletionStatus completionStatus) {
        this.mode = mode;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.actualDurationSeconds = actualDurationSeconds;
        this.completionStatus = completionStatus;
    }
    
}
