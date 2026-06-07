package com.example.persona.misc.repository;

import com.example.persona.misc.model.Enumeration;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnumerationRepository extends JpaRepository<Enumeration, Long> {
    List<Enumeration> findByCodeOrderByDisplayOrderAsc(String code);
}
