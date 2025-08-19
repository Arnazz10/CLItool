package com.monitor;

import java.io.IOException;
import java.io.PrintStream;

public class Utils {
    public static void clearScreen(PrintStream out) {
        // ANSI clear screen and move cursor to home
        out.print("\033[H\033[2J");
    }

    public static void hideCursor(PrintStream out) {
        out.print("\033[?25l");
    }

    public static void showCursor(PrintStream out) {
        out.print("\033[?25h");
    }

    public static void sleepMillis(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    // Raw mode helpers (Unix). On Windows this will silently no-op.
    private static boolean rawModeEnabled = false;

    public static boolean enableRawModeIfSupported() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return false;
        }
        try {
            // Disable canonical mode and echo: stty -icanon -echo min 1 time 0
            new ProcessBuilder("sh", "-c", "stty -g").inheritIO();
            String[] cmd = {"sh", "-c", "stty -icanon -echo min 1 time 0 < /dev/tty"};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            rawModeEnabled = (p.exitValue() == 0);
        } catch (Exception ignored) {
            rawModeEnabled = false;
        }
        return rawModeEnabled;
    }

    public static void disableRawModeIfSupported() {
        if (!rawModeEnabled) return;
        try {
            String[] cmd = {"sh", "-c", "stty sane < /dev/tty"};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
        } catch (Exception ignored) {
        }
        rawModeEnabled = false;
    }
}


