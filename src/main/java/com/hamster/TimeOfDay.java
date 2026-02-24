package com.hamster;

import java.util.Calendar;

public enum TimeOfDay {
    DAWN("새벽녘", 5, 7, 1.0, 1.0, 0.7, 1, "\uD83C\uDF05"),
    MORNING("아침", 7, 10, 1.0, 0.8, 1.0, 2, "\u2600\uFE0F"),
    DAYTIME("낮", 10, 17, 1.0, 1.0, 1.0, 1, "\uD83C\uDF24\uFE0F"),
    EVENING("저녁", 17, 20, 1.0, 1.0, 1.0, 1, "\uD83C\uDF07"),
    NIGHT("밤", 20, 24, 0.8, 1.0, 1.3, 0, "\uD83C\uDF19"),
    LATE_NIGHT("심야", 0, 5, 1.0, 1.0, 1.5, 1, "\uD83C\uDF03");

    private final String displayName;
    private final int startHour;
    private final int endHour;
    private final double hungerDrainMult;
    private final double happinessDrainMult;
    private final double energyDrainMult;
    private final int coinBonus;
    private final String emoji;

    TimeOfDay(String displayName, int startHour, int endHour,
              double hungerDrainMult, double happinessDrainMult, double energyDrainMult,
              int coinBonus, String emoji) {
        this.displayName = displayName;
        this.startHour = startHour;
        this.endHour = endHour;
        this.hungerDrainMult = hungerDrainMult;
        this.happinessDrainMult = happinessDrainMult;
        this.energyDrainMult = energyDrainMult;
        this.coinBonus = coinBonus;
        this.emoji = emoji;
    }

    public String getDisplayName() { return displayName; }
    public int getStartHour() { return startHour; }
    public int getEndHour() { return endHour; }
    public double getHungerDrainMult() { return hungerDrainMult; }
    public double getHappinessDrainMult() { return happinessDrainMult; }
    public double getEnergyDrainMult() { return energyDrainMult; }
    public int getCoinBonus() { return coinBonus; }
    public String getEmoji() { return emoji; }

    public double getSleepChanceBonus() {
        if (this == NIGHT) return 0.2;
        if (this == LATE_NIGHT) return 0.4;
        return 0.0;
    }

    public double getEnergyRecoveryMult() {
        if (this == DAWN) return 1.5;
        if (this == LATE_NIGHT) return 1.3;
        return 1.0;
    }

    public static TimeOfDay getCurrentPeriod() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 5) return LATE_NIGHT;
        if (hour >= 5 && hour < 7) return DAWN;
        if (hour >= 7 && hour < 10) return MORNING;
        if (hour >= 10 && hour < 17) return DAYTIME;
        if (hour >= 17 && hour < 20) return EVENING;
        return NIGHT;
    }
}
