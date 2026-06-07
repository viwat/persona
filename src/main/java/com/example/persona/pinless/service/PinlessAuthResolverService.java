package com.example.persona.pinless.service;

import com.example.persona.pinless.model.dto.AuthResolveRequest;
import com.example.persona.pinless.model.dto.AuthResolveResponse;

public interface PinlessAuthResolverService {

    /**
     * Core entry point for mobile.
     * Resolves whether a transaction qualifies for pinless or must use PIN.
     * Records the outcome in pinless_txn_counter for reporting.
     */
    AuthResolveResponse resolve(AuthResolveRequest request);
}
