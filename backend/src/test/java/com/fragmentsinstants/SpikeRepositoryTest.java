package com.fragmentsinstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest(properties = "spring.datasource.url=jdbc:sqlite:target/spike-test.db?foreign_keys=on")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SpikeRepositoryTest {
    @Autowired
    private SpikeItemRepository SIR;

    @Autowired
    JdbcTemplate template;

    @Test
    public void spike() {
        SpikeItem item = new SpikeItem("test un");
        assertThat(item.getMyId()).isNull();

        SpikeItem premierSauvegarde = SIR.save(item);
        assertThat(premierSauvegarde.getMyId()).isNotNull();

        SpikeItem item2 = SIR.save(new SpikeItem("test deux"));
        assertThat(item2.getMyId()).isNotNull();
    }

    @Test
    public void laBaseRefuseUnLienVersUneTacheInexistante() {
        assertThatThrownBy(() -> template.update("INSERT INTO task_tag (task_id, tag_id) VALUES (999, 999)"))
                .hasMessageContaining("FOREIGN KEY");
    }

    @Test
    public void supprimerUneTacheVideLeLienDesSessions() {
        template.update(
                "INSERT INTO tasks(title, created_at, updated_at) VALUES ('mytag', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        Long taskId = template.queryForObject("SELECT id FROM tasks WHERE title = ?", Long.class, "mytag");
        template.update(
                "INSERT INTO pomodoro_sessions(mode, task_id, started_at, ended_at, actual_duration_seconds, completion_status, created_at) "
                        +
                        "VALUES ('FOCUS', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 'TERMINEE', CURRENT_TIMESTAMP)",
                taskId);
        template.update("DELETE FROM tasks WHERE tasks.id = ?", taskId);

        Integer nombreDeSessions = template.queryForObject(
                "SELECT COUNT(*) FROM pomodoro_sessions", Integer.class);
        Long tacheRestante = template.queryForObject(
                "SELECT task_id FROM pomodoro_sessions", Long.class);

        assertThat(nombreDeSessions).isEqualTo(1);
        assertThat(tacheRestante).isNull();
    }

    @Test
    public void laBaseRefuseUnScoreHorsEchelle() {
        assertThatThrownBy(() -> template.update(
                "INSERT INTO mood_entries(date, mood_score, physical_fatigue_score, mental_fatigue_score, updated_at) VALUES (CURRENT_DATE, 8, 8, 8, CURRENT_TIMESTAMP)"))
                .hasMessageContaining("CHECK");
    }

    @Test
    public void laBaseRefuseUnDoublonDeDate() {
        template.update(
                "INSERT INTO mood_entries(date, mood_score, physical_fatigue_score, mental_fatigue_score, updated_at) VALUES (CURRENT_DATE, 1, 2, 3, CURRENT_TIMESTAMP)");
        assertThatThrownBy(() -> template.update(
                "INSERT INTO mood_entries(date, mood_score, physical_fatigue_score, mental_fatigue_score, updated_at) VALUES (CURRENT_DATE, 7, 7, 7, CURRENT_TIMESTAMP)"))
                .hasMessageContaining("UNIQUE");
    }

    private void insererReglages(int id, int pauseCourte) {
        template.update("DELETE FROM settings");
        template.update(
                "INSERT INTO settings(id, focus_duration_minutes, short_break_duration_minutes, "
                        + "long_break_duration_minutes, pomodoros_before_long_break, priority_coefficient_low, "
                        + "priority_coefficient_normal, priority_coefficient_high, priority_coefficient_critical, "
                        + "sound_notifications_enabled) VALUES (?, 25, ?, 15, 4, 1, 1.25, 1.5, 2, 1)",
                id, pauseCourte);
    }

    @Test
    public void desReglagesValidesSontAcceptes() {
        insererReglages(1, 5);
        Integer nombre = template.queryForObject("SELECT COUNT(*) FROM settings", Integer.class);
        assertThat(nombre).isEqualTo(1);
    }

    @Test
    public void laBaseRefuseUnePauseCourteNulle() {
        assertThatThrownBy(() -> insererReglages(1, 0))
                .hasMessageContaining("CHECK");
    }

    @Test
    public void laBaseRefuseUneDeuxiemeLigneDeReglages() {
        assertThatThrownBy(() -> insererReglages(2, 5))
                .hasMessageContaining("CHECK");
    }
}