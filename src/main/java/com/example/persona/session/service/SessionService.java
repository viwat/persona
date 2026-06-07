package com.example.persona.session.service;

import com.example.persona.session.cache.UserSessionCache;
import com.example.persona.session.dto.SessionRequest;
import com.example.persona.session.dto.SessionResponse;
import com.example.persona.session.model.UserSession;
import com.example.persona.session.repository.UserSessionRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Session service implementing cache-aside pattern. Tracks user sessions with
 * app version, OS version, last login time, and IP address. Uses SHA key for
 * cache lookups to detect session changes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final UserSessionRepository userSessionRepository;
    private final UserSessionCache userSessionCache;

    /**
     * Records or updates a user session on login. Implements cache-aside pattern
     * with SHA key: 1. Generate SHA fromEntity session attributes 2. Try to read
     * fromEntity cache using SHA key 3. If cache miss, read fromEntity DB by
     * customerKey 4. Compare SHA values to detect changes 5. If changed, update DB
     * and update cache with new SHA key
     *
     * @param request
     *            the session request with app details
     * @param ipAddress
     *            the client IP address
     * @return SessionResponse with session details and change indicator
     */
    @Transactional
    public SessionResponse recordLogin(SessionRequest request, String ipAddress) {
        String customerKey = request.getCustomerKey();
        log.info("Recording login for customer: {}", customerKey);

        // Build new session with request data
        UserSession newSession = buildSessionFromRequest(request, ipAddress);
        String newSha = newSession.generateSessionSha();

        // Cache-Aside: Try to read fromEntity cache using SHA key
        Optional<UserSession> cachedSession = userSessionCache.getBySha(newSha);

        UserSession existingSession;
        boolean sessionChanged;
        String oldSha = null;

        if (cachedSession.isPresent()) {
            log.debug("Cache hit for SHA: {}", newSha);
            existingSession = cachedSession.get();
            // Same SHA means no change
            sessionChanged = false;
        } else {
            log.debug("Cache miss for SHA: {}, checking database", newSha);
            // Cache miss - check database by customerKey
            Optional<UserSession> dbSession = userSessionRepository.findByCustomerKey(customerKey);
            if (dbSession.isPresent()) {
                existingSession = dbSession.get();
                oldSha = existingSession.getSessionSha();
                sessionChanged = hasSessionChanged(oldSha, newSha);
            } else {
                // New session
                existingSession = null;
                sessionChanged = true;
            }
        }

        UserSession savedSession;
        if (sessionChanged) {
            // Session changed or new session - update DB and cache
            log.info(
                    "Session changed for customer: {}, updating database. Old SHA: {}, New SHA: {}",
                    customerKey,
                    oldSha,
                    newSha);

            // Evict old SHA fromEntity cache if exists
            if (oldSha != null) {
                userSessionCache.evict(oldSha);
            }

            savedSession = updateSession(existingSession, newSession, newSha);

            // Update cache with new SHA key
            userSessionCache.put(newSha, savedSession);
        } else {
            // Session unchanged - just update last login time
            log.debug("Session unchanged for customer: {}, updating login time only", customerKey);
            existingSession.setLastLoginTime(LocalDateTime.now());
            existingSession.setIpAddress(ipAddress);
            savedSession = userSessionRepository.save(existingSession);

            // Update cache with same SHA key
            userSessionCache.put(newSha, savedSession);
        }

        return SessionResponse.fromEntity(savedSession, sessionChanged);
    }

    /**
     * Get current session for a customer by SHA key. Implements cache-aside read
     * pattern.
     *
     * @param sessionSha
     *            the session SHA key
     * @return Optional containing the session if found
     */
    public Optional<SessionResponse> getSessionBySha(String sessionSha) {
        // Try cache first using SHA key
        Optional<UserSession> cachedSession = userSessionCache.getBySha(sessionSha);
        return cachedSession.map(userSession -> SessionResponse.fromEntity(userSession, false));
    }

    /**
     * Get current session for a customer by customerKey. Checks database and
     * populates cache.
     *
     * @param customerKey
     *            the customer key
     * @return Optional containing the session if found
     */
    public Optional<SessionResponse> getSession(String customerKey) {
        // Check database by customerKey
        Optional<UserSession> dbSession = userSessionRepository.findByCustomerKey(customerKey);
        if (dbSession.isPresent()) {
            UserSession session = dbSession.get();
            // Populate cache for next read using SHA key
            if (session.getSessionSha() != null) {
                userSessionCache.put(session.getSessionSha(), session);
            }
            return Optional.of(SessionResponse.fromEntity(session, false));
        }

        return Optional.empty();
    }

    /**
     * Invalidate session cache by SHA key.
     *
     * @param sessionSha
     *            the session SHA key
     */
    public void invalidateCache(String sessionSha) {
        userSessionCache.evict(sessionSha);
        log.info("Cache invalidated for SHA: {}", sessionSha);
    }

    private UserSession buildSessionFromRequest(SessionRequest request, String ipAddress) {
        return UserSession.builder()
                .customerKey(request.getCustomerKey())
                .customerAppId(request.getCustomerAppId())
                .deviceId(request.getDeviceId())
                .appVersion(request.getAppVersion())
                .osVersion(request.getOsVersion())
                .osType(request.getOsType())
                .deviceModel(request.getDeviceModel())
                .deviceBrand(request.getDeviceBrand())
                .deviceManufacturer(request.getDeviceManufacturer())
                .ipAddress(ipAddress)
                .lastLoginTime(LocalDateTime.now())
                .sourceEvent(request.getSourceEvent())
                .metadata(request.getMetadata())
                .status(com.example.persona.enums.StatusType.ACTIVE)
                .build();
    }

    private boolean hasSessionChanged(String existingSha, String newSha) {
        if (existingSha == null || newSha == null) {
            return true;
        }
        return !existingSha.equals(newSha);
    }

    private UserSession updateSession(UserSession existing, UserSession newSession, String newSha) {
        if (existing == null) {
            // Create new session
            newSession.setSessionSha(newSha);
            return userSessionRepository.save(newSession);
        }

        // Update existing session
        existing.setCustomerAppId(newSession.getCustomerAppId());
        existing.setAppVersion(newSession.getAppVersion());
        existing.setOsVersion(newSession.getOsVersion());
        existing.setOsType(newSession.getOsType());
        existing.setDeviceId(newSession.getDeviceId());
        existing.setDeviceModel(newSession.getDeviceModel());
        existing.setDeviceBrand(newSession.getDeviceBrand());
        existing.setDeviceManufacturer(newSession.getDeviceManufacturer());
        existing.setIpAddress(newSession.getIpAddress());
        existing.setLastLoginTime(newSession.getLastLoginTime());
        existing.setSourceEvent(newSession.getSourceEvent());
        existing.setMetadata(newSession.getMetadata());
        existing.setSessionSha(newSha);

        return userSessionRepository.save(existing);
    }
}
