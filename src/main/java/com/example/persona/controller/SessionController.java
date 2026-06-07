package com.example.persona.controller;

import com.example.persona.dto.ApiResponse;
import com.example.persona.session.dto.SessionRequest;
import com.example.persona.session.dto.SessionResponse;
import com.example.persona.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Session", description = "Session management APIs for tracking user login sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/login")
    @Operation(
            summary = "Record login session",
            description =
                    "Records or updates user session on app login. Compares SHA value to detect changes in app version, OS version, or device. If changed, saves to DB and updates cache.")
    public ResponseEntity<ApiResponse<SessionResponse>> recordLogin(
            @Valid @RequestBody SessionRequest request, HttpServletRequest httpRequest) {

        String ipAddress = extractIpAddress(httpRequest);
        SessionResponse response = sessionService.recordLogin(request, ipAddress);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{customerKey}")
    @Operation(
            summary = "Get current session",
            description = "Retrieves the current session information for a customer")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(@PathVariable String customerKey) {

        return sessionService
                .getSession(customerKey)
                .map(session -> ResponseEntity.ok(ApiResponse.success(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/cache/{sessionSha}")
    @Operation(
            summary = "Invalidate session cache by SHA",
            description = "Invalidates the session cache using the SHA key")
    public ResponseEntity<ApiResponse<Void>> invalidateCache(@PathVariable String sessionSha) {

        sessionService.invalidateCache(sessionSha);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/sha/{sessionSha}")
    @Operation(
            summary = "Get session by SHA",
            description = "Retrieves the session information using the SHA key from cache")
    public ResponseEntity<ApiResponse<SessionResponse>> getSessionBySha(@PathVariable String sessionSha) {

        return sessionService
                .getSessionBySha(sessionSha)
                .map(session -> ResponseEntity.ok(ApiResponse.success(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Extracts the client IP address from the request. Handles proxied
     * requests by checking X-Forwarded-For header.
     */
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
