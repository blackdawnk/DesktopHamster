package com.hamster;

import java.util.Random;

public enum Personality {
    GLUTTON("먹보", "항상 배고픈 햄스터",
            1.3, 1.0, 1.0, 1.3, 0.9, 1.0, 0.9),
    CHEERFUL("쾌활한", "언제나 밝고 즐거운 햄스터",
            1.0, 0.8, 1.0, 1.0, 1.3, 1.0, 1.1),
    LAZY("게으른", "느긋하고 여유로운 햄스터",
            0.9, 1.0, 1.3, 0.9, 0.9, 1.3, 0.8),
    ENERGETIC("활발한", "에너지 넘치는 햄스터",
            1.1, 1.1, 0.7, 1.0, 1.1, 0.8, 1.0),
    TIMID("소심한", "조용하고 겁 많은 햄스터",
            1.0, 1.2, 1.1, 1.0, 0.8, 1.1, 0.9),
    HARDY("튼튼한", "건강하고 강인한 햄스터",
            0.9, 1.0, 0.8, 1.1, 1.0, 1.1, 1.0),
    GREEDY("욕심쟁이", "코인을 잘 모으는 햄스터",
            1.1, 1.1, 1.0, 1.0, 1.0, 1.0, 1.5),
    SOCIAL("사교적인", "다른 햄스터와 잘 어울리는 햄스터",
            1.0, 0.9, 1.0, 1.0, 1.2, 1.0, 1.1);

    private final String displayName;
    private final String description;
    private final double hungerDrainMult;
    private final double happinessDrainMult;
    private final double energyDrainMult;
    private final double feedMult;
    private final double playMult;
    private final double sleepMult;
    private final double coinMult;

    Personality(String displayName, String description,
                double hungerDrainMult, double happinessDrainMult, double energyDrainMult,
                double feedMult, double playMult, double sleepMult, double coinMult) {
        this.displayName = displayName;
        this.description = description;
        this.hungerDrainMult = hungerDrainMult;
        this.happinessDrainMult = happinessDrainMult;
        this.energyDrainMult = energyDrainMult;
        this.feedMult = feedMult;
        this.playMult = playMult;
        this.sleepMult = sleepMult;
        this.coinMult = coinMult;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public double getHungerDrainMult() { return hungerDrainMult; }
    public double getHappinessDrainMult() { return happinessDrainMult; }
    public double getEnergyDrainMult() { return energyDrainMult; }
    public double getFeedMult() { return feedMult; }
    public double getPlayMult() { return playMult; }
    public double getSleepMult() { return sleepMult; }
    public double getCoinMult() { return coinMult; }

    public static Personality random(Random rng) {
        Personality[] values = values();
        return values[rng.nextInt(values.length)];
    }
}
