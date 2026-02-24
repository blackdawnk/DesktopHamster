package com.hamster;

public enum Achievement {
    // Milestones
    FIRST_HAMSTER("\uCCAB \uD584\uC2A4\uD130", "\uCCAB \uD584\uC2A4\uD130\uB97C \uD0A4\uC6B0\uC138\uC694", RewardType.SEEDS, 1),
    FIVE_HAMSTERS("5\uB9C8\uB9AC \uB2EC\uC131", "\uCD1D 5\uB9C8\uB9AC\uC758 \uD584\uC2A4\uD130\uB97C \uD0A4\uC6B0\uC138\uC694", RewardType.SEEDS, 2),
    TEN_HAMSTERS("10\uB9C8\uB9AC \uB2EC\uC131", "\uCD1D 10\uB9C8\uB9AC\uC758 \uD584\uC2A4\uD130\uB97C \uD0A4\uC6B0\uC138\uC694", RewardType.SEEDS, 3),
    THREE_GENERATIONS("3\uC138\uB300 \uB2EC\uC131", "3\uC138\uB300 \uD584\uC2A4\uD130\uB97C \uD0A4\uC6B0\uC138\uC694", RewardType.SEEDS, 3),
    TEN_GENERATIONS("10\uC138\uB300 \uB2EC\uC131", "10\uC138\uB300 \uD584\uC2A4\uD130\uB97C \uD0A4\uC6B0\uC138\uC694", RewardType.SEEDS, 5),

    // Economy
    FIVE_HUNDRED_COINS("500\uCF54\uC778 \uBAA8\uC74C", "\uCD1D 500\uCF54\uC778\uC744 \uD68D\uB4DD\uD558\uC138\uC694", RewardType.SEEDS, 2),
    FIVE_THOUSAND_COINS("5000\uCF54\uC778 \uBAA8\uC74C", "\uCD1D 5000\uCF54\uC778\uC744 \uD68D\uB4DD\uD558\uC138\uC694", RewardType.SEEDS, 5),

    // Stats
    MAX_STAT("\uCD5C\uB300\uCE58 \uB2EC\uC131", "\uC2A4\uD0EF \uD558\uB098\uB97C \uCD5C\uB300\uCE58\uB85C \uC62C\uB9AC\uC138\uC694", RewardType.SEEDS, 2),
    SURVIVE_30("\uC7A5\uC218 30\uC77C", "\uD584\uC2A4\uD130\uAC00 30\uC77C \uC0DD\uC874\uD558\uC138\uC694", RewardType.SEEDS, 3),
    SURVIVE_50("\uC7A5\uC218 50\uC77C", "\uD584\uC2A4\uD130\uAC00 50\uC77C \uC0DD\uC874\uD558\uC138\uC694", RewardType.SEEDS, 5),

    // Activity
    POOP_50("\uCCAD\uC18C\uBD80 50", "\uC751\uAC00\uB97C 50\uBC88 \uCCAD\uC18C\uD558\uC138\uC694", RewardType.SEEDS, 2),
    POOP_200("\uCCAD\uC18C\uBD80 200", "\uC751\uAC00\uB97C 200\uBC88 \uCCAD\uC18C\uD558\uC138\uC694", RewardType.SEEDS, 3),
    EVENT_20("\uC774\uBCA4\uD2B8 20", "\uC774\uBCA4\uD2B8 20\uAC1C\uB97C \uACBD\uD5D8\uD558\uC138\uC694", RewardType.SEEDS, 2),
    EVENT_100("\uC774\uBCA4\uD2B8 100", "\uC774\uBCA4\uD2B8 100\uAC1C\uB97C \uACBD\uD5D8\uD558\uC138\uC694", RewardType.SEEDS, 5),
    BREED_5("\uAD50\uBC30 5\uD68C", "5\uBC88 \uAD50\uBC30\uD558\uC138\uC694", RewardType.SEEDS, 2),
    BREED_20("\uAD50\uBC30 20\uD68C", "20\uBC88 \uAD50\uBC30\uD558\uC138\uC694", RewardType.SEEDS, 5),
    PLAY_100("\uB180\uAE30 100\uD68C", "100\uBC88 \uB180\uC544\uC8FC\uC138\uC694", RewardType.SEEDS, 3),

    // Collection
    ALL_COLORS("\uBAA8\uB4E0 \uC0C9\uC0C1", "\uBAA8\uB4E0 \uC0C9\uC0C1\uC758 \uD584\uC2A4\uD130\uB97C \uD0A4\uC6B0\uC138\uC694", RewardType.SEEDS, 5),
    ALL_FOODS("\uBAA8\uB4E0 \uC74C\uC2DD", "\uBAA8\uB4E0 \uC885\uB958\uC758 \uC74C\uC2DD\uC744 \uBA39\uC5EC\uBCF4\uC138\uC694", RewardType.SEEDS, 3),
    ALL_PERSONALITIES("\uBAA8\uB4E0 \uC131\uACA9", "\uBAA8\uB4E0 \uC131\uACA9\uC758 \uD584\uC2A4\uD130\uB97C \uB9CC\uB098\uC138\uC694", RewardType.SEEDS, 5),
    FIRST_ACCESSORY("\uCCAB \uC545\uC138\uC11C\uB9AC", "\uCCAB \uC545\uC138\uC11C\uB9AC\uB97C \uAD6C\uB9E4\uD558\uC138\uC694", RewardType.SEEDS, 1),
    ALL_ACCESSORIES("\uBAA8\uB4E0 \uC545\uC138\uC11C\uB9AC", "\uBAA8\uB4E0 \uC545\uC138\uC11C\uB9AC\uB97C \uAD6C\uB9E4\uD558\uC138\uC694", RewardType.SEEDS, 10),

    // Time
    MIDNIGHT("\uC2EC\uC57C \uD584\uC2A4\uD130", "\uC2EC\uC57C 12\uC2DC\uC5D0 \uD50C\uB808\uC774\uD558\uC138\uC694", RewardType.SEEDS, 1),
    EARLY_BIRD("\uC0C8\uBCBD \uD584\uC2A4\uD130", "\uC624\uC804 6\uC2DC\uC5D0 \uD50C\uB808\uC774\uD558\uC138\uC694", RewardType.SEEDS, 1),

    // Interaction
    INTERACTION_50("\uC0AC\uAD50\uAC00 50", "50\uBC88 \uC0C1\uD638\uC791\uC6A9\uD558\uC138\uC694", RewardType.SEEDS, 3);

    public enum RewardType {
        COINS, SEEDS
    }

    private final String displayName;
    private final String description;
    private final RewardType rewardType;
    private final int rewardAmount;

    Achievement(String displayName, String description, RewardType rewardType, int rewardAmount) {
        this.displayName = displayName;
        this.description = description;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public RewardType getRewardType() { return rewardType; }
    public int getRewardAmount() { return rewardAmount; }
}
