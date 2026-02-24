package com.hamster.system;

import com.hamster.model.GameConstants;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Centralized logging for DesktopHamster.
 * Logs to both file (~/.desktophamster/game.log) and console.
 */
public final class GameLogger {

    private static final Logger LOGGER = Logger.getLogger("DesktopHamster");
    private static boolean initialized = false;

    private GameLogger() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.ALL);

        // Console handler (warnings and above)
        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Level.WARNING);
        console.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(console);

        // File handler
        try {
            String logDir = GameConstants.SAVE_DIR;
            File dir = new File(logDir);
            if (!dir.exists()) dir.mkdirs();
            FileHandler fh = new FileHandler(logDir + "game.log", 500_000, 2, true);
            fh.setLevel(Level.ALL);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
        } catch (IOException e) {
            System.err.println("[GameLogger] Failed to create file handler: " + e.getMessage());
        }
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void warn(String msg) {
        LOGGER.warning(msg);
    }

    public static void error(String msg, Throwable t) {
        LOGGER.log(Level.SEVERE, msg, t);
    }

    public static void error(String msg) {
        LOGGER.severe(msg);
    }

    public static void debug(String msg) {
        LOGGER.fine(msg);
    }
}
