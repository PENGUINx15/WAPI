package me.penguinx13.wapi.commands.core.context;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ExecutionState {
    private final UUID correlationId = UUID.randomUUID();
    private final Instant startedAt = Instant.now();
    private final Map<String, List<String>> suggestionCache = new ConcurrentHashMap<>();
    private final Map<String, Object> middlewareStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> stageNanos = new ConcurrentHashMap<>();
    private volatile boolean cancelled;

    public UUID correlationId() { return correlationId; }
    public Instant startedAt() { return startedAt; }
    public Map<String, List<String>> suggestionCache() { return suggestionCache; }
    public Map<String, Object> middlewareStorage() { return middlewareStorage; }
    public Map<String, Long> stageNanos() { return stageNanos; }
    public boolean cancelled() { return cancelled; }
    public void cancel() { this.cancelled = true; }

    public void recordStageTiming(String stageName, long nanos) {
        stageNanos.put(stageName, nanos);
    }

    public Duration elapsed() {
        return Duration.between(startedAt, Instant.now());
    }
}
