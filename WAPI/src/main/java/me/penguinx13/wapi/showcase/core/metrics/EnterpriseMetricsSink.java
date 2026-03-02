package me.penguinx13.wapi.showcase.core.metrics;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.result.CommandResult;
import me.penguinx13.wapi.commands.core.spi.MetricsSink;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.atomic.AtomicLong;

public final class EnterpriseMetricsSink implements MetricsSink {
    private final Plugin plugin;
    private final AtomicLong totalCommands = new AtomicLong();
    private final AtomicLong totalLatencyNanos = new AtomicLong();
    private final AtomicLong slowCommands = new AtomicLong();

    public EnterpriseMetricsSink(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onExecutionFinished(CommandContext context, CommandResult result, long nanos) {
        totalCommands.incrementAndGet();
        totalLatencyNanos.addAndGet(nanos);
        if (nanos > 100_000_000L) {
            slowCommands.incrementAndGet();
            plugin.getLogger().warning("Slow command " + context.rawInput() + " took " + (nanos / 1_000_000.0) + " ms");
        }
    }

    public long totalCommands() {
        return totalCommands.get();
    }

    public long slowCommands() {
        return slowCommands.get();
    }

    public double averageLatencyMillis() {
        long total = totalCommands.get();
        if (total == 0) return 0D;
        return (totalLatencyNanos.get() / 1_000_000D) / total;
    }
}
