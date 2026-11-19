package com.fragmentsinstants.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest (properties = "spring.datasource.url=jdbc:sqlite:target/spike-test.db?foreign_keys=on")
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
public class MoodEntryRepositoryTest {
    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Test
    void testFindByDate() {
        MoodEntryEntity mood1 = new MoodEntryEntity(LocalDate.now(), 1, 7,5);
        MoodEntryEntity mood2 = new MoodEntryEntity(LocalDate.now().minusDays(2), 5, 6,4);

        moodEntryRepository.save(mood1);
        moodEntryRepository.save(mood2);

        Optional<MoodEntryEntity> result = moodEntryRepository.findByDate(LocalDate.now());

        assertThat(result).contains(mood1);
    }

    @Test
    void testFindByDateBetween() {
        MoodEntryEntity mood1 = new MoodEntryEntity(LocalDate.now(), 1, 7,5);
        MoodEntryEntity mood2 = new MoodEntryEntity(LocalDate.now().minusDays(2), 5, 6,4);

        moodEntryRepository.save(mood1);
        moodEntryRepository.save(mood2);

        Optional<MoodEntryEntity> result = moodEntryRepository.findByDateBetween(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertThat(result).contains(mood1);
    }
}