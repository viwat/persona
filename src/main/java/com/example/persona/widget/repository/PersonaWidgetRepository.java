package com.example.persona.widget.repository;

import com.example.persona.widget.model.PersonaWidget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaWidgetRepository extends JpaRepository<PersonaWidget, Long> {
    List<PersonaWidget> findByCustomerNo(String customerNo);

    Optional<PersonaWidget> findByCustomerNoAndId(String customerNo, Long sn);
}
