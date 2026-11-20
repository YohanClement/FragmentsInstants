package com.fragmentsinstants;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fragmentsinstants.infrastructure.persistence.SettingsEntity;
import com.fragmentsinstants.infrastructure.persistence.SettingsRepository;
import com.fragmentsinstants.infrastructure.persistence.TagEntity;
import com.fragmentsinstants.infrastructure.persistence.TagRepository;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/smoke-test.db?foreign_keys=on")
class FragmentsInstantsApplicationTests {
	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private SettingsRepository settingsRepository;

	@BeforeAll
	static void suppressionDBavantTest() throws IOException {
		Files.deleteIfExists((Path.of("target/smoke-test.db")));
	}

	@Test
	void contextLoads() {
	}

	@Test
	void testdefaultTagsAreSeeded() {
		assertThat(tagRepository.count()).isEqualTo(8);
		Optional<TagEntity> tag = tagRepository.findByNameIgnoreCase("autre");
		assertThat(tag).isPresent();
		assertThat(tag.get().isProtected()).isTrue();
	}

	@Test 
	void testDefaultSettings(){
		assertThat(settingsRepository.count()).isEqualTo(1);
		List<SettingsEntity> settings = settingsRepository.findAll();
		assertThat(settings.get(0).getFocusDurationMinutes()).isEqualTo(25);
		assertThat(settings.get(0).getShortBreakDurationMinutes()).isEqualTo(5);
		assertThat(settings.get(0).getLongBreakDurationMinutes()).isEqualTo(15);
		assertThat(settings.get(0).getPomodorosBeforeLongBreak()).isEqualTo(4);
		assertThat(settings.get(0).getPriorityCoefficientLow()).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(settings.get(0).getPriorityCoefficientNormal()).isEqualByComparingTo(new BigDecimal("1.25"));
        assertThat(settings.get(0).getPriorityCoefficientHigh()).isEqualByComparingTo(new BigDecimal("1.50"));
        assertThat(settings.get(0).getPriorityCoefficientCritical()).isEqualByComparingTo(new BigDecimal("2.00"));
		assertThat(settings.get(0).getSoundNotificationsEnabled()).isTrue();
	}

}
