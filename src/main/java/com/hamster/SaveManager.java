package com.hamster;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class SaveManager {

    private static final String SAVE_DIR = System.getProperty("user.home") + "/.desktophamster/";
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

        Properties props = new Properties();
        props.setProperty("money", String.valueOf(state.money));
        props.setProperty("totalFrames", String.valueOf(state.totalFrames));
        props.setProperty("hamstersRaised", String.valueOf(state.hamstersRaised));

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
        }

        props.setProperty("poopCount", String.valueOf(state.poops.size()));
        for (int i = 0; i < state.poops.size(); i++) {
            GameState.PoopData pd = state.poops.get(i);
            String prefix = "poop." + i + ".";
            props.setProperty(prefix + "screenX", String.valueOf(pd.screenX));
            props.setProperty(prefix + "screenY", String.valueOf(pd.screenY));
        }

        File tempFile = new File(path + ".tmp");
        File targetFile = new File(path);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            props.store(fos, "DesktopHamster Save - " + label);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (targetFile.exists()) {
            targetFile.delete();
        }
        tempFile.renameTo(targetFile);
    }

    private static GameState loadFromFile(String path) {
        File file = new File(path);
        if (!file.exists()) return null;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        GameState state = new GameState();
        state.money = Integer.parseInt(props.getProperty("money", "0"));
        state.totalFrames = Long.parseLong(props.getProperty("totalFrames", "0"));
        state.hamstersRaised = Integer.parseInt(props.getProperty("hamstersRaised", "1"));

        int hamsterCount = Integer.parseInt(props.getProperty("hamsterCount", "0"));
        state.hamsters = new ArrayList<>();
        for (int i = 0; i < hamsterCount; i++) {
            String prefix = "hamster." + i + ".";
            GameState.HamsterData hd = new GameState.HamsterData();
            hd.name = props.getProperty(prefix + "name", "\uD584\uC2A4\uD130");
            hd.color = HamsterColor.valueOf(props.getProperty(prefix + "color", "BROWN"));
            hd.hunger = Integer.parseInt(props.getProperty(prefix + "hunger", "70"));
            hd.happiness = Integer.parseInt(props.getProperty(prefix + "happiness", "70"));
            hd.energy = Integer.parseInt(props.getProperty(prefix + "energy", "70"));
            hd.poopTimer = Integer.parseInt(props.getProperty(prefix + "poopTimer", "0"));
            hd.ageFrames = Long.parseLong(props.getProperty(prefix + "ageFrames", "0"));
            hd.lifespanFrames = Integer.parseInt(props.getProperty(prefix + "lifespanFrames", "225000"));
            hd.windowX = Integer.parseInt(props.getProperty(prefix + "windowX", "500"));
            hd.windowY = Integer.parseInt(props.getProperty(prefix + "windowY", "700"));
            // Roguelike fields (defaults for backwards compat)
            hd.generation = Integer.parseInt(props.getProperty(prefix + "generation", "1"));
            hd.legacyHungerBonus = Integer.parseInt(props.getProperty(prefix + "legacyHungerBonus", "0"));
            hd.legacyHappinessBonus = Integer.parseInt(props.getProperty(prefix + "legacyHappinessBonus", "0"));
            hd.legacyEnergyBonus = Integer.parseInt(props.getProperty(prefix + "legacyEnergyBonus", "0"));
            hd.legacyLifespanBonus = Integer.parseInt(props.getProperty(prefix + "legacyLifespanBonus", "0"));
            hd.legacyMaxStatBonus = Integer.parseInt(props.getProperty(prefix + "legacyMaxStatBonus", "0"));
            hd.maxHunger = Integer.parseInt(props.getProperty(prefix + "maxHunger", "100"));
            hd.maxHappiness = Integer.parseInt(props.getProperty(prefix + "maxHappiness", "100"));
            hd.maxEnergy = Integer.parseInt(props.getProperty(prefix + "maxEnergy", "100"));
            hd.breedCooldownFrames = Integer.parseInt(props.getProperty(prefix + "breedCooldownFrames", "0"));
            int buffCount = Integer.parseInt(props.getProperty(prefix + "buffCount", "0"));
            hd.buffs = new java.util.ArrayList<>();
            for (int j = 0; j < buffCount; j++) {
                String bp = prefix + "buff." + j + ".";
                GameState.BuffData bd = new GameState.BuffData();
                bd.type = Buff.Type.valueOf(props.getProperty(bp + "type", "HUNGER_DRAIN"));
                bd.multiplier = Double.parseDouble(props.getProperty(bp + "multiplier", "1.0"));
                bd.remainingFrames = Integer.parseInt(props.getProperty(bp + "remainingFrames", "0"));
                bd.description = props.getProperty(bp + "description", "");
                hd.buffs.add(bd);
            }
            state.hamsters.add(hd);
        }

        int poopCount = Integer.parseInt(props.getProperty("poopCount", "0"));
        state.poops = new ArrayList<>();
        for (int i = 0; i < poopCount; i++) {
            String prefix = "poop." + i + ".";
            GameState.PoopData pd = new GameState.PoopData();
            pd.screenX = Integer.parseInt(props.getProperty(prefix + "screenX", "0"));
            pd.screenY = Integer.parseInt(props.getProperty(prefix + "screenY", "0"));
            state.poops.add(pd);
        }

        return state;
    }

}
