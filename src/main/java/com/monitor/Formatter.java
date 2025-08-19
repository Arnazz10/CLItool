package com.monitor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Formatter {
    private static final String HLINE = repeat('-', 80);

    public String renderScreen(
            double cpuAvg,
            double[] perCore,
            long totalMem,
            long usedMem,
            List<SystemMonitor.DiskUsage> disks,
            List<SystemMonitor.ProcessInfo> processes
    ) {
        StringBuilder sb = new StringBuilder();

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sb.append("System Resource Monitor  ").append(now).append('\n');
        sb.append(HLINE).append('\n');

        // CPU
        sb.append("CPU Usage: ").append(String.format("%5.1f%% avg", cpuAvg)).append("\n");
        if (perCore != null && perCore.length > 0) {
            for (int i = 0; i < perCore.length; i++) {
                String bar = bar(perCore[i], 30);
                sb.append(String.format("  Core %2d: %5.1f%% %s\n", i, perCore[i], bar));
            }
        }
        sb.append(HLINE).append('\n');

        // Memory
        sb.append("Memory: ")
                .append(humanBytes(usedMem)).append(" / ")
                .append(humanBytes(totalMem)).append("  (")
                .append(String.format("%4.1f%%", totalMem > 0 ? 100.0 * usedMem / totalMem : 0)).append(")\n");
        sb.append(HLINE).append('\n');

        // Disks
        sb.append("Disks:\n");
        sb.append(String.format("  %-22s %-20s %10s %12s\n", "Name", "Mount", "Used", "Total"));
        if (disks != null) {
            for (SystemMonitor.DiskUsage du : disks) {
                sb.append(String.format("  %-22.22s %-20.20s %10s %12s\n",
                        safe(du.name), safe(du.mount), humanBytes(du.usedBytes), humanBytes(du.totalBytes)));
            }
        }
        sb.append(HLINE).append('\n');

        // Processes
        sb.append("Top Processes (by CPU):\n");
        sb.append(String.format("  %-7s %-28s %8s %10s\n", "PID", "Name", "CPU%", "RSS"));
        if (processes != null) {
            for (SystemMonitor.ProcessInfo pi : processes) {
                sb.append(String.format("  %-7d %-28.28s %7.2f %10s\n",
                        pi.pid, safe(pi.name), pi.cpuLoadPct, humanBytes(pi.residentSetSize)));
            }
        }
        sb.append(HLINE).append('\n');
        sb.append("Press 'q' to quit. Refresh: 2s\n");

        return sb.toString();
    }

    private static String humanBytes(long bytes) {
        if (bytes < 0) return "n/a";
        final String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
        double b = bytes;
        int idx = 0;
        while (b >= 1024 && idx < units.length - 1) {
            b /= 1024.0;
            idx++;
        }
        return String.format(idx == 0 ? "%.0f %s" : "%.1f %s", b, units[idx]);
    }

    private static String bar(double pct, int width) {
        double p = Math.max(0, Math.min(100, pct));
        int filled = (int) Math.round(p / 100.0 * width);
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < width; i++) {
            sb.append(i < filled ? '#' : ' ');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String repeat(char c, int times) {
        StringBuilder sb = new StringBuilder(times);
        for (int i = 0; i < times; i++) sb.append(c);
        return sb.toString();
    }
}


