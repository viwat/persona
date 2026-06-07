package com.example.persona.theme.service;

import com.example.persona.theme.repository.UserPersonaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PersonaService {
    private final UserPersonaRepository personaRepository;

    public PersonaService(UserPersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }
}
