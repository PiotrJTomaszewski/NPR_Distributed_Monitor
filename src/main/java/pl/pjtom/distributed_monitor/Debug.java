package pl.pjtom.distributed_monitor;

import java.io.PrintStream;

public class Debug {
    private static final String ANSI_BLUE   = "\u001B[34m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RESET  = "\u001B[0m";
    private static DebugLevel level;

    public enum DebugLevel {
        NO_DEBUG, LEVEL_BASIC, LEVEL_MORE, LEVEL_HIGHEST
    }

    public enum Color {
        DEFAULT, GREEN, BLUE, RED, YELLOW
    }

    private static synchronized void printf_(PrintStream stream, Color color, String format, Object... args) {
        if (!format.endsWith("\n")) {
            format += "\n";
        }
        switch (color) {
            case GREEN:
                stream.printf(ANSI_GREEN + format + ANSI_RESET, args);
                break;
            case BLUE:
                stream.printf(ANSI_BLUE + format + ANSI_RESET, args);
                break;
            case RED:
                stream.printf(ANSI_RED + format + ANSI_RESET, args);
                break;
            case YELLOW:
                stream.printf(ANSI_YELLOW + format + ANSI_RESET, args);
                break;
            default:
                stream.printf(format, args);
                break;
        }
    }

    public static void init() {
        String debug_env = System.getenv("DMON_DEBUG");
        if (debug_env == null) {
            debug_env = "0";
        }
        switch (debug_env) {
            case "1":
                level = DebugLevel.LEVEL_BASIC;
                break;
            case "2":
                level = DebugLevel.LEVEL_MORE;
                break;
            case "3":
                level = DebugLevel.LEVEL_HIGHEST;
                break;
            default:
                level = DebugLevel.NO_DEBUG;
                break;
        }
    }

    public static void printf(DebugLevel min_debug_level, Color color, String format, Object... args) {
        if (level.ordinal() >= min_debug_level.ordinal())
            printf_(System.out, color, format, args);
    }

    public static void errorPrintf(DebugLevel min_debug_level, Color color, String format, Object... args) {
        if (level.ordinal() >= min_debug_level.ordinal())
            printf_(System.err, color, format, args);
    }

    public static void printf(DebugLevel min_debug_level, String format, Object... args) {
        if (level.ordinal() >= min_debug_level.ordinal())
            printf_(System.out, Color.DEFAULT, format, args);
    }

    public static void errorPrintf(DebugLevel min_debug_level, String format, Object... args) {
        if (level.ordinal() >= min_debug_level.ordinal())
            printf_(System.err, Color.DEFAULT, format, args);
    }
}
