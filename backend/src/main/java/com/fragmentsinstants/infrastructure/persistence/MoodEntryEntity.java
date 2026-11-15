package com.fragmentsinstants.infrastructure.persistence;

import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table (name = "mood_entries")
public class MoodEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JdbcTypeCode(SqlTypes.INTEGER)
    private Long id;

    @Column (name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column (name= "mood_score", nullable = false)
    private Integer moodScore;

    @Column (name= "physical_fatigue_score", nullable = false)
    private Integer physicalFatigueScore;

    @Column (name= "mental_fatigue_score", nullable = false)
    private Integer mentalFatigueScore;

    @UpdateTimestamp
    @Column (name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(Integer moodScore) {
        this.moodScore = moodScore;
    }

    public Integer getPhysicalFatigueScore() {
        return physicalFatigueScore;
    }

    public void setPhysicalFatigueScore(Integer physicalFatigueScore) {
        this.physicalFatigueScore = physicalFatigueScore;
    }

    public Integer getMentalFatigueScore() {
        return mentalFatigueScore;
    }

    public void setMentalFatigueScore(Integer mentalFatigueScore) {
        this.mentalFatigueScore = mentalFatigueScore;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public MoodEntryEntity() {
    }

    public MoodEntryEntity(LocalDate date, Integer moodScore, Integer physicalFatigueScore, Integer mentalFatigueScore) {
        this.date = date;
        this.moodScore = moodScore;
        this.physicalFatigueScore = physicalFatigueScore;
        this.mentalFatigueScore = mentalFatigueScore;
    }   
    
}
