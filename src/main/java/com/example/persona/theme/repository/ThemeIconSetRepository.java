package com.example.persona.theme.repository;

import com.example.persona.theme.model.ThemeIconSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeIconSetRepository extends JpaRepository<ThemeIconSet, Long> {}
