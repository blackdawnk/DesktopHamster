package com.hamster;

public enum FoodItem {
    SEED("\uD574\uBC14\uB77C\uAE30\uC528", 3, 15, 0, 0, "\uAE30\uBCF8\uC801\uC778 \uD584\uC2A4\uD130 \uBA39\uC774", "\uD83C\uDF3B"),
    CARROT("\uB2F9\uADFC", 8, 20, 0, 0, "\uC2E0\uC120\uD55C \uB2F9\uADFC", "\uD83E\uDD55"),
    APPLE("\uC0AC\uACFC", 12, 18, 10, 0, "\uB2EC\uCF64\uD55C \uC0AC\uACFC \uC870\uAC01", "\uD83C\uDF4E"),
    CHEESE("\uCE58\uC988", 18, 25, 8, 5, "\uACE0\uC18C\uD55C \uCE58\uC988", "\uD83E\uDDC0"),
    BROCCOLI("\uBE0C\uB85C\uCF5C\uB9AC", 6, 12, 0, 5, "\uAC74\uAC15\uD55C \uBE0C\uB85C\uCF5C\uB9AC", "\uD83E\uDD66"),
    YOGURT("\uC694\uAC70\uD2B8", 22, 20, 15, 0, "\uB9DB\uC788\uB294 \uC694\uAC70\uD2B8", "\uD83E\uDD5B"),
    NUT_MIX("\uACCC\uACFC\uB958 \uBBF9\uC2A4", 28, 30, 0, 10, "\uB2E4\uC591\uD55C \uACCC\uACFC\uB958", "\uD83E\uDD5C"),
    DRIED_FRUIT("\uAC74\uACFC\uC77C", 15, 15, 12, 0, "\uB9D0\uB9B0 \uACFC\uC77C \uBAA8\uC74C", "\uD83C\uDF47"),
    PREMIUM_PELLET("\uD504\uB9AC\uBBF8\uC5C4 \uD3A0\uB9BF", 45, 40, 10, 10, "\uCD5C\uACE0\uAE09 \uC601\uC591 \uD3A0\uB9BF", "\u2728"),
    CAKE("\uCF00\uC774\uD06C", 35, 0, 30, -5, "\uB2EC\uCF64\uD55C \uCF00\uC774\uD06C \uC870\uAC01", "\uD83C\uDF70");

    private final String displayName;
    private final int cost;
    private final int hungerGain;
    private final int happinessGain;
    private final int energyGain;
    private final String description;
    private final String emoji;

    FoodItem(String displayName, int cost, int hungerGain, int happinessGain, int energyGain, String description, String emoji) {
        this.displayName = displayName;
        this.cost = cost;
        this.hungerGain = hungerGain;
        this.happinessGain = happinessGain;
        this.energyGain = energyGain;
        this.description = description;
        this.emoji = emoji;
    }

    public String getDisplayName() { return displayName; }
    public int getCost() { return cost; }
    public int getHungerGain() { return hungerGain; }
    public int getHappinessGain() { return happinessGain; }
    public int getEnergyGain() { return energyGain; }
    public String getDescription() { return description; }
    public String getEmoji() { return emoji; }

    public String getEffectText() {
        StringBuilder sb = new StringBuilder();
        if (hungerGain > 0) sb.append("\uBC30\uACE0\uD514+").append(hungerGain).append(" ");
        if (happinessGain > 0) sb.append("\uD589\uBCF5+").append(happinessGain).append(" ");
        else if (happinessGain < 0) sb.append("\uD589\uBCF5").append(happinessGain).append(" ");
        if (energyGain > 0) sb.append("\uCCB4\uB825+").append(energyGain);
        else if (energyGain < 0) sb.append("\uCCB4\uB825").append(energyGain);
        return sb.toString().trim();
    }
}
