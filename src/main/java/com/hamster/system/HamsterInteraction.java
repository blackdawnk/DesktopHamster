package com.hamster.system;
import com.hamster.model.Hamster;
import com.hamster.ui.HamsterWindow;

import java.awt.*;
import java.util.Random;

public class HamsterInteraction {

    public enum Type {
        PLAY_TOGETHER("\uAC19\uC774 \uB180\uAE30", 0, 15, -5, 30),
        SHARE_FOOD("\uBA39\uC774 \uB098\uB204\uAE30", 10, 5, 0, 20),
        FIGHT("\uC2F8\uC6C0", 0, -7, -3, 5),
        GROOM("\uADF8\uB8E8\uBC0D", 0, 12, 7, 25),
        NAP_TOGETHER("\uAC19\uC774 \uB0AE\uC7A0", 0, 5, 15, 10),
        CHASE("\uCCAB\uBC14\uD034", 0, 10, -10, 10);

        private final String displayName;
        private final int hungerEffect;
        private final int happinessEffect;
        private final int energyEffect;
        private final int weight;

        Type(String displayName, int hungerEffect, int happinessEffect, int energyEffect, int weight) {
            this.displayName = displayName;
            this.hungerEffect = hungerEffect;
            this.happinessEffect = happinessEffect;
            this.energyEffect = energyEffect;
            this.weight = weight;
        }

        public String getDisplayName() { return displayName; }
        public int getHungerEffect() { return hungerEffect; }
        public int getHappinessEffect() { return happinessEffect; }
        public int getEnergyEffect() { return energyEffect; }
    }

    private static final int CLOSE_DISTANCE = 150;

    public static boolean areClose(HamsterWindow a, HamsterWindow b) {
        Point pa = a.getHamsterScreenPosition();
        Point pb = b.getHamsterScreenPosition();
        int dx = pa.x - pb.x;
        int dy = pa.y - pb.y;
        return Math.sqrt(dx * dx + dy * dy) < CLOSE_DISTANCE;
    }

    public static String interact(Hamster a, Hamster b, Type type) {
        applyEffect(a, type);
        applyEffect(b, type);
        return a.getName() + "\uC640(\uACFC) " + b.getName() + "\uC774(\uAC00) "
                + type.getDisplayName() + "! (" + effectText(type) + ")";
    }

    private static void applyEffect(Hamster h, Type type) {
        if (type.hungerEffect != 0) {
            h.setHunger(Math.max(0, Math.min(h.getMaxHunger(), h.getHunger() + type.hungerEffect)));
        }
        if (type.happinessEffect != 0) {
            h.setHappiness(Math.max(0, Math.min(h.getMaxHappiness(), h.getHappiness() + type.happinessEffect)));
        }
        if (type.energyEffect != 0) {
            h.setEnergy(Math.max(0, Math.min(h.getMaxEnergy(), h.getEnergy() + type.energyEffect)));
        }
    }

    private static String effectText(Type type) {
        StringBuilder sb = new StringBuilder();
        if (type.hungerEffect > 0) sb.append("\uBC30\uACE0\uD514+").append(type.hungerEffect).append(" ");
        else if (type.hungerEffect < 0) sb.append("\uBC30\uACE0\uD514").append(type.hungerEffect).append(" ");
        if (type.happinessEffect > 0) sb.append("\uD589\uBCF5+").append(type.happinessEffect).append(" ");
        else if (type.happinessEffect < 0) sb.append("\uD589\uBCF5").append(type.happinessEffect).append(" ");
        if (type.energyEffect > 0) sb.append("\uCCB4\uB825+").append(type.energyEffect);
        else if (type.energyEffect < 0) sb.append("\uCCB4\uB825").append(type.energyEffect);
        return sb.toString().trim();
    }

    public static Type pickRandom(Random rng) {
        int totalWeight = 0;
        for (Type t : Type.values()) {
            totalWeight += t.weight;
        }
        int roll = rng.nextInt(totalWeight);
        int cumulative = 0;
        for (Type t : Type.values()) {
            cumulative += t.weight;
            if (roll < cumulative) return t;
        }
        return Type.PLAY_TOGETHER;
    }
}
