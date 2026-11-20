package com.fragmentsinstants.infrastructure.persistence;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table (name = "settings")
public class SettingsEntity {
    @Id
    @JdbcTypeCode (SqlTypes.INTEGER)
    private Long id = 1L;

    @Column (name = "focus_duration_minutes", nullable = false)
    private Integer focusDurationMinutes;

    @Column (name = "short_break_duration_minutes", nullable = false)
    private Integer shortBreakDurationMinutes;

    @Column (name = "long_break_duration_minutes", nullable = false)
    private Integer longBreakDurationMinutes;

    @Column (name = "pomodoros_before_long_break", nullable = false)
    private Integer pomodorosBeforeLongBreak;

    @Column (name = "priority_coefficient_low", nullable = false, precision = 4, scale = 2)
    private BigDecimal priorityCoefficientLow;

    @Column (name = "priority_coefficient_normal", nullable = false, precision = 4, scale = 2)
    private BigDecimal priorityCoefficientNormal;

    @Column (name = "priority_coefficient_high", nullable = false, precision = 4, scale = 2)
    private BigDecimal priorityCoefficientHigh;

    @Column (name = "priority_coefficient_critical", nullable = false, precision = 4, scale = 2 )
    private BigDecimal priorityCoefficientCritical;

    @Column (name = "sound_notifications_enabled", nullable = false)
    private Boolean soundNotificationsEnabled;

    public Long getId() {
        return id;
    }

    public Integer getFocusDurationMinutes() {
        return focusDurationMinutes;
    }

    public void setFocusDurationMinutes(Integer focusDurationMinutes) {
        this.focusDurationMinutes = focusDurationMinutes;
    }

    public Integer getShortBreakDurationMinutes() {
        return shortBreakDurationMinutes;
    }

    public void setShortBreakDurationMinutes(Integer shortBreakDurationMinutes) {
        this.shortBreakDurationMinutes = shortBreakDurationMinutes;
    }

    public Integer getLongBreakDurationMinutes() {
        return longBreakDurationMinutes;
    }

    public void setLongBreakDurationMinutes(Integer longBreakDurationMinutes) {
        this.longBreakDurationMinutes = longBreakDurationMinutes;
    }

    public Integer getPomodorosBeforeLongBreak() {
        return pomodorosBeforeLongBreak;
    }

    public void setPomodorosBeforeLongBreak(Integer pomodorosBeforeLongBreak) {
        this.pomodorosBeforeLongBreak = pomodorosBeforeLongBreak;
    }

    public BigDecimal getPriorityCoefficientLow() {
        return priorityCoefficientLow;
    }

    public void setPriorityCoefficientLow(BigDecimal priorityCoefficientLow) {
        this.priorityCoefficientLow = priorityCoefficientLow;
    }

    public BigDecimal getPriorityCoefficientNormal() {
        return priorityCoefficientNormal;
    }

    public void setPriorityCoefficientNormal(BigDecimal priorityCoefficientNormal) {
        this.priorityCoefficientNormal = priorityCoefficientNormal;
    }

    public BigDecimal getPriorityCoefficientHigh() {
        return priorityCoefficientHigh;
    }

    public void setPriorityCoefficientHigh(BigDecimal priorityCoefficientHigh) {
        this.priorityCoefficientHigh = priorityCoefficientHigh;
    }

    public BigDecimal getPriorityCoefficientCritical() {
        return priorityCoefficientCritical;
    }

    public void setPriorityCoefficientCritical(BigDecimal priorityCoefficientCritical) {
        this.priorityCoefficientCritical = priorityCoefficientCritical;
    }

    public Boolean getSoundNotificationsEnabled() {
        return soundNotificationsEnabled;
    }

    public void setSoundNotificationsEnabled(Boolean soundNotificationsEnabled) {
        this.soundNotificationsEnabled = soundNotificationsEnabled;
    }

    protected SettingsEntity() {}        
}