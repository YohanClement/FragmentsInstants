package com.fragmentsinstants.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<SettingsEntity, Long>{
    
}
