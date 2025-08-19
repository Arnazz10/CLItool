package com.monitor;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SystemMonitor {
    public static class DiskUsage {
        public final String name;
        public final String mount;
        public final long totalBytes;
        public final long usedBytes;

        public DiskUsage(String name, String mount, long totalBytes, long usedBytes) {
            this.name = name;
            this.mount = mount;
            this.totalBytes = totalBytes;
            this.usedBytes = usedBytes;
        }
    }

    public static class ProcessInfo {
        public final int pid;
        public final String name;
        public final double cpuLoadPct;
        public final long residentSetSize;

        public ProcessInfo(int pid, String name, double cpuLoadPct, long residentSetSize) {
            this.pid = pid;
            this.name = name;
            this.cpuLoadPct = cpuLoadPct;
            this.residentSetSize = residentSetSize;
        }
    }

    private final SystemInfo systemInfo;
    private final CentralProcessor processor;
    private final GlobalMemory memory;
    private final OperatingSystem os;

    private long[] prevTicks;
    private long[][] prevProcTicks;
    private double cpuAverageLoad;
    private double[] perCoreCpuLoads;
    private long totalMemoryBytes;
    private long usedMemoryBytes;
    private List<DiskUsage> disksSnapshot;
    private List<ProcessInfo> topProcessesSnapshot;

    public SystemMonitor() {
        this.systemInfo = new SystemInfo();
        this.processor = systemInfo.getHardware().getProcessor();
        this.memory = systemInfo.getHardware().getMemory();
        this.os = systemInfo.getOperatingSystem();
        this.prevTicks = processor.getSystemCpuLoadTicks();
        this.perCoreCpuLoads = new double[processor.getLogicalProcessorCount()];
        this.prevProcTicks = processor.getProcessorCpuLoadTicks();
        this.disksSnapshot = new ArrayList<>();
        this.topProcessesSnapshot = new ArrayList<>();
        // Warm-up small sleep to allow meaningful deltas on first read
        Utils.sleepMillis(200);
    }

    public void refresh() {
        updateCpu();
        updateMemory();
        updateDisks();
        updateTopProcesses();
    }

    private void updateCpu() {
        long[] ticks = processor.getSystemCpuLoadTicks();
        this.cpuAverageLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100.0;
        this.prevTicks = ticks;

        double[] coreLoads = processor.getProcessorCpuLoadBetweenTicks(prevProcTicks);
        if (coreLoads != null) {
            this.perCoreCpuLoads = Arrays.stream(coreLoads)
                    .map(v -> v * 100.0)
                    .toArray();
        }
        // Update ticks for next interval
        this.prevProcTicks = processor.getProcessorCpuLoadTicks();
    }

    private void updateMemory() {
        this.totalMemoryBytes = memory.getTotal();
        long available = memory.getAvailable();
        this.usedMemoryBytes = totalMemoryBytes - available;
    }

    private void updateDisks() {
        List<DiskUsage> usages = new ArrayList<>();
        for (HWDiskStore store : systemInfo.getHardware().getDiskStores()) {
            store.updateAttributes();
            // OSHI cannot always map partitions to mounts; try via OS file system
            systemInfo.getOperatingSystem().getFileSystem().getFileStores().forEach(fs -> {
                try {
                    if (fs.getName() != null && store.getName() != null && fs.getName().contains(store.getName())) {
                        long total = fs.getTotalSpace();
                        long usable = fs.getUsableSpace();
                        long used = Math.max(0, total - usable);
                        usages.add(new DiskUsage(store.getModel() == null || store.getModel().isBlank() ? store.getName() : store.getModel(),
                                fs.getMount(), total, used));
                    }
                } catch (Exception ignored) {
                }
            });
        }
        // Fallback: if none matched, use file system list alone
        if (usages.isEmpty()) {
            systemInfo.getOperatingSystem().getFileSystem().getFileStores().forEach(fs -> {
                long total = fs.getTotalSpace();
                long usable = fs.getUsableSpace();
                long used = Math.max(0, total - usable);
                usages.add(new DiskUsage(fs.getName(), fs.getMount(), total, used));
            });
        }
        this.disksSnapshot = usages;
    }

    private void updateTopProcesses() {
        List<OSProcess> procs = os.getProcesses(
                p -> true,
                Comparator.comparingDouble(OSProcess::getProcessCpuLoadCumulative).reversed(),
                64
        );
        List<ProcessInfo> top = procs.stream()
                .map(p -> new ProcessInfo(p.getProcessID(), p.getName(), 100d * p.getProcessCpuLoadCumulative(), p.getResidentSetSize()))
                .limit(5)
                .collect(Collectors.toList());
        this.topProcessesSnapshot = top;
    }

    public double getCpuAverageLoad() { return cpuAverageLoad; }
    public double[] getPerCoreCpuLoads() { return perCoreCpuLoads; }
    public long getTotalMemoryBytes() { return totalMemoryBytes; }
    public long getUsedMemoryBytes() { return usedMemoryBytes; }
    public List<DiskUsage> getDisksSnapshot() { return disksSnapshot; }
    public List<ProcessInfo> getTopProcessesSnapshot() { return topProcessesSnapshot; }
}


