package com.hamster.model;

/**
 * Central location for all game balance constants and magic numbers.
 */
public final class GameConstants {

    private GameConstants() {}

    // === Timing (frames) ===
    /** Frames per in-game day (~5 minutes real time at 30fps) */
    public static final int FRAMES_PER_DAY = 9000;
    /** Game loop tick interval in milliseconds (~30fps) */
    public static final int GAME_TICK_MS = 33;
    /** Auto-save interval in frames (~1 minute) */
    public static final int AUTO_SAVE_INTERVAL = 1800;
    /** Passive income interval in frames (~30 seconds) */
    public static final int PASSIVE_INCOME_INTERVAL = 900;
    /** Achievement check interval in frames (~30 seconds) */
    public static final int ACHIEVEMENT_CHECK_INTERVAL = 900;
    /** Hamster interaction check interval in frames (~90 seconds) */
    public static final int INTERACTION_CHECK_INTERVAL = 2700;
    /** User action animation duration in frames (~3 seconds) */
    public static final int USER_ACTION_ANIM_FRAMES = 90;

    // === Hamster stats ===
    /** Minimum poop timer before poop can occur */
    public static final int POOP_MIN_TIMER = 200;
    /** Poop penalty check interval in frames */
    public static final int POOP_PENALTY_INTERVAL = 200;
    /** Breed cooldown in days */
    public static final int BREED_COOLDOWN_DAYS = 2;
    /** Interaction cooldown in frames */
    public static final int INTERACTION_COOLDOWN = 2700;
    /** Maximum stat value cap */
    public static final int MAX_STAT_CAP = 200;
    /** AI sleep recovery interval in frames */
    public static final int AI_SLEEP_INTERVAL = 200;
    /** AI energy drain interval in frames (when not sleeping) */
    public static final int AI_ENERGY_DRAIN_INTERVAL = 400;
    /** AI running wheel stat interval in frames */
    public static final int AI_WHEEL_INTERVAL = 200;
    /** Base stat drain interval in frames (~10 seconds) */
    public static final int BASE_DRAIN_INTERVAL = 300;

    // === Economy ===
    /** Coins earned from clicking a poop */
    public static final int POOP_CLICK_REWARD = 5;
    /** Coins earned per poop when using clean-all */
    public static final int POOP_CLEAN_ALL_REWARD = 3;
    /** Base cost for purchasing a hamster (multiplied by purchase count + 1) */
    public static final int HAMSTER_PURCHASE_BASE_COST = 100;
    /** Starting food seeds for new game */
    public static final int STARTING_FOOD_SEEDS = 5;
    /** Passive income per living hamster per interval */
    public static final int PASSIVE_INCOME_AMOUNT = 1;
    /** Seeds calculation: per hamster raised */
    public static final int SEEDS_PER_HAMSTER = 10;
    /** Seeds calculation: coins divisor */
    public static final int SEEDS_COINS_DIVISOR = 5;

    // === Window sizes ===
    public static final int HAMSTER_WINDOW_WIDTH = 80;
    public static final int HAMSTER_WINDOW_HEIGHT = 100;
    public static final int WHEEL_WINDOW_WIDTH = 150;
    public static final int WHEEL_WINDOW_HEIGHT = 140;

    // === Save format ===
    public static final int SAVE_VERSION = 2;
    public static final String SAVE_DIR = System.getProperty("user.home") + "/.desktophamster/";
}
