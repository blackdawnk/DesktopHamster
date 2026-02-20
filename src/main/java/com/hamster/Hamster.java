package com.hamster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Hamster {

    public enum State {
        IDLE, WALKING, EATING, SLEEPING, HAPPY, RUNNING_WHEEL
    }

    public static final int FRAMES_PER_DAY = 9000; // ~5 minutes real time

    private int x, y;
    private int direction = 1; // 1: right, -1: left (for rendering)
    private double moveX = 2.0;
    private double moveY = 0.0;
    private State state = State.IDLE;
    private int stateTimer = 0;
    private int animFrame = 0;

    private String name = "내 햄스터";

    private int hunger = 70;    // 0~100
    private int happiness = 70; // 0~100
    private int energy = 70;    // 0~100

    private int poopTimer = 0;
    private boolean userAction = false; // true when action was user-initiated
    private int pendingCoins = 0;       // coins accumulated from ongoing actions

    // Meta-configurable values (set by Main from MetaProgress)
    private int agingSpeed = 3;        // frames aged per update (default: 3x fast)
    private int actionGain = 3;        // stat gain per action tick
    private int drainMultiplier = 3;   // passive stat drain multiplier

    private HamsterColor color;
    private long ageFrames = 0;
    private int lifespanFrames;
    private boolean dead = false;

    // Max stats (can be increased via legacy/upgrades)
    private int maxHunger = 100;
    private int maxHappiness = 100;
    private int maxEnergy = 100;
    public static final int MAX_STAT_CAP = 200;

    // Roguelike fields
    private int generation = 1;
    private int legacyHungerBonus = 0;
    private int legacyHappinessBonus = 0;
    private int legacyEnergyBonus = 0;
    private int legacyLifespanBonus = 0;
    private int legacyMaxStatBonus = 0;
    private final List<Buff> buffs = new ArrayList<>();
    private int breedCooldownFrames = 0;

    private final Random random = new Random();
    private final int screenWidth;
    private final int groundY;

    public Hamster(int screenWidth, int groundY) {
        this(screenWidth, groundY, HamsterColor.BROWN);
    }

    public Hamster(int screenWidth, int groundY, HamsterColor color) {
        this.screenWidth = screenWidth;
        this.groundY = groundY;
        this.x = screenWidth / 2;
        this.y = groundY;
        this.color = color;
        // Random lifespan: 20~35 days
        this.lifespanFrames = (20 + random.nextInt(16)) * FRAMES_PER_DAY;
    }

    public Hamster(int screenWidth, int groundY, HamsterColor color, int lifespanFrames) {
        this.screenWidth = screenWidth;
        this.groundY = groundY;
        this.x = screenWidth / 2;
        this.y = groundY;
        this.color = color;
        this.lifespanFrames = lifespanFrames;
    }

    public void update() {
        if (dead) return;

        ageFrames += agingSpeed;
        if (ageFrames >= lifespanFrames) {
            dead = true;
            return;
        }

        animFrame++;
        stateTimer--;
        poopTimer++;

        // Tick breed cooldown
        if (breedCooldownFrames > 0) {
            breedCooldownFrames = Math.max(0, breedCooldownFrames - agingSpeed);
        }

        // Tick buffs
        for (Iterator<Buff> it = buffs.iterator(); it.hasNext(); ) {
            Buff b = it.next();
            b.tick();
            if (b.isExpired()) it.remove();
        }

        // decrease stats over time (with buff multipliers + meta drain)
        if (animFrame % 300 == 0) {
            double hungerMult = getBuffMultiplier(Buff.Type.HUNGER_DRAIN);
            hunger = Math.max(0, hunger - (int)(2 * drainMultiplier * hungerMult));
            double happinessMult = getBuffMultiplier(Buff.Type.HAPPINESS_DRAIN);
            happiness = Math.max(0, happiness - (int)(1 * drainMultiplier * happinessMult));
        }

        // User-initiated action ticks: every ~3 seconds, fill stats + award coins
        if (userAction && animFrame % ACTION_TICK_FRAMES == 0) {
            switch (state) {
                case EATING:
                    hunger = Math.min(maxHunger, hunger + actionGain);
                    happiness = Math.min(maxHappiness, happiness + Math.max(1, actionGain / 3));
                    pendingCoins += 2;
                    if (hunger >= maxHunger) { state = State.IDLE; stateTimer = 60; userAction = false; }
                    break;
                case HAPPY:
                    happiness = Math.min(maxHappiness, happiness + actionGain);
                    energy = Math.max(0, energy - 3);
                    pendingCoins += 3;
                    if (happiness >= maxHappiness) { state = State.IDLE; stateTimer = 60; userAction = false; }
                    break;
                case RUNNING_WHEEL:
                    happiness = Math.min(maxHappiness, happiness + actionGain);
                    energy = Math.max(0, energy - 5);
                    pendingCoins += 1;
                    if (happiness >= maxHappiness || energy < 10) { state = State.IDLE; stateTimer = 60; userAction = false; }
                    break;
                case SLEEPING:
                    energy = Math.min(maxEnergy, energy + actionGain);
                    if (energy >= maxEnergy) { state = State.IDLE; stateTimer = 60; userAction = false; }
                    break;
            }
        }

        // Natural (AI-chosen) sleeping: recover energy
        if (!userAction && state == State.SLEEPING && animFrame % 120 == 0) {
            energy = Math.min(maxEnergy, energy + 3);
        } else if (state != State.SLEEPING && !userAction && animFrame % 200 == 0) {
            double energyMult = getBuffMultiplier(Buff.Type.ENERGY_DRAIN);
            energy = Math.max(0, energy - (int)(1 * drainMultiplier * energyMult));
        }

        // Natural (AI-chosen) running wheel: drain energy, boost happiness
        if (!userAction && state == State.RUNNING_WHEEL && animFrame % 100 == 0) {
            double energyMult = getBuffMultiplier(Buff.Type.ENERGY_DRAIN);
            energy = Math.max(0, energy - (int)(2 * drainMultiplier * energyMult));
            happiness = Math.min(maxHappiness, happiness + 2);
        }
        if (state == State.RUNNING_WHEEL && energy < 10) {
            state = State.IDLE;
            stateTimer = 60;
            userAction = false;
        }

        // Die if any stat reaches 0
        if (hunger <= 0 || happiness <= 0 || energy <= 0) {
            dead = true;
            return;
        }

        if (stateTimer <= 0 && state != State.EATING && state != State.HAPPY) {
            chooseNextState();
        }

        // movement is handled by HamsterWindow
    }

    private void chooseNextState() {
        userAction = false;
        if (energy < 20) {
            state = State.SLEEPING;
            stateTimer = 300 + random.nextInt(200);
            return;
        }

        int r = random.nextInt(100);
        if (r < 40) {
            state = State.WALKING;
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 2.0;
            moveX = Math.cos(angle) * speed;
            moveY = Math.sin(angle) * speed;
            direction = moveX >= 0 ? 1 : -1;
            stateTimer = 100 + random.nextInt(200);
        } else if (r < 55 && energy > 30) {
            state = State.RUNNING_WHEEL;
            stateTimer = 150 + random.nextInt(150);
        } else {
            state = State.IDLE;
            stateTimer = 60 + random.nextInt(120);
        }
    }

    private static final int ACTION_TICK_FRAMES = 90; // ~3 seconds between each tick

    public boolean feed() {
        if (dead) return false;
        state = State.EATING;
        stateTimer = Integer.MAX_VALUE;
        userAction = true;
        return true;
    }

    public boolean play() {
        if (dead) return false;
        state = State.HAPPY;
        stateTimer = Integer.MAX_VALUE;
        userAction = true;
        return true;
    }

    public boolean runWheel() {
        if (dead) return false;
        state = State.RUNNING_WHEEL;
        stateTimer = Integer.MAX_VALUE;
        userAction = true;
        return true;
    }

    public boolean sleep() {
        if (dead) return false;
        state = State.SLEEPING;
        stateTimer = Integer.MAX_VALUE;
        userAction = true;
        return true;
    }

    public int collectPendingCoins() {
        int coins = pendingCoins;
        pendingCoins = 0;
        return coins;
    }

    public void wake() {
        if (state == State.SLEEPING || state == State.RUNNING_WHEEL) {
            state = State.IDLE;
            stateTimer = 60;
        }
    }

    public boolean shouldPoop() {
        if (dead) return false;
        if (poopTimer < 200) return false;
        // Higher hunger = more likely to poop (well-fed hamster poops more)
        int chance = 2 + hunger / 20; // 2~7 per frame out of 1000
        if (random.nextInt(1000) < chance) {
            poopTimer = 0;
            return true;
        }
        return false;
    }

    public void applyPoopPenalty(int poopCount) {
        if (dead) return;
        if (poopCount > 0 && animFrame % 200 == 0) {
            happiness = Math.max(0, happiness - poopCount);
        }
    }

    // getters
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getDirection() { return direction; }
    public double getMoveX() { return moveX; }
    public double getMoveY() { return moveY; }
    public void bounceX() { moveX = -moveX; direction = moveX >= 0 ? 1 : -1; }
    public void bounceY() { moveY = -moveY; }
    public State getState() { return state; }
    public int getAnimFrame() { return animFrame; }
    public int getHunger() { return hunger; }
    public int getHappiness() { return happiness; }
    public int getEnergy() { return energy; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // new getters/setters for color, age, death
    public HamsterColor getColor() { return color; }
    public void setColor(HamsterColor color) { this.color = color; }
    public long getAgeFrames() { return ageFrames; }
    public void setAgeFrames(long ageFrames) { this.ageFrames = ageFrames; }
    public int getAgeDays() { return (int)(ageFrames / FRAMES_PER_DAY); }
    public int getLifespanFrames() { return lifespanFrames; }
    public boolean isDead() { return dead; }
    public void setHunger(int hunger) { this.hunger = Math.max(0, Math.min(maxHunger, hunger)); }
    public void setHappiness(int happiness) { this.happiness = Math.max(0, Math.min(maxHappiness, happiness)); }
    public void setEnergy(int energy) { this.energy = Math.max(0, Math.min(maxEnergy, energy)); }
    public int getMaxHunger() { return maxHunger; }
    public int getMaxHappiness() { return maxHappiness; }
    public int getMaxEnergy() { return maxEnergy; }
    public void setMaxHunger(int v) { this.maxHunger = Math.min(MAX_STAT_CAP, v); }
    public void setMaxHappiness(int v) { this.maxHappiness = Math.min(MAX_STAT_CAP, v); }
    public void setMaxEnergy(int v) { this.maxEnergy = Math.min(MAX_STAT_CAP, v); }
    public void setPoopTimer(int poopTimer) { this.poopTimer = poopTimer; }
    public int getPoopTimer() { return poopTimer; }
    public void setAgingSpeed(int v) { this.agingSpeed = v; }
    public void setActionGain(int v) { this.actionGain = v; }
    public void setDrainMultiplier(int v) { this.drainMultiplier = v; }

    // Buff methods
    public double getBuffMultiplier(Buff.Type type) {
        double mult = 1.0;
        for (Buff b : buffs) {
            if (b.getType() == type) {
                mult *= b.getMultiplier();
            }
        }
        return mult;
    }

    public double getCoinMultiplier() {
        return getBuffMultiplier(Buff.Type.COIN_BONUS);
    }

    public void addBuff(Buff buff) {
        buffs.add(buff);
    }

    public List<Buff> getBuffs() {
        return buffs;
    }

    // Generation / Legacy
    public int getGeneration() { return generation; }
    public void setGeneration(int generation) { this.generation = generation; }

    public int getLegacyHungerBonus() { return legacyHungerBonus; }
    public void setLegacyHungerBonus(int v) { this.legacyHungerBonus = v; }

    public int getLegacyHappinessBonus() { return legacyHappinessBonus; }
    public void setLegacyHappinessBonus(int v) { this.legacyHappinessBonus = v; }

    public int getLegacyEnergyBonus() { return legacyEnergyBonus; }
    public void setLegacyEnergyBonus(int v) { this.legacyEnergyBonus = v; }

    public int getLegacyLifespanBonus() { return legacyLifespanBonus; }
    public void setLegacyLifespanBonus(int v) { this.legacyLifespanBonus = v; }

    public int getLegacyMaxStatBonus() { return legacyMaxStatBonus; }
    public void setLegacyMaxStatBonus(int v) { this.legacyMaxStatBonus = v; }

    // Breeding
    public boolean canBreed(int breedAgeFrames) {
        return !dead && ageFrames >= breedAgeFrames && breedCooldownFrames <= 0
                && hunger >= 50 && happiness >= 50 && energy >= 50;
    }
    public void startBreedCooldown() { breedCooldownFrames = 2 * FRAMES_PER_DAY; }
    public int getBreedCooldownFrames() { return breedCooldownFrames; }
    public void setBreedCooldownFrames(int v) { this.breedCooldownFrames = v; }

    public void applyLegacyBonuses() {
        maxHunger = Math.min(MAX_STAT_CAP, 100 + legacyMaxStatBonus);
        maxHappiness = Math.min(MAX_STAT_CAP, 100 + legacyMaxStatBonus);
        maxEnergy = Math.min(MAX_STAT_CAP, 100 + legacyMaxStatBonus);
        hunger = Math.min(maxHunger, hunger + legacyHungerBonus);
        happiness = Math.min(maxHappiness, happiness + legacyHappinessBonus);
        energy = Math.min(maxEnergy, energy + legacyEnergyBonus);
        lifespanFrames += legacyLifespanBonus;
    }

    /**
     * Compute legacy bonuses for the next generation.
     * @return int array: [nextGen, hungerBonus, happinessBonus, energyBonus, lifespanBonus, maxStatBonus]
     */
    public int[] computeLegacy() {
        int avgStat = (hunger + happiness + energy) / 3;
        int nextHungerBonus = legacyHungerBonus;
        int nextHappinessBonus = legacyHappinessBonus;
        int nextEnergyBonus = legacyEnergyBonus;
        int nextLifespanBonus = legacyLifespanBonus;
        int nextMaxStatBonus = legacyMaxStatBonus;
        if (avgStat > 30) {
            nextHungerBonus = Math.min(25, legacyHungerBonus + 5);
            nextHappinessBonus = Math.min(25, legacyHappinessBonus + 5);
            nextEnergyBonus = Math.min(25, legacyEnergyBonus + 5);
            nextLifespanBonus = Math.min(5 * FRAMES_PER_DAY, legacyLifespanBonus + FRAMES_PER_DAY);
            nextMaxStatBonus = Math.min(MAX_STAT_CAP - 100, legacyMaxStatBonus + 5);
        }
        return new int[] { generation + 1, nextHungerBonus, nextHappinessBonus, nextEnergyBonus, nextLifespanBonus, nextMaxStatBonus };
    }

    public static int upgradeCost(int currentMax) {
        return currentMax - 50;
    }
}
