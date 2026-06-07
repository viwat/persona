package com.example.persona.config;

import com.example.persona.dto.UserContext;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

/**
 * Copies {@link UserContext}, selected {@link MDC} keys, and Micrometer tracing / observation
 * {@linkplain ThreadLocal thread locals} into {@code @Async} worker threads so logs and
 * {@link io.micrometer.tracing.Tracer} see the same trace as the HTTP request that submitted the task.
 */
@Component
public class ContextPropagatingTaskDecorator implements TaskDecorator {

    private static final ContextSnapshotFactory CONTEXT_SNAPSHOT_FACTORY =
            ContextSnapshotFactory.builder().build();

    @NotNull
    @Override
    public Runnable decorate(@NotNull Runnable runnable) {
        UserContext userContext = UserContextHolder.getCurrentContext();
        String requestId = MDC.get("requestId");
        String sessionId = MDC.get("sessionId");
        ContextSnapshot tracingAndObservationContext = CONTEXT_SNAPSHOT_FACTORY.captureAll();

        return () -> {
            try (ContextSnapshot.Scope ignored = tracingAndObservationContext.setThreadLocals()) {
                try {
                    if (userContext != null) {
                        UserContextHolder.setUserContext(userContext);
                    }
                    if (requestId != null) {
                        MDC.put("requestId", requestId);
                    }
                    if (sessionId != null) {
                        MDC.put("sessionId", sessionId);
                    }
                    runnable.run();
                } finally {
                    UserContextHolder.clearUserContext();
                    MDC.remove("requestId");
                    MDC.remove("sessionId");
                }
            }
        };
    }
}
