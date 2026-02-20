package com.hamster;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    private final List<Hamster> hamsters = new ArrayList<>();
    private final List<HamsterWindow> hamsterWindows = new ArrayList<>();
    private final List<PoopWindow> poopWindows = new ArrayList<>();
    private final Random random = new Random();

    private ControlPanel controlPanel;
    private int money = 0;
    private long totalFrames = 0;
    private boolean hidden = false;
    private GlobalHotkeyManager hotkeyManager;
    private int eventTimer = 0;
    private static final int EVENT_INTERVAL = 4500; // ~2.5 minutes
    private int[] pendingLegacy = null; // stored when last hamster dies

    private MetaProgress metaProgress;
    private int hamstersRaised = 1;
    private Timer gameTimer;
    private boolean systemSetupDone = false;

    public static void main(String[] args) {
        // Global exception handler for debugging
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("[ERROR] Uncaught exception in thread: " + t.getName());
            e.printStackTrace();
        });

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            new Main().start();
        });
    }

    private void start() {
        metaProgress = MetaProgress.load();
        StartDialog dialog = StartDialog.showAndWait(metaProgress);
        StartDialog.Result result = dialog.getResult();

        switch (result) {
            case NEW_GAME:
                startNewGame();
                break;
            case LOAD_GAME:
                startFromSave(dialog.getLoadedState());
                break;
            case EXIT:
            default:
                System.exit(0);
                break;
        }
    }

    private void startNewGame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());
        int groundY = screenSize.height - insets.bottom - 100;

        int minDays = metaProgress.getMinLifespanDays();
        int maxDays = metaProgress.getMaxLifespanDays();
        int lifespanFrames = (minDays + random.nextInt(maxDays - minDays + 1)) * Hamster.FRAMES_PER_DAY;

        Hamster h = new Hamster(screenSize.width, 10, HamsterColor.WHITE, lifespanFrames);
        h.setName("\uB0B4 \uD584\uC2A4\uD130");
        h.setAgingSpeed(metaProgress.getAgingSpeed());
        h.setActionGain(metaProgress.getActionGain());
        h.setDrainMultiplier(metaProgress.getDrainMultiplier());

        if (pendingLegacy != null) {
            h.setGeneration(pendingLegacy[0]);
            h.setLegacyHungerBonus(pendingLegacy[1]);
            h.setLegacyHappinessBonus(pendingLegacy[2]);
            h.setLegacyEnergyBonus(pendingLegacy[3]);
            h.setLegacyLifespanBonus(pendingLegacy[4]);
            h.setLegacyMaxStatBonus(pendingLegacy[5]);
            h.applyLegacyBonuses();
            pendingLegacy = null;
        }

        hamsters.add(h);

        HamsterWindow w = new HamsterWindow(h);
        w.setLocation(screenSize.width / 2 - 40, groundY);
        hamsterWindows.add(w);

        money = 0;
        totalFrames = 0;
        hamstersRaised = 1;

        startGameLoop();
    }

    private void startFromSave(GameState state) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        money = state.money;
        totalFrames = state.totalFrames;
        hamstersRaised = state.hamstersRaised;
        pendingLegacy = null; // discard pending legacy when loading a save

        for (GameState.HamsterData hd : state.hamsters) {
            Hamster h = new Hamster(screenSize.width, 10, hd.color, hd.lifespanFrames);
            h.setName(hd.name);
            h.setHunger(hd.hunger);
            h.setHappiness(hd.happiness);
            h.setEnergy(hd.energy);
            h.setPoopTimer(hd.poopTimer);
            h.setAgeFrames(hd.ageFrames);
            // Restore roguelike data
            h.setGeneration(hd.generation);
            h.setLegacyHungerBonus(hd.legacyHungerBonus);
            h.setLegacyHappinessBonus(hd.legacyHappinessBonus);
            h.setLegacyEnergyBonus(hd.legacyEnergyBonus);
            h.setLegacyLifespanBonus(hd.legacyLifespanBonus);
            h.setLegacyMaxStatBonus(hd.legacyMaxStatBonus);
            h.setMaxHunger(hd.maxHunger);
            h.setMaxHappiness(hd.maxHappiness);
            h.setMaxEnergy(hd.maxEnergy);
            h.setBreedCooldownFrames(hd.breedCooldownFrames);
            // Apply meta values
            h.setAgingSpeed(metaProgress.getAgingSpeed());
            h.setActionGain(metaProgress.getActionGain());
            h.setDrainMultiplier(metaProgress.getDrainMultiplier());
            for (GameState.BuffData bd : hd.buffs) {
                h.addBuff(new Buff(bd.type, bd.multiplier, bd.remainingFrames, bd.description));
            }
            hamsters.add(h);

            HamsterWindow w = new HamsterWindow(h);
            w.setLocation(hd.windowX, hd.windowY);
            hamsterWindows.add(w);
        }

        for (GameState.PoopData pd : state.poops) {
            Poop poop = new Poop(pd.screenX, pd.screenY, 0);
            final PoopWindow[] holder = new PoopWindow[1];
            holder[0] = new PoopWindow(poop, () -> {
                poopWindows.remove(holder[0]);
                addMoney(5, null);
            });
            poopWindows.add(holder[0]);
        }

        startGameLoop();
    }

    private void startGameLoop() {
        controlPanel = new ControlPanel(hamsters, new ControlPanel.Callbacks() {
            @Override
            public void onCleanAll() {
                int count = poopWindows.size();
                for (PoopWindow pw : new ArrayList<>(poopWindows)) {
                    pw.dispose();
                }
                addMoney(3 * count, null);
                poopWindows.clear();
            }

            @Override
            public void onFeed(Hamster h) {
                h.feed();
            }

            @Override
            public void onPlay(Hamster h) {
                h.play();
            }

            @Override
            public void onRunWheel(Hamster h) {
                h.runWheel();
            }

            @Override
            public void onSleep(Hamster h) {
                h.sleep();
            }

            @Override
            public void onOpenShop() {
                openShop();
            }

            @Override
            public void onBreed() {
                openBreed();
            }
        });

        gameTimer = new Timer(33, e -> gameLoop());
        gameTimer.start();

        if (!systemSetupDone) {
            setupTrayIcon();
            hotkeyManager = new GlobalHotkeyManager();
            hotkeyManager.start(this::toggleAllWindows);
            systemSetupDone = true;
        }
    }

    private void gameLoop() {
        if (hidden) return;

        totalFrames++;

        // Tick all windows
        for (HamsterWindow w : hamsterWindows) {
            w.tick();
        }

        // Check poop for each hamster
        for (int i = 0; i < hamsters.size(); i++) {
            Hamster h = hamsters.get(i);
            if (!h.isDead() && h.shouldPoop()) {
                spawnPoop(hamsterWindows.get(i));
            }
        }

        // Collect pending coins from ongoing actions
        for (Hamster h : hamsters) {
            int coins = h.collectPendingCoins();
            if (coins > 0) {
                addMoney(coins, h);
            }
        }

        // Apply poop penalty
        int poopCount = poopWindows.size();
        for (Hamster h : hamsters) {
            h.applyPoopPenalty(poopCount);
        }

        // Check deaths
        checkDeaths();

        // Random event check
        eventTimer++;
        if (eventTimer >= EVENT_INTERVAL && !hamsters.isEmpty()) {
            eventTimer = 0;
            triggerRandomEvent();
        }

        // Passive income: every 900 frames (~30 seconds), +1 coin per living hamster
        if (totalFrames % 900 == 0) {
            for (Hamster h : hamsters) {
                if (!h.isDead()) {
                    addMoney(1, h);
                }
            }
        }

        // Auto-save: every 1800 frames (~1 minute)
        if (totalFrames % 1800 == 0) {
            autoSave();
        }

        // Refresh control panel
        controlPanel.refresh(poopCount, money);
    }

    private void spawnPoop(HamsterWindow window) {
        Point pos = window.getHamsterScreenPosition();
        int offsetX = random.nextInt(21) - 10;
        int offsetY = random.nextInt(11) - 5;
        Poop poop = new Poop(pos.x - 15 + offsetX, pos.y - 30 + offsetY, 0);
        final PoopWindow[] holder = new PoopWindow[1];
        holder[0] = new PoopWindow(poop, () -> {
            poopWindows.remove(holder[0]);
            addMoney(5, null);
        });
        poopWindows.add(holder[0]);
    }

    private void checkDeaths() {
        boolean anyDied = false;
        for (int i = hamsters.size() - 1; i >= 0; i--) {
            Hamster h = hamsters.get(i);
            if (h.isDead()) {
                String name = h.getName();
                int gen = h.getGeneration();
                int[] legacy = h.computeLegacy();
                pendingLegacy = legacy;

                HamsterWindow w = hamsterWindows.get(i);
                w.dispose();
                hamsters.remove(i);
                hamsterWindows.remove(i);
                anyDied = true;

                String deathMsg = name + "\uC774(\uAC00) \uBB34\uC9C0\uAC1C \uB2E4\uB9AC\uB97C \uAC74\uB110\uC2B5\uB2C8\uB2E4. (" + gen + "\uC138\uB300)";

                // Show legacy earned message
                int avgStat = (h.getHunger() + h.getHappiness() + h.getEnergy()) / 3;
                if (avgStat > 30) {
                    deathMsg += "\n\uB808\uAC70\uC2DC \uD68D\uB4DD! \uBC30\uACE0\uD514+5, \uD589\uBCF5+5, \uCCB4\uB825+5, \uC218\uBA85+1\uC77C, \uCD5C\uB300\uC2A4\uD0EF+5";
                }

                JOptionPane.showMessageDialog(controlPanel, deathMsg,
                        "\uC548\uB155\uD788...", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        if (anyDied) {
            controlPanel.rebuild(hamsters);
            if (hamsters.isEmpty()) {
                gameOver();
            }
        }
    }

    private void gameOver() {
        gameTimer.stop();

        int seeds = MetaProgress.calculateSeeds(hamstersRaised, money);
        metaProgress.addSeeds(seeds);

        String msg = "\uAC8C\uC784 \uC624\uBC84!\n\n" +
                "\uD0A4\uC6B4 \uD584\uC2A4\uD130: " + hamstersRaised + "\uB9C8\uB9AC\n" +
                "\uB0A8\uC740 \uCF54\uC778: " + money + "\n\n" +
                "\uD68D\uB4DD \uD574\uBC14\uB77C\uAE30\uC528: " + seeds + "\uAC1C\n" +
                "\uCD1D \uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C";
        JOptionPane.showMessageDialog(controlPanel, msg,
                "\uAC8C\uC784 \uC624\uBC84", JOptionPane.INFORMATION_MESSAGE);

        // Cleanup windows
        for (HamsterWindow w : new ArrayList<>(hamsterWindows)) w.dispose();
        hamsterWindows.clear();
        for (PoopWindow pw : new ArrayList<>(poopWindows)) pw.dispose();
        poopWindows.clear();
        hamsters.clear();
        controlPanel.dispose();

        // Delete auto-save (game over = no continue)
        SaveManager.deleteAutoSave();

        // Reset state (pendingLegacy is kept for next run)
        money = 0;
        totalFrames = 0;
        hamstersRaised = 1;
        eventTimer = 0;

        // Back to start screen
        start();
    }

    private void openBreed() {
        int maxSlots = metaProgress.getMaxHamsterSlots();
        if (hamsters.size() >= maxSlots) {
            JOptionPane.showMessageDialog(controlPanel,
                    "\uD584\uC2A4\uD130 \uC2AC\uB86F\uC774 \uAC00\uB4DD \uCC3C\uC2B5\uB2C8\uB2E4!",
                    "\uAD50\uBC30", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int breedAgeFrames = metaProgress.getBreedAgeFrames();

        // Find eligible hamsters
        List<Hamster> eligible = new ArrayList<>();
        for (Hamster h : hamsters) {
            if (h.canBreed(breedAgeFrames)) {
                eligible.add(h);
            }
        }

        if (eligible.size() < 2) {
            StringBuilder cond = new StringBuilder();
            cond.append("\uAD50\uBC30 \uAC00\uB2A5\uD55C \uD584\uC2A4\uD130\uAC00 2\uB9C8\uB9AC \uC774\uC0C1 \uD544\uC694\uD569\uB2C8\uB2E4.\n");
            cond.append("\uC870\uAC74: \uB098\uC774 ").append(metaProgress.getBreedAgeDaysText()).append(" \uC774\uC0C1, ");
            cond.append("\uCFE8\uB2E4\uC6B4 \uC5C6\uC74C,\n");
            cond.append("\uBC30\uACE0\uD514/\uD589\uBCF5/\uCCB4\uB825 \uBAA8\uB450 50 \uC774\uC0C1");
            JOptionPane.showMessageDialog(controlPanel, cond.toString(),
                    "\uAD50\uBC30", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Select parent 1
        String[] names1 = new String[eligible.size()];
        for (int i = 0; i < eligible.size(); i++) {
            Hamster h = eligible.get(i);
            names1[i] = h.getName() + " (" + h.getAgeDays() + "\uC77C, " + h.getGeneration() + "\uC138\uB300)";
        }

        String sel1 = (String) JOptionPane.showInputDialog(controlPanel,
                "\uCCAB \uBC88\uC9F8 \uBD80\uBAA8\uB97C \uC120\uD0DD\uD558\uC138\uC694:",
                "\uAD50\uBC30 - \uBD80\uBAA8 1", JOptionPane.PLAIN_MESSAGE, null, names1, names1[0]);
        if (sel1 == null) return;

        int idx1 = -1;
        for (int i = 0; i < names1.length; i++) {
            if (sel1.equals(names1[i])) { idx1 = i; break; }
        }
        if (idx1 < 0) return;
        Hamster parent1 = eligible.get(idx1);

        // Select parent 2 (exclude parent 1)
        List<Hamster> eligible2 = new ArrayList<>(eligible);
        eligible2.remove(parent1);

        String[] names2 = new String[eligible2.size()];
        for (int i = 0; i < eligible2.size(); i++) {
            Hamster h = eligible2.get(i);
            names2[i] = h.getName() + " (" + h.getAgeDays() + "\uC77C, " + h.getGeneration() + "\uC138\uB300)";
        }

        String sel2 = (String) JOptionPane.showInputDialog(controlPanel,
                "\uB450 \uBC88\uC9F8 \uBD80\uBAA8\uB97C \uC120\uD0DD\uD558\uC138\uC694:",
                "\uAD50\uBC30 - \uBD80\uBAA8 2", JOptionPane.PLAIN_MESSAGE, null, names2, names2[0]);
        if (sel2 == null) return;

        int idx2 = -1;
        for (int i = 0; i < names2.length; i++) {
            if (sel2.equals(names2[i])) { idx2 = i; break; }
        }
        if (idx2 < 0) return;
        Hamster parent2 = eligible2.get(idx2);

        // Confirm
        int confirm = JOptionPane.showConfirmDialog(controlPanel,
                parent1.getName() + " \u00D7 " + parent2.getName() + " \uAD50\uBC30\uB97C \uC9C4\uD589\uD560\uAE4C\uC694?",
                "\uAD50\uBC30 \uD655\uC778", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Create baby hamster
        HamsterColor babyColor = random.nextBoolean() ? parent1.getColor() : parent2.getColor();
        int babyGen = Math.max(parent1.getGeneration(), parent2.getGeneration()) + 1;

        // Baby inherits max of parents' legacy bonuses
        int babyHungerBonus = Math.max(parent1.getLegacyHungerBonus(), parent2.getLegacyHungerBonus());
        int babyHappinessBonus = Math.max(parent1.getLegacyHappinessBonus(), parent2.getLegacyHappinessBonus());
        int babyEnergyBonus = Math.max(parent1.getLegacyEnergyBonus(), parent2.getLegacyEnergyBonus());
        int babyLifespanBonus = Math.max(parent1.getLegacyLifespanBonus(), parent2.getLegacyLifespanBonus());
        int babyMaxStatBonus = Math.max(parent1.getLegacyMaxStatBonus(), parent2.getLegacyMaxStatBonus());

        String name = JOptionPane.showInputDialog(controlPanel,
                "\uC544\uAE30 \uD584\uC2A4\uD130\uC758 \uC774\uB984\uC744 \uC785\uB825\uD558\uC138\uC694:",
                "\uC0C8 \uD584\uC2A4\uD130");
        if (name == null || name.trim().isEmpty()) {
            name = "\uC544\uAE30 \uD584\uC2A4\uD130";
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());
        int groundY = screenSize.height - insets.bottom - 100;

        int minDays = metaProgress.getMinLifespanDays();
        int maxDays = metaProgress.getMaxLifespanDays();
        int lifespanFrames = (minDays + random.nextInt(maxDays - minDays + 1)) * Hamster.FRAMES_PER_DAY;

        Hamster baby = new Hamster(screenSize.width, 10, babyColor, lifespanFrames);
        baby.setName(name.trim());
        baby.setGeneration(babyGen);
        baby.setLegacyHungerBonus(babyHungerBonus);
        baby.setLegacyHappinessBonus(babyHappinessBonus);
        baby.setLegacyEnergyBonus(babyEnergyBonus);
        baby.setLegacyLifespanBonus(babyLifespanBonus);
        baby.setLegacyMaxStatBonus(babyMaxStatBonus);
        baby.applyLegacyBonuses();
        baby.setAgingSpeed(metaProgress.getAgingSpeed());
        baby.setActionGain(metaProgress.getActionGain());
        baby.setDrainMultiplier(metaProgress.getDrainMultiplier());
        hamsters.add(baby);
        hamstersRaised++;

        int offsetX = (hamsters.size() - 1) * 80;
        HamsterWindow w = new HamsterWindow(baby);
        w.setLocation(screenSize.width / 2 - 40 + offsetX, groundY);
        hamsterWindows.add(w);

        // Apply cooldown to both parents
        parent1.startBreedCooldown();
        parent2.startBreedCooldown();

        controlPanel.rebuild(hamsters);
        autoSave();

        JOptionPane.showMessageDialog(controlPanel,
                "\uC544\uAE30 \uD584\uC2A4\uD130 \"" + baby.getName() + "\"\uC774(\uAC00) \uD0DC\uC5B4\uB0AC\uC2B5\uB2C8\uB2E4! (" + babyGen + "\uC138\uB300)\n" +
                "\uBD80\uBAA8: " + parent1.getName() + " \u00D7 " + parent2.getName(),
                "\uAD50\uBC30 \uC131\uACF5!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void autoSave() {
        GameState state = GameState.capture(money, totalFrames, hamsters, hamsterWindows, poopWindows, hamstersRaised);
        SaveManager.saveAuto(state);
    }

    private void openShop() {
        int maxSlots = metaProgress.getMaxHamsterSlots();
        HamsterColor purchased = ShopDialog.showAndBuy(money, hamsters.size(), maxSlots);
        if (purchased != null) {
            money -= purchased.getShopPrice();
            hamstersRaised++;

            String name = JOptionPane.showInputDialog(controlPanel,
                    "\uC0C8 \uD584\uC2A4\uD130\uC758 \uC774\uB984\uC744 \uC785\uB825\uD558\uC138\uC694:",
                    "\uC0C8 \uD584\uC2A4\uD130");
            if (name == null || name.trim().isEmpty()) {
                name = purchased.getDisplayName() + " \uD584\uC2A4\uD130";
            }

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
                    GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice().getDefaultConfiguration());
            int groundY = screenSize.height - insets.bottom - 100;

            int minDays = metaProgress.getMinLifespanDays();
            int maxDays = metaProgress.getMaxLifespanDays();
            int lifespanFrames = (minDays + random.nextInt(maxDays - minDays + 1)) * Hamster.FRAMES_PER_DAY;

            Hamster h = new Hamster(screenSize.width, 10, purchased, lifespanFrames);
            h.setName(name.trim());
            h.setAgingSpeed(metaProgress.getAgingSpeed());
            h.setActionGain(metaProgress.getActionGain());
            h.setDrainMultiplier(metaProgress.getDrainMultiplier());
            hamsters.add(h);

            int offsetX = (hamsters.size() - 1) * 80;
            HamsterWindow w = new HamsterWindow(h);
            w.setLocation(screenSize.width / 2 - 40 + offsetX, groundY);
            hamsterWindows.add(w);

            controlPanel.rebuild(hamsters);
            autoSave();
        }
    }

    public void addMoney(int amount, Hamster source) {
        if (source != null) {
            money += (int)(amount * source.getCoinMultiplier());
        } else {
            money += amount;
        }
    }

    private void triggerRandomEvent() {
        // Pick a random living hamster
        Hamster target = null;
        List<Hamster> living = new ArrayList<>();
        for (Hamster h : hamsters) {
            if (!h.isDead()) living.add(h);
        }
        if (living.isEmpty()) return;
        target = living.get(random.nextInt(living.size()));

        RandomEvent event = RandomEvent.pickRandom(random);
        boolean choiceA = EventDialog.showEvent(event);
        String result;
        if (choiceA) {
            result = event.applyChoiceA(target, this);
        } else {
            result = event.applyChoiceB(target, this);
        }

        JOptionPane.showMessageDialog(controlPanel,
                target.getName() + ": " + result,
                "\uC774\uBCA4\uD2B8 \uACB0\uACFC", JOptionPane.INFORMATION_MESSAGE);
    }

    private void toggleAllWindows() {
        hidden = !hidden;
        if (hidden) {
            for (HamsterWindow w : hamsterWindows) w.setVisible(false);
            for (PoopWindow pw : poopWindows) pw.setVisible(false);
            controlPanel.setVisible(false);
        } else {
            for (HamsterWindow w : hamsterWindows) w.setVisible(true);
            for (PoopWindow pw : poopWindows) pw.setVisible(true);
            controlPanel.setVisible(true);
        }
    }

    private void setupTrayIcon() {
        if (!SystemTray.isSupported()) return;
        try {
            Image trayImage = createTrayImage();
            TrayIcon trayIcon = new TrayIcon(trayImage, "\uB370\uC2A4\uD06C\uD0D1 \uD584\uC2A4\uD130");
            trayIcon.setImageAutoSize(true);

            // Use Swing JPopupMenu instead of AWT PopupMenu for proper Korean rendering
            final JPopupMenu popup = new JPopupMenu();

            JMenuItem showItem = new JMenuItem("\uD328\uB110 \uBCF4\uC774\uAE30");
            showItem.addActionListener(e -> controlPanel.setVisible(true));

            JMenuItem toggleItem = new JMenuItem("\uC228\uAE30\uAE30/\uBCF4\uC774\uAE30 (ALT+Q)");
            toggleItem.addActionListener(e -> toggleAllWindows());

            JMenuItem exitItem = new JMenuItem("\uC885\uB8CC");
            exitItem.addActionListener(e -> {
                if (hotkeyManager != null) hotkeyManager.stop();
                System.exit(0);
            });

            popup.add(showItem);
            popup.add(toggleItem);
            popup.addSeparator();
            popup.add(exitItem);

            // Hidden dialog as anchor for the popup menu
            final JDialog trayAnchor = new JDialog();
            trayAnchor.setUndecorated(true);
            trayAnchor.setSize(1, 1);
            trayAnchor.setAlwaysOnTop(true);
            trayAnchor.setIconImages(HamsterIcon.createIcons());

            // Auto-hide popup when it loses focus
            popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}
                @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                    trayAnchor.setVisible(false);
                }
                @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                    trayAnchor.setVisible(false);
                }
            });

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        int popupH = popup.getPreferredSize().height;
                        trayAnchor.setLocation(e.getX(), e.getY() - popupH);
                        trayAnchor.setVisible(true);
                        popup.show(trayAnchor, 0, 0);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                        controlPanel.setVisible(true);
                    }
                }
            });

            SystemTray.getSystemTray().add(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Image createTrayImage() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(230, 180, 120));
        g2.fillOval(1, 3, 14, 12);
        g2.setColor(new Color(255, 235, 205));
        g2.fillOval(4, 6, 8, 7);
        g2.setColor(new Color(30, 30, 30));
        g2.fillOval(4, 6, 2, 2);
        g2.fillOval(10, 6, 2, 2);
        g2.setColor(new Color(200, 120, 120));
        g2.fillOval(7, 8, 2, 2);
        g2.setColor(new Color(230, 180, 120));
        g2.fillOval(2, 0, 5, 5);
        g2.fillOval(9, 0, 5, 5);
        g2.dispose();
        return img;
    }
}
