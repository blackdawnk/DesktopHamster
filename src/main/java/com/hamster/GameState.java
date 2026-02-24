package com.hamster;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    int money;
    long totalFrames;
    int hamstersRaised;
    int hamsterPurchaseCount;
    List<HamsterData> hamsters;
    List<PoopData> poops;
    FoodInventory foodInventory;

    public static class HamsterData {
        String name;
        HamsterColor color;
        int hunger, happiness, energy, poopTimer;
        long ageFrames;
        int lifespanFrames;
        int windowX, windowY;
        // Roguelike fields
        int generation;
        int legacyHungerBonus, legacyHappinessBonus, legacyEnergyBonus, legacyLifespanBonus;
        int legacyMaxStatBonus;
        int maxHunger, maxHappiness, maxEnergy;
        int breedCooldownFrames;
        List<BuffData> buffs = new ArrayList<>();
        // 2.0 fields
        String personality;
        List<String> equippedAccessories = new ArrayList<>();
        List<String> ownedAccessories = new ArrayList<>();
    }

    public static class BuffData {
        Buff.Type type;
        double multiplier;
        int remainingFrames;
        String description;
    }

    public static class PoopData {
        int screenX, screenY;
    }

    public static GameState capture(int money, long totalFrames,
                                    List<Hamster> hamsters, List<HamsterWindow> windows,
                                    List<PoopWindow> poopWindows,
                                    int hamstersRaised,
                                    FoodInventory foodInventory,
                                    int hamsterPurchaseCount) {
        GameState state = new GameState();
        state.money = money;
        state.totalFrames = totalFrames;
        state.hamstersRaised = hamstersRaised;
        state.hamsterPurchaseCount = hamsterPurchaseCount;
        state.foodInventory = foodInventory;

        state.hamsters = new ArrayList<>();
        for (int i = 0; i < hamsters.size(); i++) {
            Hamster h = hamsters.get(i);
            HamsterWindow w = windows.get(i);
            HamsterData hd = new HamsterData();
            hd.name = h.getName();
            hd.color = h.getColor();
            hd.hunger = h.getHunger();
            hd.happiness = h.getHappiness();
            hd.energy = h.getEnergy();
            hd.poopTimer = h.getPoopTimer();
            hd.ageFrames = h.getAgeFrames();
            hd.lifespanFrames = h.getLifespanFrames();
            hd.windowX = w.getLocation().x;
            hd.windowY = w.getLocation().y;
            // Roguelike data
            hd.generation = h.getGeneration();
            hd.legacyHungerBonus = h.getLegacyHungerBonus();
            hd.legacyHappinessBonus = h.getLegacyHappinessBonus();
            hd.legacyEnergyBonus = h.getLegacyEnergyBonus();
            hd.legacyLifespanBonus = h.getLegacyLifespanBonus();
            hd.legacyMaxStatBonus = h.getLegacyMaxStatBonus();
            hd.maxHunger = h.getMaxHunger();
            hd.maxHappiness = h.getMaxHappiness();
            hd.maxEnergy = h.getMaxEnergy();
            hd.breedCooldownFrames = h.getBreedCooldownFrames();
            for (Buff b : h.getBuffs()) {
                BuffData bd = new BuffData();
                bd.type = b.getType();
                bd.multiplier = b.getMultiplier();
                bd.remainingFrames = b.getRemainingFrames();
                bd.description = b.getDescription();
                hd.buffs.add(bd);
            }
            // 2.0 fields
            hd.personality = h.getPersonality() != null ? h.getPersonality().name() : "CHEERFUL";
            for (Accessory acc : h.getEquippedAccessories()) {
                hd.equippedAccessories.add(acc.name());
            }
            for (String owned : h.getOwnedAccessories()) {
                hd.ownedAccessories.add(owned);
            }
            state.hamsters.add(hd);
        }

        state.poops = new ArrayList<>();
        for (PoopWindow pw : poopWindows) {
            PoopData pd = new PoopData();
            pd.screenX = pw.getPoop().getScreenX();
            pd.screenY = pw.getPoop().getScreenY();
            state.poops.add(pd);
        }

        return state;
    }
}
