package com.example.persona.transaction.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthenticationMethod {
    PIN("PIN", 1),
    BIOMETRIC("Biometric", 2),
    FACE_PASS("FacePass", 3),
    MULTI_FACTOR("MultiFactor", 4);

    private final String name;
    private final int securityLevel;
}
