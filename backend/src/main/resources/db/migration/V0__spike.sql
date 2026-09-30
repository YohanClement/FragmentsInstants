--Spring Boot 4.1.1, Java 21, Hibernate 7.4.5 et SQLite fonctionnent ensemble.
--Flyway est aligné sur 12.4.0 (version de Boot), avec le module flyway-database-nc-sqlite dans la même version.
--hibernate-community-dialects doit avoir la même version que hibernate-core (7.4.5), sans numéro explicite dans le pom.
--Les ids INTEGER PRIMARY KEY AUTOINCREMENT exigent @JdbcTypeCode(SqlTypes.INTEGER) sur l'id de chaque entité, sinon la validation Hibernate refuse Long.

CREATE TABLE spike_item(
    my_id INTEGER PRIMARY KEY AUTOINCREMENT,
    my_text VARCHAR(100)
);