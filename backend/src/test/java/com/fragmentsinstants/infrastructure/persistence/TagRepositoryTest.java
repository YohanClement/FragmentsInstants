package com.fragmentsinstants.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest (properties = "spring.datasource.url=jdbc:sqlite:target/spike-test.db?foreign_keys=on")
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
public class TagRepositoryTest {
    @Autowired 
    private TagRepository tagRepository;

 // Cas 1 : ton test d'origine, avec AssertJ à la place de assert
    @Test
    void  tagSavedCaseIgnored() {
        // Préparer : on insère un tag qui n'existe pas dans les données de V2
        TagEntity tag = new TagEntity("TestTag");
        tagRepository.save(tag);

        // Agir : on le cherche en minuscules
        Optional<TagEntity> Found = tagRepository.findByNameIgnoreCase("testtag");

        // Vérifier : il est présent, et son nom est celui d'origine (pas "testtag")
        assertThat(Found).isPresent();
        assertThat(Found.get().getName()).isEqualTo("TestTag");
    }

    // Cas 2 : un tag inséré par la migration V2 (rien à préparer)
    @Test
    void findDefaultTagInLowerCase() {
        Optional<TagEntity> Found = tagRepository.findByNameIgnoreCase("maison");

        assertThat(Found).isPresent();
        assertThat(Found.get().getName()).isEqualTo("Maison");
    }

    // Cas 3 : le plus important pour le futur service.
    // "Vide" veut dire "ce nom est libre, on peut créer le tag".
    @Test
    void nonexitingNameGivesEmptyResult() {
        Optional<TagEntity> Found = tagRepository.findByNameIgnoreCase("Jardinage");

        assertThat(Found).isEmpty();
    }

    // Cas 4 : documente la limite de SQLite.
    // upper() de SQLite ne convertit que A-Z, donc "Écriture" n'est pas retrouvé en "ÉCRITURE".
    // Ce test décrit le comportement RÉEL, pas le comportement idéal :
    // s'il échoue un jour, c'est que la limite a disparu.
    @Test
    void accentSensitiveCase() {
        Optional<TagEntity> Found = tagRepository.findByNameIgnoreCase("écriture");

        assertThat(Found).isEmpty();
    }
}
