package com.fragmentsinstants.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TagEntity, Long> {

    public Optional<TagEntity> findByNameIgnoreCase(String name);
    
}
