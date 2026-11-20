package com.fragmentsinstants.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest (properties = "spring.datasource.url=jdbc:sqlite:target/spike-test.db?foreign_keys=on")
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
public class SettingsRepositoryTest {
    @Autowired
    private SettingsRepository settingsRepository;
    
    @Test
    void testOnlyOneSettingEntry(){
        Optional<SettingsEntity> existingSetting = settingsRepository.findById(1L);
        Optional<SettingsEntity> emptySetting = settingsRepository.findById(2L);

        assertThat(existingSetting).isPresent();
        assertThat(emptySetting).isEmpty();
    }

    @Test
    void testCheckDefaultValues(){
        Optional<SettingsEntity> existingSetting = settingsRepository.findById(1L);

        SettingsEntity defaultSettings = existingSetting.orElseThrow();

        assertThat(defaultSettings.getFocusDurationMinutes()).isEqualTo(25);
        assertThat(defaultSettings.getShortBreakDurationMinutes()).isEqualTo(5);
        assertThat(defaultSettings.getLongBreakDurationMinutes()).isEqualTo(15);
        assertThat(defaultSettings.getPomodorosBeforeLongBreak()).isEqualTo(4);
        assertThat(defaultSettings.getPriorityCoefficientLow()).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(defaultSettings.getPriorityCoefficientNormal()).isEqualByComparingTo(new BigDecimal("1.25"));
        assertThat(defaultSettings.getPriorityCoefficientHigh()).isEqualByComparingTo(new BigDecimal("1.50"));
        assertThat(defaultSettings.getPriorityCoefficientCritical()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(defaultSettings.getSoundNotificationsEnabled()).isTrue();
    }
}