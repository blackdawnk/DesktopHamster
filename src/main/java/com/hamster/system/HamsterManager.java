package com.hamster.system;

import com.hamster.model.*;

import java.util.Random;

/**
 * Manages hamster creation, meta value application, and lifespan calculation.
 * Extracted from Main.java to reduce complexity.
 */
public class HamsterManager {

    private final MetaProgress metaProgress;
    private final Random random;

    public HamsterManager(MetaProgress metaProgress, Random random) {
        this.metaProgress = metaProgress;
        this.random = random;
    }

    /**
     * Calculate a random lifespan based on meta progress.
     */
    public int calculateLifespan() {
        int minDays = metaProgress.getMinLifespanDays();
        int maxDays = metaProgress.getMaxLifespanDays();
        return (minDays + random.nextInt(maxDays - minDays + 1)) * GameConstants.FRAMES_PER_DAY;
    }

    /**
     * Create a new hamster with full meta values and starting stats applied.
     */
    public Hamster createHamster(int screenWidth, HamsterColor color, String name) {
        int lifespanFrames = calculateLifespan();
        Hamster h = new Hamster(screenWidth, 10, color, lifespanFrames);
        h.setName(name);
        applyMetaValues(h);
        applyStartingStats(h);
        return h;
    }

    /**
     * Create a baby hamster from two parents (breeding).
     */
    public Hamster createBabyHamster(int screenWidth, Hamster parent1, Hamster parent2, String name) {
        HamsterColor babyColor = random.nextBoolean() ? parent1.getColor() : parent2.getColor();
        int babyGen = Math.max(parent1.getGeneration(), parent2.getGeneration()) + 1;

        Hamster baby = createHamster(screenWidth, babyColor, name);
        baby.setGeneration(babyGen);

        // Baby inherits max of parents' legacy bonuses
        baby.setLegacyHungerBonus(Math.max(parent1.getLegacyHungerBonus(), parent2.getLegacyHungerBonus()));
        baby.setLegacyHappinessBonus(Math.max(parent1.getLegacyHappinessBonus(), parent2.getLegacyHappinessBonus()));
        baby.setLegacyEnergyBonus(Math.max(parent1.getLegacyEnergyBonus(), parent2.getLegacyEnergyBonus()));
        baby.setLegacyLifespanBonus(Math.max(parent1.getLegacyLifespanBonus(), parent2.getLegacyLifespanBonus()));
        baby.setLegacyMaxStatBonus(Math.max(parent1.getLegacyMaxStatBonus(), parent2.getLegacyMaxStatBonus()));
        baby.applyLegacyBonuses();

        return baby;
    }

    /**
     * Apply pending legacy bonuses to a hamster.
     */
    public void applyPendingLegacy(Hamster h, int[] pendingLegacy) {
        if (pendingLegacy == null) return;
        h.setGeneration(pendingLegacy[0]);
        h.setLegacyHungerBonus(pendingLegacy[1]);
        h.setLegacyHappinessBonus(pendingLegacy[2]);
        h.setLegacyEnergyBonus(pendingLegacy[3]);
        h.setLegacyLifespanBonus(pendingLegacy[4]);
        h.setLegacyMaxStatBonus(pendingLegacy[5]);
        h.applyLegacyBonuses();
    }

    /**
     * Register a hamster with the achievement manager (color, personality, accessories).
     */
    public void registerWithAchievements(Hamster h, AchievementManager achMgr) {
        achMgr.colorsSeen.add(h.getColor().name());
        if (h.getPersonality() != null) {
            achMgr.personalitiesSeen.add(h.getPersonality().name());
        }
        for (String accName : achMgr.accessoriesBought) {
            h.getOwnedAccessories().add(accName);
        }
    }

    /**
     * Apply meta-progress values to a hamster.
     */
    public void applyMetaValues(Hamster h) {
        h.setAgingSpeed(metaProgress.getAgingSpeed());
        h.setActionGain(metaProgress.getActionGain());
        h.setDrainMultiplier(metaProgress.getDrainMultiplier());
        h.setDrainInterval(metaProgress.getDrainInterval());
        h.setCoinBonus(metaProgress.getCoinBonus());
        h.setPoopChanceMultiplier(metaProgress.getPoopChanceMultiplier());
        h.setPoopPenaltyMultiplier(metaProgress.getPoopPenaltyMultiplier());
        h.setBuffDurationMultiplier(metaProgress.getBuffDurationMultiplier());
    }

    /**
     * Apply starting stats from meta-progress.
     */
    public void applyStartingStats(Hamster h) {
        int stats = metaProgress.getStartingStats();
        h.setHunger(stats);
        h.setHappiness(stats);
        h.setEnergy(stats);
    }

    /**
     * Determine cause of death for a hamster.
     */
    public String getCauseOfDeath(Hamster h) {
        if (h.getAgeFrames() >= h.getLifespanFrames()) return "\uB178\uD658";
        if (h.getHunger() <= 0) return "\uBC30\uACE0\uD514 \uBD80\uC871";
        if (h.getHappiness() <= 0) return "\uD589\uBCF5 \uBD80\uC871";
        if (h.getEnergy() <= 0) return "\uCCB4\uB825 \uBD80\uC871";
        return "\uAC8C\uC784 \uD3EC\uAE30";
    }

    /**
     * Compute the ground Y position for hamster placement.
     */
    public int getGroundY(java.awt.Dimension screenSize) {
        java.awt.Insets insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(
                java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());
        return screenSize.height - insets.bottom - 100;
    }
}
