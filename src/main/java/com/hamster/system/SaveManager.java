package com.hamster.system;
import com.hamster.model.Buff;
import com.hamster.model.FoodInventory;
import com.hamster.model.GameConstants;
import com.hamster.model.GameState;
import com.hamster.model.HamsterColor;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class SaveManager {

    private static final String SAVE_DIR = GameConstants.SAVE_DIR;
    private static final String AUTOSAVE_FILE = SAVE_DIR + "save_autosave.properties";

    public static void saveAuto(GameState state) {
        saveToFile(state, AUTOSAVE_FILE, "AutoSave");
    }

    public static GameState loadAuto() {
        return loadFromFile(AUTOSAVE_FILE);
    }

    public static boolean autoSaveExists() {
        return new File(AUTOSAVE_FILE).exists();
    }

    public static void deleteAutoSave() {
        File file = new File(AUTOSAVE_FILE);
        if (file.exists()) file.delete();
    }

    private static void saveToFile(GameState state, String path, String label) {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Create backup of existing save
        File targetFile = new File(path);
        File backupFile = new File(path + ".bak");
        if (targetFile.exists()) {
            try {
                copyFile(targetFile, backupFile);
            } catch (IOException e) {
                GameLogger.warn("Failed to create backup: " + e.getMessage());
            }
        }

        Properties props = new Properties();
        props.setProperty("saveVersion", String.valueOf(GameConstants.SAVE_VERSION));
        props.setProperty("money", String.valueOf(state.money));
        props.setProperty("totalFrames", String.valueOf(state.totalFrames));
        props.setProperty("hamstersRaised", String.valueOf(state.hamstersRaised));
        props.setProperty("qualifiedHamsters", String.valueOf(state.qualifiedHamsters));
        props.setProperty("hamsterPurchaseCount", String.valueOf(state.hamsterPurchaseCount));

        props.setProperty("hamsterCount", String.valueOf(state.hamsters.size()));
        for (int i = 0; i < state.hamsters.size(); i++) {
            GameState.HamsterData hd = state.hamsters.get(i);
            String prefix = "hamster." + i + ".";
            props.setProperty(prefix + "name", hd.name);
            props.setProperty(prefix + "color", hd.color.name());
            props.setProperty(prefix + "hunger", String.valueOf(hd.hunger));
            props.setProperty(prefix + "happiness", String.valueOf(hd.happiness));
            props.setProperty(prefix + "energy", String.valueOf(hd.energy));
            props.setProperty(prefix + "poopTimer", String.valueOf(hd.poopTimer));
            props.setProperty(prefix + "ageFrames", String.valueOf(hd.ageFrames));
            props.setProperty(prefix + "lifespanFrames", String.valueOf(hd.lifespanFrames));
            props.setProperty(prefix + "windowX", String.valueOf(hd.windowX));
            props.setProperty(prefix + "windowY", String.valueOf(hd.windowY));
            // Roguelike fields
            props.setProperty(prefix + "generation", String.valueOf(hd.generation));
            props.setProperty(prefix + "legacyHungerBonus", String.valueOf(hd.legacyHungerBonus));
            props.setProperty(prefix + "legacyHappinessBonus", String.valueOf(hd.legacyHappinessBonus));
            props.setProperty(prefix + "legacyEnergyBonus", String.valueOf(hd.legacyEnergyBonus));
            props.setProperty(prefix + "legacyLifespanBonus", String.valueOf(hd.legacyLifespanBonus));
            props.setProperty(prefix + "legacyMaxStatBonus", String.valueOf(hd.legacyMaxStatBonus));
            props.setProperty(prefix + "maxHunger", String.valueOf(hd.maxHunger));
            props.setProperty(prefix + "maxHappiness", String.valueOf(hd.maxHappiness));
            props.setProperty(prefix + "maxEnergy", String.valueOf(hd.maxEnergy));
            props.setProperty(prefix + "breedCooldownFrames", String.valueOf(hd.breedCooldownFrames));
            props.setProperty(prefix + "buffCount", String.valueOf(hd.buffs.size()));
            for (int j = 0; j < hd.buffs.size(); j++) {
                GameState.BuffData bd = hd.buffs.get(j);
                String bp = prefix + "buff." + j + ".";
                props.setProperty(bp + "type", bd.type.name());
                props.setProperty(bp + "multiplier", String.valueOf(bd.multiplier));
                props.setProperty(bp + "remainingFrames", String.valueOf(bd.remainingFrames));
                props.setProperty(bp + "description", bd.description);
            }
            // 2.0 fields
            props.setProperty(prefix + "personality", hd.personality != null ? hd.personality : "CHEERFUL");
            props.setProperty(prefix + "equippedAccessories", joinList(hd.equippedAccessories));
            props.setProperty(prefix + "ownedAccessories", joinList(hd.ownedAccessories));
        }

        props.setProperty("poopCount", String.valueOf(state.poops.size()));
        for (int i = 0; i < state.poops.size(); i++) {
            GameState.PoopData pd = state.poops.get(i);
            String prefix = "poop." + i + ".";
            props.setProperty(prefix + "screenX", String.valueOf(pd.screenX));
            props.setProperty(prefix + "screenY", String.valueOf(pd.screenY));
        }

        // Food inventory
        if (state.foodInventory != null) {
            state.foodInventory.saveToProperties(props, "inv.");
        }

        File tempFile = new File(path + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            props.store(fos, "DesktopHamster Save - " + label);
        } catch (IOException e) {
            GameLogger.error("Failed to save game: " + label, e);
            return;
        }

        if (targetFile.exists()) {
            targetFile.delete();
        }
        if (!tempFile.renameTo(targetFile)) {
            GameLogger.error("Failed to rename temp save file to: " + path);
            // Try to restore from backup
            if (backupFile.exists()) {
                try {
                    copyFile(backupFile, targetFile);
                    GameLogger.info("Restored save from backup");
                } catch (IOException e) {
                    GameLogger.error("Failed to restore backup", e);
                }
            }
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dst);
            byte[] buf = new byte[4096];
            int len;
            while ((len = fis.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        } finally {
            if (fis != null) try { fis.close(); } catch (IOException ignored) {}
            if (fos != null) try { fos.close(); } catch (IOException ignored) {}
        }
    }

    /** Returns a brief summary of the auto-save for display in the start dialog, or null if none. */
    public static String getAutoSaveSummary() {
        File file = new File(AUTOSAVE_FILE);
        if (!file.exists()) return null;
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            return null;
        }
        try {
            int count = Integer.parseInt(props.getProperty("hamsterCount", "0"));
            int money = Integer.parseInt(props.getProperty("money", "0"));
            if (count == 0) return null;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                String prefix = "hamster." + i + ".";
                String name = props.getProperty(prefix + "name", "\uD584\uC2A4\uD130");
                int gen = Integer.parseInt(props.getProperty(prefix + "generation", "1"));
                long ageFrames = Long.parseLong(props.getProperty(prefix + "ageFrames", "0"));
                int ageDays = (int)(ageFrames / GameConstants.FRAMES_PER_DAY);
                if (i > 0) sb.append(", ");
                sb.append(name).append("(").append(ageDays).append("\uC77C");
                if (gen > 1) sb.append(", ").append(gen).append("\uC138\uB300");
                sb.append(")");
            }
            sb.append(" | ").append(money).append("\uCF54\uC778");
            return sb.toString();
        } catch (NumberFormatException e) {
            GameLogger.error("Corrupted auto-save file", e);
            return null;
        }
    }

    private static GameState parseGameState(Properties props) {
        int saveVersion = Integer.parseInt(props.getProperty("saveVersion", "1"));
        GameLogger.debug("Loading save version " + saveVersion);

        GameState state = new GameState();
        state.money = clampInt(props.getProperty("money", "0"), 0, Integer.MAX_VALUE);
        state.totalFrames = Long.parseLong(props.getProperty("totalFrames", "0"));
        state.hamstersRaised = clampInt(props.getProperty("hamstersRaised", "1"), 1, Integer.MAX_VALUE);
        state.qualifiedHamsters = clampInt(props.getProperty("qualifiedHamsters", "0"), 0, Integer.MAX_VALUE);
        state.hamsterPurchaseCount = clampInt(props.getProperty("hamsterPurchaseCount", "0"), 0, Integer.MAX_VALUE);

        int hamsterCount = clampInt(props.getProperty("hamsterCount", "0"), 0, 100);
        state.hamsters = new ArrayList<>();
        for (int i = 0; i < hamsterCount; i++) {
            String prefix = "hamster." + i + ".";
            GameState.HamsterData hd = new GameState.HamsterData();
            hd.name = props.getProperty(prefix + "name", "\uD584\uC2A4\uD130");
            hd.color = safeEnum(HamsterColor.class, props.getProperty(prefix + "color", "BROWN"), HamsterColor.BROWN);
            hd.hunger = clampInt(props.getProperty(prefix + "hunger", "70"), 0, GameConstants.MAX_STAT_CAP);
            hd.happiness = clampInt(props.getProperty(prefix + "happiness", "70"), 0, GameConstants.MAX_STAT_CAP);
            hd.energy = clampInt(props.getProperty(prefix + "energy", "70"), 0, GameConstants.MAX_STAT_CAP);
            hd.poopTimer = clampInt(props.getProperty(prefix + "poopTimer", "0"), 0, Integer.MAX_VALUE);
            hd.ageFrames = Long.parseLong(props.getProperty(prefix + "ageFrames", "0"));
            hd.lifespanFrames = clampInt(props.getProperty(prefix + "lifespanFrames", "225000"), 1, Integer.MAX_VALUE);
            hd.windowX = Integer.parseInt(props.getProperty(prefix + "windowX", "500"));
            hd.windowY = Integer.parseInt(props.getProperty(prefix + "windowY", "700"));
            // Roguelike fields (defaults for backwards compat)
            hd.generation = clampInt(props.getProperty(prefix + "generation", "1"), 1, Integer.MAX_VALUE);
            hd.legacyHungerBonus = clampInt(props.getProperty(prefix + "legacyHungerBonus", "0"), 0, 25);
            hd.legacyHappinessBonus = clampInt(props.getProperty(prefix + "legacyHappinessBonus", "0"), 0, 25);
            hd.legacyEnergyBonus = clampInt(props.getProperty(prefix + "legacyEnergyBonus", "0"), 0, 25);
            hd.legacyLifespanBonus = clampInt(props.getProperty(prefix + "legacyLifespanBonus", "0"), 0, Integer.MAX_VALUE);
            hd.legacyMaxStatBonus = clampInt(props.getProperty(prefix + "legacyMaxStatBonus", "0"), 0, GameConstants.MAX_STAT_CAP - 100);
            hd.maxHunger = clampInt(props.getProperty(prefix + "maxHunger", "100"), 100, GameConstants.MAX_STAT_CAP);
            hd.maxHappiness = clampInt(props.getProperty(prefix + "maxHappiness", "100"), 100, GameConstants.MAX_STAT_CAP);
            hd.maxEnergy = clampInt(props.getProperty(prefix + "maxEnergy", "100"), 100, GameConstants.MAX_STAT_CAP);
            hd.breedCooldownFrames = clampInt(props.getProperty(prefix + "breedCooldownFrames", "0"), 0, Integer.MAX_VALUE);
            int buffCount = clampInt(props.getProperty(prefix + "buffCount", "0"), 0, 50);
            hd.buffs = new java.util.ArrayList<>();
            for (int j = 0; j < buffCount; j++) {
                String bp = prefix + "buff." + j + ".";
                GameState.BuffData bd = new GameState.BuffData();
                bd.type = safeEnum(Buff.Type.class, props.getProperty(bp + "type", "HUNGER_DRAIN"), Buff.Type.HUNGER_DRAIN);
                bd.multiplier = Double.parseDouble(props.getProperty(bp + "multiplier", "1.0"));
                bd.remainingFrames = clampInt(props.getProperty(bp + "remainingFrames", "0"), 0, Integer.MAX_VALUE);
                bd.description = props.getProperty(bp + "description", "");
                hd.buffs.add(bd);
            }
            // 2.0 fields
            hd.personality = props.getProperty(prefix + "personality", "CHEERFUL");
            hd.equippedAccessories = splitList(props.getProperty(prefix + "equippedAccessories", ""));
            hd.ownedAccessories = splitList(props.getProperty(prefix + "ownedAccessories", ""));
            state.hamsters.add(hd);
        }

        int poopCount = clampInt(props.getProperty("poopCount", "0"), 0, 500);
        state.poops = new ArrayList<>();
        for (int i = 0; i < poopCount; i++) {
            String prefix = "poop." + i + ".";
            GameState.PoopData pd = new GameState.PoopData();
            pd.screenX = Integer.parseInt(props.getProperty(prefix + "screenX", "0"));
            pd.screenY = Integer.parseInt(props.getProperty(prefix + "screenY", "0"));
            state.poops.add(pd);
        }

        // Food inventory
        state.foodInventory = FoodInventory.loadFromProperties(props, "inv.");

        return state;
    }

    private static int clampInt(String value, int min, int max) {
        try {
            int v = Integer.parseInt(value);
            return Math.max(min, Math.min(max, v));
        } catch (NumberFormatException e) {
            return min;
        }
    }

    private static <T extends Enum<T>> T safeEnum(Class<T> enumType, String value, T defaultValue) {
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    private static String joinList(java.util.List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (sb.length() > 0) sb.append(",");
            sb.append(s);
        }
        return sb.toString();
    }

    private static java.util.List<String> splitList(String str) {
        java.util.List<String> list = new ArrayList<>();
        if (str == null || str.isEmpty()) return list;
        String[] parts = str.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    private static GameState loadFromFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            // Try backup
            File backup = new File(path + ".bak");
            if (backup.exists()) {
                GameLogger.warn("Main save missing, trying backup: " + path);
                file = backup;
            } else {
                return null;
            }
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            GameLogger.error("Failed to load save: " + path, e);
            // Try backup
            File backup = new File(path + ".bak");
            if (backup.exists() && !file.equals(backup)) {
                GameLogger.info("Trying backup file");
                try (FileInputStream bfis = new FileInputStream(backup)) {
                    props.load(bfis);
                } catch (IOException e2) {
                    GameLogger.error("Failed to load backup", e2);
                    return null;
                }
            } else {
                return null;
            }
        }

        try {
            return parseGameState(props);
        } catch (Exception e) {
            GameLogger.error("Corrupted save data, attempting backup: " + path, e);
            // Try loading from backup
            File backup = new File(path + ".bak");
            if (backup.exists()) {
                Properties backupProps = new Properties();
                try (FileInputStream bfis = new FileInputStream(backup)) {
                    backupProps.load(bfis);
                    GameState backupState = parseGameState(backupProps);
                    GameLogger.info("Successfully loaded from backup");
                    return backupState;
                } catch (Exception e2) {
                    GameLogger.error("Backup also corrupted", e2);
                }
            }
            return null;
        }
    }

}
