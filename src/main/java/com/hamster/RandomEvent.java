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

        // === ORIGINAL 5 EVENTS ===

        // 1. Food found
        events.add(new RandomEvent(
                "\uD83C\uDF3D \uBA39\uC774\uB97C \uBC1C\uACAC\uD588\uC5B4\uC694!",
                "\uAE38\uC5D0\uC11C \uB9DB\uC788\uB294 \uBA39\uC774\uB97C \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4.\n\uC5B4\uB5BB\uAC8C \uD560\uAE4C\uC694?",
                "\uBA39\uB294\uB2E4", "\uC800\uC7A5\uD55C\uB2E4",
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
                "\uC870\uC0AC\uD55C\uB2E4", "\uBB34\uC2DC\uD55C\uB2E4",
                (h, main) -> {
                    if (rng.nextBoolean()) {
                        h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 20));
                        return "\uD589\uBCF5+20 (\uC7AC\uBBF8\uC788\uB294 \uAC83\uC744 \uBC1C\uACAC!)";
                    } else {
                        h.setEnergy(Math.max(0, h.getEnergy() - 15));
                        return "\uCCB4\uB825-15 (\uB180\uB77C\uBC84\uB838\uC5B4\uC694!)";
                    }
                },
                (h, main) -> "\uD6A8\uACFC \uC5C6\uC74C"
        ));

        // 3. Sunny day
        events.add(new RandomEvent(
                "\u2600\uFE0F \uB9D1\uC740 \uB0A0\uC774\uC5D0\uC694!",
                "\uB0A0\uC528\uAC00 \uC815\uB9D0 \uC88B\uC2B5\uB2C8\uB2E4.\n\uBB50 \uD560\uAE4C\uC694?",
                "\uC0B0\uCC45\uD55C\uB2E4", "\uC26C\uB294\uB2E4",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 15));
                    h.setEnergy(Math.max(0, h.getEnergy() - 10));
                    h.addBuff(new Buff(Buff.Type.HAPPINESS_DRAIN, 0.5, 5400, "\uD589\uBCF5\uAC10\uC18C\u2193"));
                    return "\uD589\uBCF5+15, \uCCB4\uB825-10, \uD589\uBCF5\uAC10\uC18C\u2193 3\uBD84";
                },
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 20));
                    h.addBuff(new Buff(Buff.Type.ENERGY_DRAIN, 0.5, 5400, "\uCCB4\uB825\uAC10\uC18C\u2193"));
                    return "\uCCB4\uB825+20, \uCCB4\uB825\uAC10\uC18C\u2193 3\uBD84";
                }
        ));

        // 4. Coin found
        events.add(new RandomEvent(
                "\uD83E\uDE99 \uB3D9\uC804\uC744 \uBC1C\uACAC\uD588\uC5B4\uC694!",
                "\uBC18\uC9DD\uC774\uB294 \uB3D9\uC804\uC744 \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4.\n\uC5B4\uB5BB\uAC8C \uD560\uAE4C\uC694?",
                "\uC90D\uB294\uB2E4", "\uC18C\uC6D0\uC744 \uBE4C\uB2E4",
                (h, main) -> {
                    main.addMoney(30, null);
                    return "\uCF54\uC778+30";
                },
                (h, main) -> {
                    h.addBuff(new Buff(Buff.Type.COIN_BONUS, 2.0, 3600, "\uCF54\uC778\uD68D\uB4DD\u2191"));
                    return "\uCF54\uC778\uD68D\uB4DD\u2191 2\uBD84";
                }
        ));

        // 5. Rainy day
        events.add(new RandomEvent(
                "\uD83C\uDF27\uFE0F \uBE44 \uC624\uB294 \uB0A0\uC774\uC5D0\uC694!",
                "\uBE44\uAC00 \uC8FC\uB989\uC8FC\uB989 \uB0B4\uB9AC\uACE0 \uC788\uC2B5\uB2C8\uB2E4.\n\uBB50 \uD560\uAE4C\uC694?",
                "\uBE44\uC5D0\uC11C \uB17C\uB2E4", "\uB4E4\uC5B4\uAC04\uB2E4",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 25));
                    h.addBuff(new Buff(Buff.Type.HUNGER_DRAIN, 1.5, 5400, "\uBC30\uACE0\uD514\uAC10\uC18C\u2191"));
                    return "\uD589\uBCF5+25, \uBC30\uACE0\uD514\uAC10\uC18C\u2191 3\uBD84";
                },
                (h, main) -> {
                    h.addBuff(new Buff(Buff.Type.HUNGER_DRAIN, 0.5, 5400, "\uBC30\uACE0\uD514\uAC10\uC18C\u2193"));
                    return "\uBC30\uACE0\uD514\uAC10\uC18C\u2193 3\uBD84";
                }
        ));

        // === WEATHER EVENTS (+4) ===

        // 6. Snowy day
        events.add(new RandomEvent(
                "\u2744\uFE0F \uB208\uC774 \uC640\uC694!",
                "\uD558\uC580 \uB208\uC774 \uD3C5\uD3C5 \uB0B4\uB9AC\uACE0 \uC788\uC2B5\uB2C8\uB2E4.\n\uBB50 \uD560\uAE4C\uC694?",
                "\uB208\uC0AC\uB78C \uB9CC\uB4E4\uAE30", "\uB530\uB73B\uD558\uAC8C \uC26C\uAE30",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 20));
                    h.setEnergy(Math.max(0, h.getEnergy() - 15));
                    return "\uD589\uBCF5+20, \uCCB4\uB825-15";
                },
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 15));
                    h.setHunger(Math.max(0, h.getHunger() - 5));
                    return "\uCCB4\uB825+15, \uBC30\uACE0\uD514-5";
                }
        ));

        // 7. Windy day
        events.add(new RandomEvent(
                "\uD83C\uDF2C\uFE0F \uBC14\uB78C\uC774 \uBD88\uC5B4\uC694!",
                "\uAC15\uD55C \uBC14\uB78C\uC774 \uBD88\uACE0 \uC788\uC2B5\uB2C8\uB2E4.\n\uC5B4\uB5BB\uAC8C \uD560\uAE4C\uC694?",
                "\uBC14\uB78C\uC744 \uB9DE\uC73C\uBA70 \uB6F0\uAE30", "\uC228\uAE30",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 10));
                    h.setEnergy(Math.max(0, h.getEnergy() - 10));
                    main.addMoney(20, null);
                    return "\uD589\uBCF5+10, \uCCB4\uB825-10, \uCF54\uC778+20";
                },
                (h, main) -> {
                    h.addBuff(new Buff(Buff.Type.ENERGY_DRAIN, 0.7, 3600, "\uCCB4\uB825\uAC10\uC18C\u2193"));
                    return "\uCCB4\uB825\uAC10\uC18C\u2193 2\uBD84";
                }
        ));

        // 8. Foggy morning
        events.add(new RandomEvent(
                "\uD83C\uDF2B\uFE0F \uC548\uAC1C\uAC00 \uB07C\uC5C8\uC5B4\uC694!",
                "\uC9C0\uC5AD\uC5D0 \uC548\uAC1C\uAC00 \uC790\uC6B1\uD569\uB2C8\uB2E4.\n\uD0D0\uD5D8\uD574\uBCFC\uAE4C\uC694?",
                "\uD0D0\uD5D8\uD55C\uB2E4", "\uAE30\uB2E4\uB9B0\uB2E4",
                (h, main) -> {
                    if (rng.nextInt(3) == 0) {
                        main.addMoney(80, null);
                        return "\uCF54\uC778+80 (\uBCF4\uBB3C\uC744 \uBC1C\uACAC!)";
                    } else {
                        h.setHappiness(Math.max(0, h.getHappiness() - 10));
                        return "\uD589\uBCF5-10 (\uAE38\uC744 \uC783\uC5C8\uC5B4\uC694)";
                    }
                },
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 10));
                    return "\uCCB4\uB825+10";
                }
        ));

        // 9. Rainbow
        events.add(new RandomEvent(
                "\uD83C\uDF08 \uBB34\uC9C0\uAC1C\uAC00 \uB5B4\uC5B4\uC694!",
                "\uC544\uB984\uB2E4\uC6B4 \uBB34\uC9C0\uAC1C\uAC00 \uD558\uB298\uC5D0 \uB5B4\uC2B5\uB2C8\uB2E4.\n\uAC10\uC0C1\uD574\uBCFC\uAE4C\uC694?",
                "\uAC10\uC0C1\uD55C\uB2E4", "\uD638\uAE30\uC2EC\uC744 \uB530\uB77C\uAC04\uB2E4",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 25));
                    return "\uD589\uBCF5+25";
                },
                (h, main) -> {
                    main.addMoney(40, null);
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 10));
                    return "\uCF54\uC778+40, \uD589\uBCF5+10";
                }
        ));

        // === DISCOVERY EVENTS (+5) ===

        // 10. Hidden stash
        events.add(new RandomEvent(
                "\uD83D\uDCE6 \uC228\uACA8\uC9C4 \uC800\uC7A5\uC18C!",
                "\uB204\uAD70\uAC00 \uC228\uACA8\uB454 \uBB3C\uAC74\uC744 \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4.\n\uC5F4\uC5B4\uBCFC\uAE4C\uC694?",
                "\uC5F4\uC5B4\uBCF8\uB2E4", "\uB0B4\uBC84\uB824\uB454\uB2E4",
                (h, main) -> {
                    int roll = rng.nextInt(3);
                    if (roll == 0) {
                        main.addMoney(100, null);
                        return "\uCF54\uC778+100 (\uB300\uBC15!)";
                    } else if (roll == 1) {
                        h.setHunger(Math.min(h.getMaxHunger(), h.getHunger() + 25));
                        return "\uBC30\uACE0\uD514+25";
                    } else {
                        h.setEnergy(Math.max(0, h.getEnergy() - 10));
                        return "\uCCB4\uB825-10 (\uBE44\uC5B4\uC788\uC5C8\uC5B4\uC694...)";
                    }
                },
                (h, main) -> "\uD6A8\uACFC \uC5C6\uC74C"
        ));

        // 11. Mysterious seed
        events.add(new RandomEvent(
                "\uD83C\uDF31 \uC2E0\uBE44\uD55C \uC528\uC557!",
                "\uBC18\uC9DD\uC774\uB294 \uC528\uC557\uC744 \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4.\n\uBA39\uC5B4\uBCFC\uAE4C\uC694?",
                "\uBA39\uB294\uB2E4", "\uC2EC\uB294\uB2E4",
                (h, main) -> {
                    if (rng.nextBoolean()) {
                        h.setHunger(Math.min(h.getMaxHunger(), h.getHunger() + 40));
                        h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 15));
                        return "\uBC30\uACE0\uD514+40, \uD589\uBCF5+15 (\uB9DB\uC788\uB2E4!)";
                    } else {
                        h.setHunger(Math.min(h.getMaxHunger(), h.getHunger() + 10));
                        h.setHappiness(Math.max(0, h.getHappiness() - 10));
                        return "\uBC30\uACE0\uD514+10, \uD589\uBCF5-10 (\uC4F4\uB9DB\uC774\uC5C8\uC5B4\uC694)";
                    }
                },
                (h, main) -> {
                    main.addMoney(25, null);
                    return "\uCF54\uC778+25 (\uD314\uC558\uC5B4\uC694)";
                }
        ));

        // 12. Shiny object
        events.add(new RandomEvent(
                "\u2728 \uBC18\uC9DD\uC774\uB294 \uBB3C\uCCB4!",
                "\uBC14\uB2E5\uC5D0\uC11C \uBC18\uC9DD\uC774\uB294 \uBB3C\uCCB4\uB97C \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4.",
                "\uC8FC\uC6CC\uBCF8\uB2E4", "\uAD6C\uACBD\uB9CC \uD55C\uB2E4",
                (h, main) -> {
                    main.addMoney(60, null);
                    return "\uCF54\uC778+60";
                },
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 15));
                    return "\uD589\uBCF5+15 (\uC608\uC058\uB2E4!)";
                }
        ));

        // 13. Old tunnel
        events.add(new RandomEvent(
                "\uD83D\uDD73\uFE0F \uC624\uB798\uB41C \uD130\uB110!",
                "\uC624\uB798\uB41C \uD130\uB110\uC744 \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4.\n\uB4E4\uC5B4\uAC00\uBCFC\uAE4C\uC694?",
                "\uD0D0\uD5D8\uD55C\uB2E4", "\uC870\uC2EC\uD55C\uB2E4",
                (h, main) -> {
                    if (rng.nextBoolean()) {
                        main.addMoney(70, null);
                        h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 20));
                        return "\uCF54\uC778+70, \uD589\uBCF5+20 (\uBAA8\uD5D8 \uC131\uACF5!)";
                    } else {
                        h.setEnergy(Math.max(0, h.getEnergy() - 20));
                        return "\uCCB4\uB825-20 (\uAE38\uC744 \uC783\uC5C8\uC5B4\uC694)";
                    }
                },
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 5));
                    return "\uCCB4\uB825+5 (\uC870\uC6A9\uD788 \uAD6C\uACBD\uD588\uC5B4\uC694)";
                }
        ));

        // 14. Flower field
        events.add(new RandomEvent(
                "\uD83C\uDF3A \uAF43\uBC2D \uBC1C\uACAC!",
                "\uC544\uB984\uB2E4\uC6B4 \uAF43\uBC2D\uC744 \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4.",
                "\uAF43 \uC0AC\uC774\uC5D0\uC11C \uB6F0\uAE30", "\uAF43 \uD5A5\uAE30 \uB9E1\uAE30",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 20));
                    h.setEnergy(Math.max(0, h.getEnergy() - 8));
                    return "\uD589\uBCF5+20, \uCCB4\uB825-8";
                },
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 10));
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 5));
                    return "\uD589\uBCF5+10, \uCCB4\uB825+5";
                }
        ));

        // === SOCIAL EVENTS (+5) ===

        // 15. Friendly visitor
        events.add(new RandomEvent(
                "\uD83D\uDC3F\uFE0F \uCE5C\uAD6C\uAC00 \uCC3E\uC544\uC654\uC5B4\uC694!",
                "\uB2E4\uB78C\uC950\uAC00 \uB180\uB7EC\uC654\uC2B5\uB2C8\uB2E4.\n\uC5B4\uB5BB\uAC8C \uD560\uAE4C\uC694?",
                "\uAC19\uC774 \uB17C\uB2E4", "\uAC04\uC2DD\uC744 \uB098\uB220\uC900\uB2E4",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 20));
                    h.setEnergy(Math.max(0, h.getEnergy() - 10));
                    return "\uD589\uBCF5+20, \uCCB4\uB825-10";
                },
                (h, main) -> {
                    h.setHunger(Math.max(0, h.getHunger() - 10));
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 15));
                    main.addMoney(15, null);
                    return "\uBC30\uACE0\uD514-10, \uD589\uBCF5+15, \uCF54\uC778+15";
                }
        ));

        // 16. Music
        events.add(new RandomEvent(
                "\uD83C\uDFB5 \uC74C\uC545\uC774 \uB4E4\uB824\uC694!",
                "\uC5B4\uB514\uC120\uAC00 \uC544\uB984\uB2E4\uC6B4 \uC74C\uC545\uC774 \uB4E4\uB824\uC635\uB2C8\uB2E4.",
                "\uCDA4\uCD98\uB2E4", "\uB4E3\uB294\uB2E4",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 25));
                    h.setEnergy(Math.max(0, h.getEnergy() - 15));
                    return "\uD589\uBCF5+25, \uCCB4\uB825-15";
                },
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 10));
                    h.addBuff(new Buff(Buff.Type.HAPPINESS_DRAIN, 0.6, 5400, "\uD589\uBCF5\uAC10\uC18C\u2193"));
                    return "\uD589\uBCF5+10, \uD589\uBCF5\uAC10\uC18C\u2193 3\uBD84";
                }
        ));

        // 17. Storytelling
        events.add(new RandomEvent(
                "\uD83D\uDCD6 \uC774\uC57C\uAE30 \uC2DC\uAC04!",
                "\uB204\uAD70\uAC00 \uC7AC\uBBF8\uC788\uB294 \uC774\uC57C\uAE30\uB97C \uD574\uC90D\uB2C8\uB2E4.",
                "\uBAA8\uD5D8 \uC774\uC57C\uAE30", "\uBB34\uC11C\uC6B4 \uC774\uC57C\uAE30",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 15));
                    return "\uD589\uBCF5+15 (\uC2E0\uB098\uB294 \uBAA8\uD5D8!)";
                },
                (h, main) -> {
                    if (rng.nextBoolean()) {
                        h.setHappiness(Math.max(0, h.getHappiness() - 5));
                        return "\uD589\uBCF5-5 (\uBB34\uC11C\uC6CC\uC694...)";
                    } else {
                        h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 20));
                        return "\uD589\uBCF5+20 (\uC2A4\uB9B4 \uB9CC\uC810!)";
                    }
                }
        ));

        // 18. Competition
        events.add(new RandomEvent(
                "\uD83C\uDFC6 \uB300\uD68C\uAC00 \uC5F4\uB824\uC694!",
                "\uD584\uC2A4\uD130 \uACBD\uC8FC \uB300\uD68C\uAC00 \uC5F4\uB9BD\uB2C8\uB2E4.\n\uCC38\uAC00\uD560\uAE4C\uC694?",
                "\uCC38\uAC00\uD55C\uB2E4", "\uC751\uC6D0\uD55C\uB2E4",
                (h, main) -> {
                    if (h.getEnergy() > 50) {
                        main.addMoney(80, null);
                        h.setEnergy(Math.max(0, h.getEnergy() - 25));
                        h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 20));
                        return "\uCF54\uC778+80, \uCCB4\uB825-25, \uD589\uBCF5+20 (\uC6B0\uC2B9!)";
                    } else {
                        h.setEnergy(Math.max(0, h.getEnergy() - 15));
                        h.setHappiness(Math.max(0, h.getHappiness() - 5));
                        return "\uCCB4\uB825-15, \uD589\uBCF5-5 (\uC544\uC27D\uAC8C \uD0C8\uB77D)";
                    }
                },
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 10));
                    return "\uD589\uBCF5+10 (\uC751\uC6D0\uD558\uAE30 \uC7AC\uBBF8\uC788\uC5C8\uC5B4\uC694)";
                }
        ));

        // 19. Gift exchange
        events.add(new RandomEvent(
                "\uD83C\uDF81 \uC120\uBB3C \uAD50\uD658!",
                "\uB2E4\uB978 \uD584\uC2A4\uD130\uAC00 \uC120\uBB3C\uC744 \uC8FC\uB824\uACE0 \uD574\uC694.\n\uBC1B\uC744\uAE4C\uC694?",
                "\uBC1B\uB294\uB2E4", "\uB300\uC2E0 \uCF54\uC778\uC744 \uC900\uB2E4",
                (h, main) -> {
                    h.setHunger(Math.min(h.getMaxHunger(), h.getHunger() + 20));
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 15));
                    return "\uBC30\uACE0\uD514+20, \uD589\uBCF5+15";
                },
                (h, main) -> {
                    main.addMoney(-20, null);
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 25));
                    h.addBuff(new Buff(Buff.Type.COIN_BONUS, 1.5, 5400, "\uCF54\uC778\uD68D\uB4DD\u2191"));
                    return "\uCF54\uC778-20, \uD589\uBCF5+25, \uCF54\uC778\uD68D\uB4DD\u2191 3\uBD84";
                }
        ));

        // === HEALTH EVENTS (+4) ===

        // 20. Sneezing
        events.add(new RandomEvent(
                "\uD83E\uDD27 \uC7AC\uCC44\uAE30\uB97C \uD574\uC694!",
                "\uD584\uC2A4\uD130\uAC00 \uC7AC\uCC44\uAE30\uB97C \uD569\uB2C8\uB2E4.\n\uC5B4\uB5BB\uAC8C \uD560\uAE4C\uC694?",
                "\uB530\uB73B\uD558\uAC8C \uD574\uC900\uB2E4", "\uC57D\uC744 \uCC3E\uB294\uB2E4",
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 10));
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 10));
                    return "\uCCB4\uB825+10, \uD589\uBCF5+10 (\uD3EC\uADFC\uD574\uC84C\uC5B4\uC694)";
                },
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 20));
                    h.setHunger(Math.max(0, h.getHunger() - 5));
                    return "\uCCB4\uB825+20, \uBC30\uACE0\uD514-5";
                }
        ));

        // 21. Stretching
        events.add(new RandomEvent(
                "\uD83E\uDDD8 \uC2A4\uD2B8\uB808\uCE6D \uC2DC\uAC04!",
                "\uBAB8\uC744 \uD3B4\uBA74 \uAE30\uBD84\uC774 \uC88B\uC544\uC9C8 \uAC83 \uAC19\uC544\uC694.",
                "\uC2A4\uD2B8\uB808\uCE6D \uD558\uAE30", "\uADF8\uB0E5 \uC26C\uAE30",
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 15));
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 5));
                    return "\uCCB4\uB825+15, \uD589\uBCF5+5";
                },
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 10));
                    return "\uCCB4\uB825+10";
                }
        ));

        // 22. Tummy ache
        events.add(new RandomEvent(
                "\uD83E\uDD22 \uBC30\uAC00 \uC544\uD30C\uC694!",
                "\uD584\uC2A4\uD130\uAC00 \uBC30\uD0C8\uC744 \uD558\uACE0 \uC788\uC2B5\uB2C8\uB2E4.",
                "\uBB3C\uC744 \uB9C8\uC2DC\uAC8C \uD55C\uB2E4", "\uC26C\uAC8C \uD55C\uB2E4",
                (h, main) -> {
                    h.setHunger(Math.max(0, h.getHunger() - 10));
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 10));
                    return "\uBC30\uACE0\uD514-10, \uCCB4\uB825+10";
                },
                (h, main) -> {
                    h.addBuff(new Buff(Buff.Type.HUNGER_DRAIN, 0.5, 3600, "\uBC30\uACE0\uD514\uAC10\uC18C\u2193"));
                    return "\uBC30\uACE0\uD514\uAC10\uC18C\u2193 2\uBD84";
                }
        ));

        // 23. Sunbathing
        events.add(new RandomEvent(
                "\u2600\uFE0F \uC77C\uAD11\uC695 \uC2DC\uAC04!",
                "\uB530\uC2A4\uD55C \uD587\uBE5B\uC774 \uB0B4\uB9AC\uC3D4\uACE0 \uC788\uC5B4\uC694.",
                "\uC77C\uAD11\uC695 \uD558\uAE30", "\uADF8\uB298\uC5D0\uC11C \uC26C\uAE30",
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 20));
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 10));
                    h.setHunger(Math.max(0, h.getHunger() - 5));
                    return "\uCCB4\uB825+20, \uD589\uBCF5+10, \uBC30\uACE0\uD514-5";
                },
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 10));
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 5));
                    return "\uCCB4\uB825+10, \uD589\uBCF5+5";
                }
        ));

        // === ADVENTURE EVENTS (+4) ===

        // 24. Treasure map
        events.add(new RandomEvent(
                "\uD83D\uDDFA\uFE0F \uBCF4\uBB3C \uC9C0\uB3C4!",
                "\uB0A1\uC740 \uBCF4\uBB3C \uC9C0\uB3C4\uB97C \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4!",
                "\uBCF4\uBB3C\uC744 \uCC3E\uC73C\uB7EC \uAC04\uB2E4", "\uD314\uC544\uBC84\uB9B0\uB2E4",
                (h, main) -> {
                    if (rng.nextInt(3) != 0) {
                        main.addMoney(120, null);
                        h.setEnergy(Math.max(0, h.getEnergy() - 20));
                        return "\uCF54\uC778+120, \uCCB4\uB825-20 (\uBCF4\uBB3C \uBC1C\uACAC!)";
                    } else {
                        h.setEnergy(Math.max(0, h.getEnergy() - 20));
                        return "\uCCB4\uB825-20 (\uD5DB\uD0D5\uC774\uC5C8\uC5B4\uC694...)";
                    }
                },
                (h, main) -> {
                    main.addMoney(20, null);
                    return "\uCF54\uC778+20";
                }
        ));

        // 25. Maze
        events.add(new RandomEvent(
                "\uD83C\uDF10 \uBBF8\uB85C \uBC1C\uACAC!",
                "\uBE44\uBC00 \uBBF8\uB85C\uB97C \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4.\n\uB3C4\uC804\uD574\uBCFC\uAE4C\uC694?",
                "\uB3C4\uC804\uD55C\uB2E4", "\uB3CC\uC544\uAC04\uB2E4",
                (h, main) -> {
                    if (h.getEnergy() > 40) {
                        main.addMoney(60, null);
                        h.setEnergy(Math.max(0, h.getEnergy() - 15));
                        h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 15));
                        return "\uCF54\uC778+60, \uCCB4\uB825-15, \uD589\uBCF5+15 (\uD074\uB9AC\uC5B4!)";
                    } else {
                        h.setEnergy(Math.max(0, h.getEnergy() - 10));
                        return "\uCCB4\uB825-10 (\uB108\uBB34 \uD53C\uACE4\uD574\uC694...)";
                    }
                },
                (h, main) -> "\uD6A8\uACFC \uC5C6\uC74C"
        ));

        // 26. Slide
        events.add(new RandomEvent(
                "\uD83C\uDFA2 \uBBF8\uB044\uB7FC\uD2C0 \uBC1C\uACAC!",
                "\uAE38\uACE0 \uAE34 \uBBF8\uB044\uB7FC\uD2C0\uC744 \uBC1C\uACAC\uD588\uC5B4\uC694!",
                "\uD0C0\uBCF8\uB2E4", "\uAD6C\uACBD\uD55C\uB2E4",
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 30));
                    h.setEnergy(Math.max(0, h.getEnergy() - 10));
                    return "\uD589\uBCF5+30, \uCCB4\uB825-10 (\uC2E0\uB09C\uB2E4!)";
                },
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 5));
                    return "\uD589\uBCF5+5";
                }
        ));

        // 27. Mountain climb
        events.add(new RandomEvent(
                "\u26F0\uFE0F \uC791\uC740 \uC0B0!",
                "\uC55E\uC5D0 \uC791\uC740 \uC0B0\uC774 \uC788\uC2B5\uB2C8\uB2E4.\n\uC62C\uB77C\uBCFC\uAE4C\uC694?",
                "\uC62C\uB77C\uAC04\uB2E4", "\uB3CC\uC544\uAC04\uB2E4",
                (h, main) -> {
                    h.setEnergy(Math.max(0, h.getEnergy() - 20));
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 25));
                    main.addMoney(40, null);
                    return "\uCCB4\uB825-20, \uD589\uBCF5+25, \uCF54\uC778+40 (\uC815\uC0C1!)";
                },
                (h, main) -> {
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 5));
                    return "\uCCB4\uB825+5";
                }
        ));

        // === SPECIAL EVENTS (+3) ===

        // 28. Lucky day
        events.add(new RandomEvent(
                "\uD83C\uDF1F \uD589\uC6B4\uC758 \uB0A0!",
                "\uC624\uB298\uC740 \uBB54\uAC00 \uD2B9\uBCC4\uD55C \uB0A0\uC778 \uAC83 \uAC19\uC544\uC694!",
                "\uBAA8\uD5D8\uC744 \uB5A0\uB09C\uB2E4", "\uD587\uC6B4\uC744 \uC800\uCD95\uD55C\uB2E4",
                (h, main) -> {
                    h.setHunger(Math.min(h.getMaxHunger(), h.getHunger() + 15));
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 15));
                    h.setEnergy(Math.min(h.getMaxEnergy(), h.getEnergy() + 15));
                    return "\uBC30\uACE0\uD514+15, \uD589\uBCF5+15, \uCCB4\uB825+15 (\uBAA8\uB4E0 \uAC83\uC774 \uC88B\uC544\uC694!)";
                },
                (h, main) -> {
                    h.addBuff(new Buff(Buff.Type.COIN_BONUS, 3.0, 5400, "\uCF54\uC778\uD68D\uB4DD\u2191\u2191"));
                    return "\uCF54\uC778\uD68D\uB4DD\u2191\u2191 3\uBD84 (3\uBC30!)";
                }
        ));

        // 29. Falling star
        events.add(new RandomEvent(
                "\uD83C\uDF20 \uBCC4\uB611\uBCC4\uC774 \uB5A8\uC5B4\uC838\uC694!",
                "\uD558\uB298\uC5D0\uC11C \uBCC4\uB611\uBCC4\uC774 \uB5A8\uC5B4\uC84C\uC2B5\uB2C8\uB2E4!\n\uC18C\uC6D0\uC744 \uBE4C\uC5B4\uBCFC\uAE4C\uC694?",
                "\uAC74\uAC15\uC744 \uBE48\uB2E4", "\uBD80\uB97C \uBE48\uB2E4",
                (h, main) -> {
                    h.addBuff(new Buff(Buff.Type.ENERGY_DRAIN, 0.3, 9000, "\uCCB4\uB825\uAC10\uC18C\u2193\u2193"));
                    h.addBuff(new Buff(Buff.Type.HUNGER_DRAIN, 0.5, 9000, "\uBC30\uACE0\uD514\uAC10\uC18C\u2193"));
                    return "\uCCB4\uB825\uAC10\uC18C\u2193\u2193, \uBC30\uACE0\uD514\uAC10\uC18C\u2193 5\uBD84";
                },
                (h, main) -> {
                    h.addBuff(new Buff(Buff.Type.COIN_BONUS, 2.5, 9000, "\uCF54\uC778\uD68D\uB4DD\u2191\u2191"));
                    return "\uCF54\uC778\uD68D\uB4DD\u2191\u2191 5\uBD84 (2.5\uBC30!)";
                }
        ));

        // 30. Time capsule
        events.add(new RandomEvent(
                "\u23F3 \uD0C0\uC784\uCEA1\uC290 \uBC1C\uACAC!",
                "\uC624\uB798\uB41C \uD0C0\uC784\uCEA1\uC290\uC744 \uBC1C\uACAC\uD588\uC2B5\uB2C8\uB2E4!",
                "\uC5F4\uC5B4\uBCF8\uB2E4", "\uBB3B\uC5B4\uB454\uB2E4",
                (h, main) -> {
                    int reward = 30 + rng.nextInt(71); // 30~100
                    main.addMoney(reward, null);
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 20));
                    return "\uCF54\uC778+" + reward + ", \uD589\uBCF5+20";
                },
                (h, main) -> {
                    h.setHappiness(Math.min(h.getMaxHappiness(), h.getHappiness() + 5));
                    return "\uD589\uBCF5+5 (\uB2E4\uC74C\uC5D0 \uC5F4\uC5B4\uBCF4\uC790)";
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
