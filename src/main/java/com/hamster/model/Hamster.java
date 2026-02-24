package com.hamster.model;
import com.hamster.system.MetaProgress;
import com.hamster.ui.HamsterWindow;

import java.util.*;



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

    private int hunger = 80;    // 0~100
    private int happiness = 80; // 0~100
    private int energy = 80;    // 0~100

    private int poopTimer = 0;
    private boolean userAction = false; // true when action was user-initiated
    private int pendingCoins = 0;       // coins accumulated from ongoing actions

    // Meta-configurable values (set by Main from MetaProgress)
    private double agingSpeed = 2.0;   // frames aged per update (default: 2x fast)
    private double ageAccumulator = 0; // fractional aging accumulator
    private int actionGain = 5;        // stat gain per action tick
    private double drainMultiplier = 1.0; // passive stat drain multiplier
    private int drainInterval = 300;   // frames between stat drain ticks (~10 seconds)
    private int coinBonus = 0;         // extra coins per action
    private double poopChanceMultiplier = 1.0;  // poop probability multiplier
    private double poopPenaltyMultiplier = 1.0; // poop happiness penalty multiplier
    private double buffDurationMultiplier = 1.0; // buff duration multiplier

    private HamsterColor color;
    private long ageFrames = 0;
    private int lifespanFrames;
    private boolean dead = false;

    // 2.0 fields
    private Personality personality;
    private FoodItem pendingFood = null;
    private final List<Accessory> equippedAccessories = new ArrayList<>();
    private final Set<String> ownedAccessories = new HashSet<>();
    private int interactionCooldownFrames = 0;

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

    private boolean frozen = false;

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
        this.personality = Personality.random(random);
        // Random lifespan: 20~35 days
        this.lifespanFrames = (20 + random.nextInt(16)) * FRAMES_PER_DAY;
    }

    public Hamster(int screenWidth, int groundY, HamsterColor color, int lifespanFrames) {
        this.screenWidth = screenWidth;
        this.groundY = groundY;
        this.x = screenWidth / 2;
        this.y = groundY;
        this.color = color;
        this.personality = Personality.random(random);
        this.lifespanFrames = lifespanFrames;
    }

    public void update() {
        if (dead || frozen) return;

        ageAccumulator += agingSpeed;
        int ageGain = (int) ageAccumulator;
        ageAccumulator -= ageGain;
        ageFrames += ageGain;
        if (ageFrames >= lifespanFrames) {
            dead = true;
            return;
        }

        animFrame++;
        stateTimer--;
        poopTimer++;

        // Tick breed cooldown
        if (breedCooldownFrames > 0) {
            breedCooldownFrames = Math.max(0, breedCooldownFrames - Math.max(1, (int) agingSpeed));
        }

        // Tick interaction cooldown
        if (interactionCooldownFrames > 0) {
            interactionCooldownFrames--;
        }

        // Tick buffs
        for (Iterator<Buff> it = buffs.iterator(); it.hasNext(); ) {
            Buff b = it.next();
            b.tick();
            if (b.isExpired()) it.remove();
        }

        // decrease stats over time (with buff multipliers + meta drain + personality + TimeOfDay)
        if (animFrame % drainInterval == 0) {
            TimeOfDay tod = TimeOfDay.getCurrentPeriod();
            double pHunger = personality != null ? personality.getHungerDrainMult() : 1.0;
            double pHappiness = personality != null ? personality.getHappinessDrainMult() : 1.0;
            double hungerMult = getBuffMultiplier(Buff.Type.HUNGER_DRAIN);
            hunger = Math.max(0, hunger - Math.max(1, (int)(1 * drainMultiplier * hungerMult * pHunger * tod.getHungerDrainMult())));
            double happinessMult = getBuffMultiplier(Buff.Type.HAPPINESS_DRAIN);
            happiness = Math.max(0, happiness - Math.max(1, (int)(1 * drainMultiplier * happinessMult * pHappiness * tod.getHappinessDrainMult())));
        }

        // User-initiated action: one-shot, transition to walking when timer expires

        // Natural (AI-chosen) sleeping: recover energy
        if (!userAction && state == State.SLEEPING && animFrame % 200 == 0) {
            TimeOfDay todSleep = TimeOfDay.getCurrentPeriod();
            int baseRecover = (int)(3 * todSleep.getEnergyRecoveryMult());
            double pSleep = personality != null ? personality.getSleepMult() : 1.0;
            energy = Math.min(maxEnergy, energy + Math.max(1, (int)(baseRecover * pSleep)));
        } else if (state != State.SLEEPING && !userAction && animFrame % 400 == 0) {
            double pEnergy = personality != null ? personality.getEnergyDrainMult() : 1.0;
            TimeOfDay todE = TimeOfDay.getCurrentPeriod();
            double energyMult = getBuffMultiplier(Buff.Type.ENERGY_DRAIN);
            energy = Math.max(0, energy - Math.max(1, (int)(1 * drainMultiplier * energyMult * pEnergy * todE.getEnergyDrainMult())));
        }

        // Natural (AI-chosen) running wheel: drain energy, boost happiness
        if (!userAction && state == State.RUNNING_WHEEL && animFrame % 200 == 0) {
            double energyMult = getBuffMultiplier(Buff.Type.ENERGY_DRAIN);
            energy = Math.max(0, energy - Math.max(1, (int)(1 * drainMultiplier * energyMult)));
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

        if (stateTimer <= 0) {
            if (userAction) {
                // Apply stat/coin rewards after action animation completes
                double pCoin = personality != null ? personality.getCoinMult() : 1.0;
                TimeOfDay todCoin = TimeOfDay.getCurrentPeriod();
                int todBonus = todCoin.getCoinBonus();
                switch (state) {
                    case EATING:
                        if (pendingFood != null) {
                            double pFeed = personality != null ? personality.getFeedMult() : 1.0;
                            hunger = Math.min(maxHunger, hunger + (int)(pendingFood.getHungerGain() * pFeed));
                            happiness = Math.min(maxHappiness, happiness + pendingFood.getHappinessGain());
                            energy = Math.min(maxEnergy, Math.max(0, energy + pendingFood.getEnergyGain()));
                            pendingFood = null;
                        } else {
                            double pFeed = personality != null ? personality.getFeedMult() : 1.0;
                            hunger = Math.min(maxHunger, hunger + (int)(actionGain * pFeed));
                            happiness = Math.min(maxHappiness, happiness + Math.max(1, actionGain / 3));
                        }
                        pendingCoins += (int)((2 + coinBonus + todBonus) * pCoin);
                        break;
                    case HAPPY:
                        double pPlay = personality != null ? personality.getPlayMult() : 1.0;
                        happiness = Math.min(maxHappiness, happiness + (int)(actionGain * pPlay));
                        energy = Math.max(0, energy - 3);
                        pendingCoins += (int)((3 + coinBonus + todBonus) * pCoin);
                        break;
                    case RUNNING_WHEEL:
                        happiness = Math.min(maxHappiness, happiness + actionGain);
                        energy = Math.max(0, energy - 5);
                        pendingCoins += (int)((1 + coinBonus + todBonus) * pCoin);
                        break;
                    case SLEEPING:
                        double pSleep = personality != null ? personality.getSleepMult() : 1.0;
                        energy = Math.min(maxEnergy, energy + (int)(actionGain * pSleep));
                        break;
                }
                // Then start walking
                userAction = false;
                state = State.WALKING;
                double angle = random.nextDouble() * Math.PI * 2;
                double speed = 2.0;
                moveX = Math.cos(angle) * speed;
                moveY = Math.sin(angle) * speed;
                direction = moveX >= 0 ? 1 : -1;
                stateTimer = 100 + random.nextInt(200);
            } else {
                chooseNextState();
            }
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

        // Night/late-night: increased sleep chance
        TimeOfDay tod = TimeOfDay.getCurrentPeriod();
        double sleepBonus = tod.getSleepChanceBonus();
        if (sleepBonus > 0 && random.nextDouble() < sleepBonus && energy < 60) {
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

    private static final int USER_ACTION_ANIM_FRAMES = 90; // ~3 seconds animation

    public boolean feed() {
        if (dead) return false;
        state = State.EATING;
        stateTimer = USER_ACTION_ANIM_FRAMES;
        userAction = true;
        pendingFood = null;
        return true;
    }

    public boolean feed(FoodItem food) {
        if (dead) return false;
        state = State.EATING;
        stateTimer = USER_ACTION_ANIM_FRAMES;
        userAction = true;
        pendingFood = food;
        return true;
    }

    public boolean play() {
        if (dead) return false;
        state = State.HAPPY;
        stateTimer = USER_ACTION_ANIM_FRAMES;
        userAction = true;
        return true;
    }

    public boolean runWheel() {
        if (dead) return false;
        state = State.RUNNING_WHEEL;
        stateTimer = USER_ACTION_ANIM_FRAMES;
        userAction = true;
        return true;
    }

    public boolean sleep() {
        if (dead) return false;
        state = State.SLEEPING;
        stateTimer = USER_ACTION_ANIM_FRAMES;
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
        int chance = (int)((2 + hunger / 20) * poopChanceMultiplier); // 2~7 per frame out of 1000
        if (random.nextInt(1000) < chance) {
            poopTimer = 0;
            return true;
        }
        return false;
    }

    public void applyPoopPenalty(int poopCount) {
        if (dead) return;
        if (poopCount > 0 && animFrame % 200 == 0) {
            happiness = Math.max(0, happiness - Math.max(1, (int)(poopCount * poopPenaltyMultiplier)));
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
    public void kill() { this.dead = true; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }
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
    public void setAgingSpeed(double v) { this.agingSpeed = v; }
    public void setActionGain(int v) { this.actionGain = v; }
    public void setDrainMultiplier(double v) { this.drainMultiplier = v; }
    public void setDrainInterval(int v) { this.drainInterval = v; }
    public void setCoinBonus(int v) { this.coinBonus = v; }
    public void setPoopChanceMultiplier(double v) { this.poopChanceMultiplier = v; }
    public void setPoopPenaltyMultiplier(double v) { this.poopPenaltyMultiplier = v; }
    public void setBuffDurationMultiplier(double v) { this.buffDurationMultiplier = v; }

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
        double mult = getBuffMultiplier(Buff.Type.COIN_BONUS);
        for (Accessory acc : equippedAccessories) {
            mult += acc.getCoinBonus();
        }
        return mult;
    }

    public void addBuff(Buff buff) {
        if (buffDurationMultiplier != 1.0) {
            buff = new Buff(buff.getType(), buff.getMultiplier(),
                    (int)(buff.getRemainingFrames() * buffDurationMultiplier), buff.getDescription());
        }
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

    // 2.0 getters/setters
    public Personality getPersonality() { return personality; }
    public void setPersonality(Personality p) { this.personality = p; }
    public FoodItem getPendingFood() { return pendingFood; }
    public void setPendingFood(FoodItem food) { this.pendingFood = food; }

    public List<Accessory> getEquippedAccessories() { return equippedAccessories; }
    public Set<String> getOwnedAccessories() { return ownedAccessories; }

    public void equipAccessory(Accessory acc) {
        // Remove existing in same slot
        Iterator<Accessory> it = equippedAccessories.iterator();
        while (it.hasNext()) {
            if (it.next().getSlot() == acc.getSlot()) {
                it.remove();
            }
        }
        equippedAccessories.add(acc);
    }

    public void unequipAccessory(Accessory acc) {
        equippedAccessories.remove(acc);
    }

    public int getInteractionCooldownFrames() { return interactionCooldownFrames; }
    public void setInteractionCooldownFrames(int v) { this.interactionCooldownFrames = v; }
    public boolean canInteract() { return !dead && interactionCooldownFrames <= 0; }
    public void startInteractionCooldown() { this.interactionCooldownFrames = 2700; }
}
