package com.hamster;

import java.io.*;
import java.util.Properties;

public class MetaProgress {

    private static final String SAVE_DIR = System.getProperty("user.home") + "/.desktophamster/";
    private static final String META_FILE = SAVE_DIR + "meta.properties";

    int sunflowerSeeds = 0;

    // Upgrade levels
    int lifespanLevel = 0;
    int agingLevel = 0;
    int actionGainLevel = 0;
    int drainLevel = 0;
    int hamsterSlotLevel = 0;
    int breedAgeLevel = 0;

    // Max levels
    public static final int MAX_LIFESPAN_LEVEL = 10;
    public static final int MAX_AGING_LEVEL = 2;
    public static final int MAX_ACTION_GAIN_LEVEL = 9;
    public static final int MAX_DRAIN_LEVEL = 2;
    public static final int MAX_HAMSTER_SLOT_LEVEL = 4;
    public static final int MAX_BREED_AGE_LEVEL = 2;

    // Derived values
    public int getMinLifespanDays() { return 3 + lifespanLevel * 2; }
    public int getMaxLifespanDays() { return 5 + lifespanLevel * 3; }
    public int getAgingSpeed() { return Math.max(1, 3 - agingLevel); }
    public int getActionGain() { return 3 + actionGainLevel; }
    public int getDrainMultiplier() { return Math.max(1, 3 - drainLevel); }
    public int getMaxHamsterSlots() { return 1 + hamsterSlotLevel; }
    public int getBreedAgeFrames() { return (int)((2.0 - breedAgeLevel * 0.5) * Hamster.FRAMES_PER_DAY); }
    public String getBreedAgeDaysText() {
        switch (breedAgeLevel) {
            case 0: return "2\uC77C";
            case 1: return "1.5\uC77C";
            case 2: return "1\uC77C";
            default: return "2\uC77C";
        }
    }

    // Upgrade costs (sunflower seeds)
    public int getLifespanCost() { return 5 * (lifespanLevel + 1); }
    public int getAgingCost() { return 15 * (agingLevel + 1); }
    public int getActionGainCost() { return 10 * (actionGainLevel + 1); }
    public int getDrainCost() { return 15 * (drainLevel + 1); }
    public int getHamsterSlotCost() { return 20 * (hamsterSlotLevel + 1); }
    public int getBreedAgeCost() { return 15 * (breedAgeLevel + 1); }

    // Can upgrade checks
    public boolean canUpgradeLifespan() { return lifespanLevel < MAX_LIFESPAN_LEVEL; }
    public boolean canUpgradeAging() { return agingLevel < MAX_AGING_LEVEL; }
    public boolean canUpgradeActionGain() { return actionGainLevel < MAX_ACTION_GAIN_LEVEL; }
    public boolean canUpgradeDrain() { return drainLevel < MAX_DRAIN_LEVEL; }
    public boolean canUpgradeHamsterSlot() { return hamsterSlotLevel < MAX_HAMSTER_SLOT_LEVEL; }
    public boolean canUpgradeBreedAge() { return breedAgeLevel < MAX_BREED_AGE_LEVEL; }

    public boolean upgradeLifespan() {
        if (!canUpgradeLifespan() || sunflowerSeeds < getLifespanCost()) return false;
        sunflowerSeeds -= getLifespanCost();
        lifespanLevel++;
        save();
        return true;
    }

    public boolean upgradeAging() {
        if (!canUpgradeAging() || sunflowerSeeds < getAgingCost()) return false;
        sunflowerSeeds -= getAgingCost();
        agingLevel++;
        save();
        return true;
    }

    public boolean upgradeActionGain() {
        if (!canUpgradeActionGain() || sunflowerSeeds < getActionGainCost()) return false;
        sunflowerSeeds -= getActionGainCost();
        actionGainLevel++;
        save();
        return true;
    }

    public boolean upgradeDrain() {
        if (!canUpgradeDrain() || sunflowerSeeds < getDrainCost()) return false;
        sunflowerSeeds -= getDrainCost();
        drainLevel++;
        save();
        return true;
    }

    public boolean upgradeHamsterSlot() {
        if (!canUpgradeHamsterSlot() || sunflowerSeeds < getHamsterSlotCost()) return false;
        sunflowerSeeds -= getHamsterSlotCost();
        hamsterSlotLevel++;
        save();
        return true;
    }

    public boolean upgradeBreedAge() {
        if (!canUpgradeBreedAge() || sunflowerSeeds < getBreedAgeCost()) return false;
        sunflowerSeeds -= getBreedAgeCost();
        breedAgeLevel++;
        save();
        return true;
    }

    public void addSeeds(int amount) {
        sunflowerSeeds += amount;
        save();
    }

    public static int calculateSeeds(int hamstersRaised, int remainingCoins) {
        return hamstersRaised * 10 + remainingCoins / 5;
    }

    public void save() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();

        Properties props = new Properties();
        props.setProperty("sunflowerSeeds", String.valueOf(sunflowerSeeds));
        props.setProperty("lifespanLevel", String.valueOf(lifespanLevel));
        props.setProperty("agingLevel", String.valueOf(agingLevel));
        props.setProperty("actionGainLevel", String.valueOf(actionGainLevel));
        props.setProperty("drainLevel", String.valueOf(drainLevel));
        props.setProperty("hamsterSlotLevel", String.valueOf(hamsterSlotLevel));
        props.setProperty("breedAgeLevel", String.valueOf(breedAgeLevel));

        try (FileOutputStream fos = new FileOutputStream(META_FILE)) {
            props.store(fos, "DesktopHamster Meta Progress");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MetaProgress load() {
        MetaProgress meta = new MetaProgress();
        File file = new File(META_FILE);
        if (!file.exists()) return meta;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return meta;
        }

        meta.sunflowerSeeds = Integer.parseInt(props.getProperty("sunflowerSeeds", "0"));
        meta.lifespanLevel = Integer.parseInt(props.getProperty("lifespanLevel", "0"));
        meta.agingLevel = Integer.parseInt(props.getProperty("agingLevel", "0"));
        meta.actionGainLevel = Integer.parseInt(props.getProperty("actionGainLevel", "0"));
        meta.drainLevel = Integer.parseInt(props.getProperty("drainLevel", "0"));
        meta.hamsterSlotLevel = Integer.parseInt(props.getProperty("hamsterSlotLevel", "0"));
        meta.breedAgeLevel = Integer.parseInt(props.getProperty("breedAgeLevel", "0"));

        return meta;
    }
}
