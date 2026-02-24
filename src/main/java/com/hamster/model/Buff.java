package com.hamster.model;

public class Buff {
    public enum Type {
        HUNGER_DRAIN,
        HAPPINESS_DRAIN,
        ENERGY_DRAIN,
        COIN_BONUS
    }

    private final Type type;
    private final double multiplier;
    private int remainingFrames;
    private final String description;

    public Buff(Type type, double multiplier, int remainingFrames, String description) {
        this.type = type;
        this.multiplier = multiplier;
        this.remainingFrames = remainingFrames;
        this.description = description;
    }

    public void tick() {
        remainingFrames--;
    }

    public boolean isExpired() {
        return remainingFrames <= 0;
    }

    public Type getType() { return type; }
    public double getMultiplier() { return multiplier; }
    public int getRemainingFrames() { return remainingFrames; }
    public String getDescription() { return description; }
}
