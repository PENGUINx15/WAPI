package me.penguinx13.wapi.monitoring;

public class SystemMetricsProvider {

    private static final long MB_DIVISOR = 1024L * 1024L;

    public long getUsedMemoryMb() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / MB_DIVISOR;
    }

    public long getMaxMemoryMb() {
        return Runtime.getRuntime().maxMemory() / MB_DIVISOR;
    }
}
