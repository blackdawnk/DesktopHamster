package com.hamster.model;
import com.hamster.ui.HamsterWindow;
import com.hamster.ui.PoopWindow;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public int money;
    public long totalFrames;
    public int hamstersRaised;
    public int qualifiedHamsters;
    public int hamsterPurchaseCount;
    public List<HamsterData> hamsters;
    public List<PoopData> poops;
    public FoodInventory foodInventory;

    public static class HamsterData {
        public String name;
        public HamsterColor color;
        public int hunger, happiness, energy, poopTimer;
        public long ageFrames;
        public int lifespanFrames;
        public int windowX, windowY;
        // Roguelike fields
        public int generation;
        public int legacyHungerBonus, legacyHappinessBonus, legacyEnergyBonus, legacyLifespanBonus;
        public int legacyMaxStatBonus;
        public int maxHunger, maxHappiness, maxEnergy;
        public int breedCooldownFrames;
        public List<BuffData> buffs = new ArrayList<>();
        // 2.0 fields
        public String personality;
        public List<String> equippedAccessories = new ArrayList<>();
        public List<String> ownedAccessories = new ArrayList<>();
    }

    public static class BuffData {
        public Buff.Type type;
        public double multiplier;
        public int remainingFrames;
        public String description;
    }

    public static class PoopData {
        public int screenX, screenY;
    }

    public static GameState capture(int money, long totalFrames,
                                    List<Hamster> hamsters, List<HamsterWindow> windows,
                                    List<PoopWindow> poopWindows,
                                    int hamstersRaised, int qualifiedHamsters,
                                    FoodInventory foodInventory,
                                    int hamsterPurchaseCount) {
        GameState state = new GameState();
        state.money = money;
        state.totalFrames = totalFrames;
        state.hamstersRaised = hamstersRaised;
        state.qualifiedHamsters = qualifiedHamsters;
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
