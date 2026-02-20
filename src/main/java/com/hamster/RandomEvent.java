package com.hamster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomEvent {
    private final String title;
    private final String description;
    private final String choiceA;
    private final String choiceB;
    private final ChoiceAction actionA;
    private final ChoiceAction actionB;

    @FunctionalInterface
    public interface ChoiceAction {
        String apply(Hamster h, Main main);
    }

    public RandomEvent(String title, String description, String choiceA, String choiceB,
                       ChoiceAction actionA, ChoiceAction actionB) {
        this.title = title;
        this.description = description;
        this.choiceA = choiceA;
        this.choiceB = choiceB;
        this.actionA = actionA;
        this.actionB = actionB;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getChoiceA() { return choiceA; }
    public String getChoiceB() { return choiceB; }

    public String applyChoiceA(Hamster h, Main main) {
        return actionA.apply(h, main);
    }

    public String applyChoiceB(Hamster h, Main main) {
        return actionB.apply(h, main);
    }

    public static List<RandomEvent> allEvents() {
        List<RandomEvent> events = new ArrayList<>();
        Random rng = new Random();

        // 1. Food found
        events.add(new RandomEvent(
                "\uD83C\uDF3D \uBA39\uC774\uB97C \uBC1C\uACAC\uD588\uC5B4\uC694!",
                "\uAE38\uC5D0\uC11C \uB9DB\uC788\uB294 \uBA39\uC774\uB97C \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4.\n\uC5B4\uB5BB\uAC8C \uD560\uAE4C\uC694?",
                "\uBA39\uB294\uB2E4",
                "\uC800\uC7A5\uD55C\uB2E4",
                (h, main) -> {
                    h.setHunger(Math.min(h.getMaxHunger(), h.getHunger() + 30));
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 10));
                    return "\uBC30\uACE0\uD514+30, \uD589\uBCF5+10";
                },
                (h, main) -> {
                    main.addMoney(50, null);
                    return "\uCF54\uC778+50";
                }
        ));

        // 2. Strange sound
        events.add(new RandomEvent(
                "\uD83D\uDD0A \uC774\uC0C1\uD55C \uC18C\uB9AC\uAC00 \uB4E4\uB824\uC694!",
                "\uC5B4\uB514\uC120\uAC00 \uC774\uC0C1\uD55C \uC18C\uB9AC\uAC00 \uB4E4\uB824\uC635\uB2C8\uB2E4.\n\uC870\uC0AC\uD574\uBCFC\uAE4C\uC694?",
                "\uC870\uC0AC\uD55C\uB2E4",
                "\uBB34\uC2DC\uD55C\uB2E4",
                (h, main) -> {
                    if (rng.nextBoolean()) {
                        h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 20));
                        return "\uD589\uBCF5+20 (\uC7AC\uBBF8\uC788\uB294 \uAC83\uC744 \uBC1C\uACAC!)";
                    } else {
                        h.setEnergy(Math.max(0, h.getEnergy() - 15));
                        return "\uCCB4\uB825-15 (\uB180\uB77C\uBC84\uB838\uC5B4\uC694!)";
                    }
                },
                (h, main) -> {
                    return "\uD6A8\uACFC \uC5C6\uC74C";
                }
        ));

        // 3. Sunny day
        events.add(new RandomEvent(
                "\u2600\uFE0F \uB9D1\uC740 \uB0A0\uC774\uC5D0\uC694!",
                "\uB0A0\uC528\uAC00 \uC815\uB9D0 \uC88B\uC2B5\uB2C8\uB2E4.\n\uBB50 \uD560\uAE4C\uC694?",
                "\uC0B0\uCC45\uD55C\uB2E4",
                "\uC26C\uB294\uB2E4",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 15));
                    h.setEnergy(Math.max(0, h.getEnergy() - 10));
                    // Happiness drain down for 3 minutes (5400 frames)
                    h.addBuff(new Buff(Buff.Type.HAPPINESS_DRAIN, 0.5, 5400, "\uD589\uBCF5\uAC10\uC18C\u2193"));
                    return "\uD589\uBCF5+15, \uCCB4\uB825-10, \uD589\uBCF5\uAC10\uC18C\u2193 3\uBD84";
                },
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 20));
                    // Energy drain down for 3 minutes
                    h.addBuff(new Buff(Buff.Type.ENERGY_DRAIN, 0.5, 5400, "\uCCB4\uB825\uAC10\uC18C\u2193"));
                    return "\uCCB4\uB825+20, \uCCB4\uB825\uAC10\uC18C\u2193 3\uBD84";
                }
        ));

        // 4. Coin found
        events.add(new RandomEvent(
                "\uD83E\uDE99 \uB3D9\uC804\uC744 \uBC1C\uACAC\uD588\uC5B4\uC694!",
                "\uBC18\uC9DD\uC774\uB294 \uB3D9\uC804\uC744 \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4.\n\uC5B4\uB5BB\uAC8C \uD560\uAE4C\uC694?",
                "\uC90D\uB294\uB2E4",
                "\uC18C\uC6D0\uC744 \uBE4C\uB2E4",
                (h, main) -> {
                    main.addMoney(30, null);
                    return "\uCF54\uC778+30";
                },
                (h, main) -> {
                    // Coin bonus up for 2 minutes (3600 frames)
                    h.addBuff(new Buff(Buff.Type.COIN_BONUS, 2.0, 3600, "\uCF54\uC778\uD68D\uB4DD\u2191"));
                    return "\uCF54\uC778\uD68D\uB4DD\u2191 2\uBD84";
                }
        ));

        // 5. Rainy day
        events.add(new RandomEvent(
                "\uD83C\uDF27\uFE0F \uBE44 \uC624\uB294 \uB0A0\uC774\uC5D0\uC694!",
                "\uBE44\uAC00 \uC8FC\uB985\uC8FC\uB989 \uB0B4\uB9AC\uACE0 \uC788\uC2B5\uB2C8\uB2E4.\n\uBB50 \uD560\uAE4C\uC694?",
                "\uBE44\uC5D0\uC11C \uB17C\uB2E4",
                "\uB4E4\uC5B4\uAC04\uB2E4",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 25));
                    // Hunger drain up for 3 minutes (debuff)
                    h.addBuff(new Buff(Buff.Type.HUNGER_DRAIN, 1.5, 5400, "\uBC30\uACE0\uD514\uAC10\uC18C\u2191"));
                    return "\uD589\uBCF5+25, \uBC30\uACE0\uD514\uAC10\uC18C\u2191 3\uBD84";
                },
                (h, main) -> {
                    // Hunger drain down for 3 minutes (buff)
                    h.addBuff(new Buff(Buff.Type.HUNGER_DRAIN, 0.5, 5400, "\uBC30\uACE0\uD514\uAC10\uC18C\u2193"));
                    return "\uBC30\uACE0\uD514\uAC10\uC18C\u2193 3\uBD84";
                }
        ));

        return events;
    }

    private static List<RandomEvent> cachedEvents;

    public static RandomEvent pickRandom(Random rng) {
        if (cachedEvents == null) {
            cachedEvents = allEvents();
        }
        return cachedEvents.get(rng.nextInt(cachedEvents.size()));
    }
}
