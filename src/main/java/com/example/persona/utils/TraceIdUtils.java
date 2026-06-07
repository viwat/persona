package com.example.persona.utils;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import java.util.Optional;

public class TraceIdUtils {

    private TraceIdUtils() {}

    public static String getTraceId(Tracer tracer) {
        return Optional.ofNullable(tracer)
                .map(Tracer::currentSpan)
                .map(Span::context)
                .map(TraceContext::traceId)
                .orElse(UniqueIdGeneratorUtil.getUniqueKey());
    }

    public static String getSpanId(Tracer tracer) {
        return Optional.ofNullable(tracer)
                .map(Tracer::currentSpan)
                .map(Span::context)
                .map(TraceContext::spanId)
                .orElse(UniqueIdGeneratorUtil.getUniqueKey());
    }
}
