package com.hamster.system;
import com.hamster.model.Hamster;

import java.io.*;
import java.util.Properties;

public class MetaProgress {

    private static final String SAVE_DIR = System.getProperty("user.home") + "/.desktophamster/";
    private static final String META_FILE = SAVE_DIR + "meta.properties";

    public int sunflowerSeeds = 0;

    // Upgrade levels
    public int lifespanLevel = 0;
    public int agingLevel = 0;
    public int actionGainLevel = 0;
    public int drainLevel = 0;
    public int drainIntervalLevel = 0;
    public int hamsterSlotLevel = 0;
    public int breedAgeLevel = 0;
    public int coinBonusLevel = 0;
    public int poopFreqLevel = 0;
    public int poopPenaltyLevel = 0;
    public int eventIntervalLevel = 0;
    public int buffDurationLevel = 0;
    public int startingStatsLevel = 0;

    // Max levels
    public static final int MAX_LIFESPAN_LEVEL = 50;
    public static final int MAX_AGING_LEVEL = 30;
    public static final int MAX_ACTION_GAIN_LEVEL = 50;
    public static final int MAX_DRAIN_LEVEL = 30;
    public static final int MAX_DRAIN_INTERVAL_LEVEL = 50;
    public static final int MAX_HAMSTER_SLOT_LEVEL = 30;
    public static final int MAX_BREED_AGE_LEVEL = 30;
    public static final int MAX_COIN_BONUS_LEVEL = 50;
    public static final int MAX_POOP_FREQ_LEVEL = 30;
    public static final int MAX_POOP_PENALTY_LEVEL = 30;
    public static final int MAX_EVENT_INTERVAL_LEVEL = 50;
    public static final int MAX_BUFF_DURATION_LEVEL = 30;
    public static final int MAX_STARTING_STATS_LEVEL = 30;

    // ===================== Derived values =====================

    /** 수명 범위: base 5~8일, +1/+1 per level */
    public int getMinLifespanDays() { return 5 + lifespanLevel; }
    public int getMaxLifespanDays() { return 8 + lifespanLevel; }

    /** 노화 속도: base 2.0x, -0.05 per level → min 0.5 at lv30 */
    public double getAgingSpeed() { return Math.max(0.5, 2.0 - agingLevel * 0.05); }

    /** 행동 효과: base +5, +1 per level */
    public int getActionGain() { return 5 + actionGainLevel; }

    /** 감소량: base 1.0x, -0.02 per level → min 0.4 at lv30 */
    public double getDrainMultiplier() { return Math.max(0.2, 1.0 - drainLevel * 0.02); }

    /** 감소 주기: base 300 frames (10초), +15 per level */
    public int getDrainInterval() { return 300 + drainIntervalLevel * 15; }

    /** 햄스터 슬롯: base 1, +1 per level */
    public int getMaxHamsterSlots() { return 1 + hamsterSlotLevel; }

    /** 교배 나이: base 2.0일, -0.05 per level → min 0.5일 at lv30 */
    public double getBreedAgeDays() { return Math.max(0.1, 2.0 - breedAgeLevel * 0.05); }
    public int getBreedAgeFrames() { return (int)(getBreedAgeDays() * Hamster.FRAMES_PER_DAY); }
    public String getBreedAgeDaysText() {
        double days = getBreedAgeDays();
        if (days == (int) days) return (int) days + "\uC77C";
        return String.format("%.1f\uC77C", days);
    }

    /** 코인 획득량: +1 per level */
    public int getCoinBonus() { return coinBonusLevel; }

    /** 응가 빈도: base 100%, -2% per level → 40% at lv30 */
    public double getPoopChanceMultiplier() { return Math.max(0.1, 1.0 - poopFreqLevel * 0.02); }

    /** 응가 패널티: base 100%, -3% per level → 10% at lv30 */
    public double getPoopPenaltyMultiplier() { return Math.max(0.1, 1.0 - poopPenaltyLevel * 0.03); }

    /** 이벤트 주기: base 4500 frames (~2.5분), -75 per level → min 750 (~25초) at lv50 */
    public int getEventInterval() { return Math.max(750, 4500 - eventIntervalLevel * 75); }

    /** 버프 지속: base 1.0x, +0.1 per level → 4.0x at lv30 */
    public double getBuffDurationMultiplier() { return 1.0 + buffDurationLevel * 0.1; }

    /** 초기 스탯: base 80, +1 per level → 110 at lv30 */
    public int getStartingStats() { return 80 + startingStatsLevel; }

    // ===================== Upgrade costs =====================

    public int getLifespanCost() { return 3 * (lifespanLevel + 1); }
    public int getAgingCost() { return 8 * (agingLevel + 1); }
    public int getActionGainCost() { return 5 * (actionGainLevel + 1); }
    public int getDrainCost() { return 8 * (drainLevel + 1); }
    public int getDrainIntervalCost() { return 6 * (drainIntervalLevel + 1); }
    public int getHamsterSlotCost() { return 10 * (hamsterSlotLevel + 1); }
    public int getBreedAgeCost() { return 8 * (breedAgeLevel + 1); }
    public int getCoinBonusCost() { return 5 * (coinBonusLevel + 1); }
    public int getPoopFreqCost() { return 4 * (poopFreqLevel + 1); }
    public int getPoopPenaltyCost() { return 4 * (poopPenaltyLevel + 1); }
    public int getEventIntervalCost() { return 6 * (eventIntervalLevel + 1); }
    public int getBuffDurationCost() { return 6 * (buffDurationLevel + 1); }
    public int getStartingStatsCost() { return 5 * (startingStatsLevel + 1); }

    // ===================== Can upgrade checks =====================

    public boolean canUpgradeLifespan() { return lifespanLevel < MAX_LIFESPAN_LEVEL; }
    public boolean canUpgradeAging() { return agingLevel < MAX_AGING_LEVEL; }
    public boolean canUpgradeActionGain() { return actionGainLevel < MAX_ACTION_GAIN_LEVEL; }
    public boolean canUpgradeDrain() { return drainLevel < MAX_DRAIN_LEVEL; }
    public boolean canUpgradeDrainInterval() { return drainIntervalLevel < MAX_DRAIN_INTERVAL_LEVEL; }
    public boolean canUpgradeHamsterSlot() { return hamsterSlotLevel < MAX_HAMSTER_SLOT_LEVEL; }
    public boolean canUpgradeBreedAge() { return breedAgeLevel < MAX_BREED_AGE_LEVEL; }
    public boolean canUpgradeCoinBonus() { return coinBonusLevel < MAX_COIN_BONUS_LEVEL; }
    public boolean canUpgradePoopFreq() { return poopFreqLevel < MAX_POOP_FREQ_LEVEL; }
    public boolean canUpgradePoopPenalty() { return poopPenaltyLevel < MAX_POOP_PENALTY_LEVEL; }
    public boolean canUpgradeEventInterval() { return eventIntervalLevel < MAX_EVENT_INTERVAL_LEVEL; }
    public boolean canUpgradeBuffDuration() { return buffDurationLevel < MAX_BUFF_DURATION_LEVEL; }
    public boolean canUpgradeStartingStats() { return startingStatsLevel < MAX_STARTING_STATS_LEVEL; }

    // ===================== Upgrade methods =====================

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

    public boolean upgradeDrainInterval() {
        if (!canUpgradeDrainInterval() || sunflowerSeeds < getDrainIntervalCost()) return false;
        sunflowerSeeds -= getDrainIntervalCost();
        drainIntervalLevel++;
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

    public boolean upgradeCoinBonus() {
        if (!canUpgradeCoinBonus() || sunflowerSeeds < getCoinBonusCost()) return false;
        sunflowerSeeds -= getCoinBonusCost();
        coinBonusLevel++;
        save();
        return true;
    }

    public boolean upgradePoopFreq() {
        if (!canUpgradePoopFreq() || sunflowerSeeds < getPoopFreqCost()) return false;
        sunflowerSeeds -= getPoopFreqCost();
        poopFreqLevel++;
        save();
        return true;
    }

    public boolean upgradePoopPenalty() {
        if (!canUpgradePoopPenalty() || sunflowerSeeds < getPoopPenaltyCost()) return false;
        sunflowerSeeds -= getPoopPenaltyCost();
        poopPenaltyLevel++;
        save();
        return true;
    }

    public boolean upgradeEventInterval() {
        if (!canUpgradeEventInterval() || sunflowerSeeds < getEventIntervalCost()) return false;
        sunflowerSeeds -= getEventIntervalCost();
        eventIntervalLevel++;
        save();
        return true;
    }

    public boolean upgradeBuffDuration() {
        if (!canUpgradeBuffDuration() || sunflowerSeeds < getBuffDurationCost()) return false;
        sunflowerSeeds -= getBuffDurationCost();
        buffDurationLevel++;
        save();
        return true;
    }

    public boolean upgradeStartingStats() {
        if (!canUpgradeStartingStats() || sunflowerSeeds < getStartingStatsCost()) return false;
        sunflowerSeeds -= getStartingStatsCost();
        startingStatsLevel++;
        save();
        return true;
    }

    // ===================== Seeds =====================

    public void addSeeds(int amount) {
        sunflowerSeeds += amount;
        save();
    }

    public static int calculateSeeds(int hamstersRaised, int remainingCoins) {
        return hamstersRaised * 10 + remainingCoins / 5;
    }

    // ===================== Save / Load =====================

    public void save() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();

        Properties props = new Properties();
        props.setProperty("sunflowerSeeds", String.valueOf(sunflowerSeeds));
        props.setProperty("lifespanLevel", String.valueOf(lifespanLevel));
        props.setProperty("agingLevel", String.valueOf(agingLevel));
        props.setProperty("actionGainLevel", String.valueOf(actionGainLevel));
        props.setProperty("drainLevel", String.valueOf(drainLevel));
        props.setProperty("drainIntervalLevel", String.valueOf(drainIntervalLevel));
        props.setProperty("hamsterSlotLevel", String.valueOf(hamsterSlotLevel));
        props.setProperty("breedAgeLevel", String.valueOf(breedAgeLevel));
        props.setProperty("coinBonusLevel", String.valueOf(coinBonusLevel));
        props.setProperty("poopFreqLevel", String.valueOf(poopFreqLevel));
        props.setProperty("poopPenaltyLevel", String.valueOf(poopPenaltyLevel));
        props.setProperty("eventIntervalLevel", String.valueOf(eventIntervalLevel));
        props.setProperty("buffDurationLevel", String.valueOf(buffDurationLevel));
        props.setProperty("startingStatsLevel", String.valueOf(startingStatsLevel));

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
        meta.drainIntervalLevel = Integer.parseInt(props.getProperty("drainIntervalLevel", "0"));
        meta.hamsterSlotLevel = Integer.parseInt(props.getProperty("hamsterSlotLevel", "0"));
        meta.breedAgeLevel = Integer.parseInt(props.getProperty("breedAgeLevel", "0"));
        meta.coinBonusLevel = Integer.parseInt(props.getProperty("coinBonusLevel", "0"));
        meta.poopFreqLevel = Integer.parseInt(props.getProperty("poopFreqLevel", "0"));
        meta.poopPenaltyLevel = Integer.parseInt(props.getProperty("poopPenaltyLevel", "0"));
        meta.eventIntervalLevel = Integer.parseInt(props.getProperty("eventIntervalLevel", "0"));
        meta.buffDurationLevel = Integer.parseInt(props.getProperty("buffDurationLevel", "0"));
        meta.startingStatsLevel = Integer.parseInt(props.getProperty("startingStatsLevel", "0"));

        return meta;
    }
}
