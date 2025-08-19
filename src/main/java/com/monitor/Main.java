package com.monitor;

import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        PrintStream out = System.out;
        AtomicBoolean quitRequested = new AtomicBoolean(false);

        // Try to enable raw mode on Unix-like for single-key quit
        boolean rawModeEnabled = Utils.enableRawModeIfSupported();

        // Ensure terminal state is restored
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Utils.showCursor(out);
            if (rawModeEnabled) {
                Utils.disableRawModeIfSupported();
            }
            AnsiConsole.systemUninstall();
        }));

        // Input watcher thread to detect 'q' to quit
        Thread inputThread = new Thread(() -> waitForQuit(quitRequested));
        inputThread.setDaemon(true);
        inputThread.start();

        SystemMonitor monitor = new SystemMonitor();
        Formatter formatter = new Formatter();

        Utils.hideCursor(out);
        try {
            while (!quitRequested.get()) {
                try {
                    monitor.refresh();

                    String screen = formatter.renderScreen(
                            monitor.getCpuAverageLoad(),
                            monitor.getPerCoreCpuLoads(),
                            monitor.getTotalMemoryBytes(),
                            monitor.getUsedMemoryBytes(),
                            monitor.getDisksSnapshot(),
                            monitor.getTopProcessesSnapshot()
                    );

                    Utils.clearScreen(out);
                    out.print(screen);
                    out.flush();
                } catch (Exception e) {
                    Utils.clearScreen(out);
                    out.println("Error while updating monitor: " + e.getMessage());
                }

                Utils.sleepMillis(2000);
            }
        } finally {
            Utils.showCursor(out);
            if (rawModeEnabled) {
                Utils.disableRawModeIfSupported();
            }
            AnsiConsole.systemUninstall();
        }
    }

    private static void waitForQuit(AtomicBoolean quitRequested) {
        InputStream in = System.in;
        try {
            while (!quitRequested.get()) {
                int ch = in.read();
                if (ch == -1) {
                    // End of input; nothing to do
                    Utils.sleepMillis(100);
                    continue;
                }
                if (ch == 'q' || ch == 'Q') {
                    quitRequested.set(true);
                    break;
                }
            }
        } catch (IOException ignored) {
            // If input fails, allow loop to continue and user can Ctrl+C
        }
    }
}


