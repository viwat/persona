package com.example.persona.theme.repository;

import com.example.persona.theme.model.AccentColor;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccentColorRepository extends JpaRepository<@NonNull AccentColor, @NonNull Long> {

    @Query("SELECT a FROM AccentColor a WHERE a.status = 'ACTIVE'")
    List<AccentColor> findActiveColors();
}
