package com.hamster;
import com.hamster.model.Accessory;
import com.hamster.model.Buff;
import com.hamster.model.FoodInventory;
import com.hamster.model.FoodItem;
import com.hamster.model.GameConstants;
import com.hamster.model.GameState;
import com.hamster.model.GameStatistics;
import com.hamster.model.Hamster;
import com.hamster.model.HamsterColor;
import com.hamster.model.Personality;
import com.hamster.model.Poop;
import com.hamster.model.Settings;
import com.hamster.render.HamsterIcon;
import com.hamster.render.ItemIcon;
import com.hamster.system.Achievement;
import com.hamster.system.AchievementManager;
import com.hamster.system.GameLogger;
import com.hamster.system.GlobalHotkeyManager;
import com.hamster.system.HamsterInteraction;
import com.hamster.system.HamsterJournal;
import com.hamster.system.HamsterManager;
import com.hamster.system.MetaProgress;
import com.hamster.system.RandomEvent;
import com.hamster.system.SaveManager;
import com.hamster.ui.AchievementDialog;
import com.hamster.ui.ControlPanel;
import com.hamster.ui.EventDialog;
import com.hamster.ui.HamsterWindow;
import com.hamster.ui.JournalDialog;
import com.hamster.ui.PoopWindow;
import com.hamster.ui.SettingsDialog;
import com.hamster.ui.ShopDialog;
import com.hamster.ui.StartDialog;
import com.hamster.ui.StatisticsDialog;
import com.hamster.ui.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
    private boolean sentBack = false;
    private GlobalHotkeyManager hotkeyManager;
    private int eventTimer = 0;
    private int[] pendingLegacy = null; // stored when last hamster dies
    private boolean allFrozen = false;

    private MetaProgress metaProgress;
    private Settings settings;
    private int hamstersRaised = 1;
    private Timer gameTimer;
    private boolean systemSetupDone = false;

    // 2.0 systems
    private GameStatistics statistics;
    private AchievementManager achievementManager;
    private HamsterJournal journal;
    private FoodInventory foodInventory;
    private int interactionTimer = 0;
    private int achievementCheckTimer = 0;
    private int hamsterPurchaseCount = 0;
    private HamsterManager hamsterManager;

    // Dialog instance tracking (prevent duplicate opens)
    private JDialog activeUpgradeDialog = null;
    private JDialog activeFeedPopup = null;
    private JDialog activeEquipPopup = null;

    private final HamsterWindow.ContextMenuCallback contextMenuCallback = new HamsterWindow.ContextMenuCallback() {
        @Override public void onFeed(Hamster h) { onFeedWithFood(h); }
        @Override public void onPlay(Hamster h) { h.play(); statistics.totalPlayActions++; achievementManager.totalPlays++; }
        @Override public void onRunWheel(Hamster h) { h.runWheel(); statistics.totalWheelActions++; }
        @Override public void onSleep(Hamster h) { h.sleep(); statistics.totalSleepActions++; }
        @Override public void onEquipAccessory(Hamster h) { onEquipAccessory_(h); }
        @Override public void onKill(Hamster h) { killSingleHamster(h); }
    };

    private static final String SAVE_DIR = GameConstants.SAVE_DIR;
    private static final String LEGACY_FILE = SAVE_DIR + "pending_legacy.properties";

    public static void main(String[] args) {
        GameLogger.init();

        // Global exception handler
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            GameLogger.error("Uncaught exception in thread: " + t.getName(), e);
        });

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                GameLogger.warn("Failed to set system look and feel: " + e.getMessage());
            }

            new Main().start();
        });
    }

    private void start() {
        metaProgress = MetaProgress.load();
        settings = Settings.load();
        statistics = GameStatistics.load();
        achievementManager = AchievementManager.load();
        journal = HamsterJournal.load();
        hamsterManager = new HamsterManager(metaProgress, random);
        loadPendingLegacy();

        if (!systemSetupDone) {
            setupTrayIcon();
            hotkeyManager = new GlobalHotkeyManager();
            hotkeyManager.setFailureCallback((name, text) -> {
                JOptionPane.showMessageDialog(null,
                        text + " \uB2E8\uCD95\uD0A4 \uB4F1\uB85D\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4.\n" +
                        "\uB2E4\uB978 \uD504\uB85C\uADF8\uB7A8\uC774 \uC0AC\uC6A9 \uC911\uC77C \uC218 \uC788\uC2B5\uB2C8\uB2E4.\n" +
                        "\uC124\uC815\uC5D0\uC11C \uB2E4\uB978 \uD0A4\uB85C \uBCC0\uACBD\uD574\uC8FC\uC138\uC694.",
                        "\uB2E8\uCD95\uD0A4 \uB4F1\uB85D \uC2E4\uD328", JOptionPane.WARNING_MESSAGE);
            });
            hotkeyManager.start(settings);
            systemSetupDone = true;
        }

        StartDialog dialog = StartDialog.showAndWait(metaProgress, hotkeyManager, settings, this::onSettingsSaved);
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
        int groundY = hamsterManager.getGroundY(screenSize);

        Hamster h = hamsterManager.createHamster(screenSize.width, HamsterColor.WHITE, "\uB0B4 \uD584\uC2A4\uD130");

        if (pendingLegacy != null) {
            hamsterManager.applyPendingLegacy(h, pendingLegacy);
            pendingLegacy = null;
            savePendingLegacy();
        }

        hamsterManager.registerWithAchievements(h, achievementManager);
        hamsters.add(h);

        HamsterWindow w = new HamsterWindow(h);
        w.setContextMenuCallback(contextMenuCallback);
        w.setLocation(screenSize.width / 2 - 40, groundY);
        hamsterWindows.add(w);
        applySentBackState(w);

        money = 0;
        totalFrames = 0;
        hamstersRaised = 1;
        hamsterPurchaseCount = 0;
        foodInventory = new FoodInventory();
        statistics.totalGamesPlayed++;
        statistics.totalHamstersRaised++;

        startGameLoop();
    }

    private void startFromSave(GameState state) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        money = state.money;
        totalFrames = state.totalFrames;
        hamstersRaised = state.hamstersRaised;
        foodInventory = state.foodInventory != null ? state.foodInventory : new FoodInventory();
        hamsterPurchaseCount = state.hamsterPurchaseCount;
        pendingLegacy = null; // discard pending legacy when loading a save
        savePendingLegacy(); // delete legacy file

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
            hamsterManager.applyMetaValues(h);
            for (GameState.BuffData bd : hd.buffs) {
                h.addBuff(new Buff(bd.type, bd.multiplier, bd.remainingFrames, bd.description));
            }
            // 2.0: personality
            try {
                h.setPersonality(Personality.valueOf(hd.personality));
            } catch (Exception e) {
                h.setPersonality(Personality.CHEERFUL);
            }
            // 2.0: accessories
            for (String accName : hd.ownedAccessories) {
                h.getOwnedAccessories().add(accName);
            }
            for (String accName : hd.equippedAccessories) {
                try {
                    h.equipAccessory(Accessory.valueOf(accName));
                } catch (Exception ignored) {}
            }
            hamsters.add(h);

            HamsterWindow w = new HamsterWindow(h);
            w.setContextMenuCallback(contextMenuCallback);
            w.setLocation(hd.windowX, hd.windowY);
            hamsterWindows.add(w);
            applySentBackState(w);
        }

        // Merge global accessories to all hamsters (in case they were bought after last save)
        for (Hamster h : hamsters) {
            for (String accName : achievementManager.accessoriesBought) {
                h.getOwnedAccessories().add(accName);
            }
        }

        for (GameState.PoopData pd : state.poops) {
            Poop poop = new Poop(pd.screenX, pd.screenY, 0);
            final PoopWindow[] holder = new PoopWindow[1];
            holder[0] = new PoopWindow(poop, () -> {
                poopWindows.remove(holder[0]);
                addMoney(GameConstants.POOP_CLICK_REWARD, null);
            });
            poopWindows.add(holder[0]);
            applySentBackState(holder[0]);
            holder[0].setVisible(true);
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
                addMoney(GameConstants.POOP_CLEAN_ALL_REWARD * count, null);
                poopWindows.clear();
                statistics.totalPoopsCleaned += count;
                achievementManager.totalPoopsCleaned += count;
            }

            @Override
            public void onFeed(Hamster h) {
                onFeedWithFood(h);
            }

            @Override
            public void onPlay(Hamster h) {
                h.play();
                statistics.totalPlayActions++;
                achievementManager.totalPlays++;
            }

            @Override
            public void onRunWheel(Hamster h) {
                h.runWheel();
                statistics.totalWheelActions++;
            }

            @Override
            public void onSleep(Hamster h) {
                h.sleep();
                statistics.totalSleepActions++;
            }

            @Override
            public void onOpenShop() {
                openShop();
            }

            @Override
            public void onBreed() {
                openBreed();
            }

            @Override
            public void onShowUpgradeInfo() {
                showUpgradeInfoDialog();
            }

            @Override
            public void onOpacityChanged(float opacity) {
                setAllWindowsOpacity(opacity);
                settings.opacity = (int)(opacity * 100);
                settings.save();
            }

            @Override
            public void onOpenSettings() {
                SettingsDialog.show(controlPanel, settings, hotkeyManager, s -> onSettingsSaved(s));
            }

            @Override
            public void onKillAll() {
                killAllHamsters();
            }

            @Override
            public void onKillHamster(Hamster h) {
                killSingleHamster(h);
            }

            @Override
            public void onGatherAll() {
                gatherAllHamsters();
            }

            @Override
            public void onFreezeAll() {
                freezeAllHamsters();
            }

            @Override
            public void onShowAchievements() {
                onShowAchievements_();
            }

            @Override
            public void onShowJournal() {
                onShowJournal_();
            }

            @Override
            public void onShowStatistics() {
                onShowStatistics_();
            }

            @Override
            public void onEquipAccessory(Hamster h) {
                onEquipAccessory_(h);
            }
        }, settings.opacity);

        hotkeyManager.setCallback(this::toggleAllWindows);
        hotkeyManager.setSendBackCallback(this::sendBackAllWindows);
        hotkeyManager.setPanelToggleCallback(this::toggleControlPanel);

        // Apply saved opacity
        if (settings.opacity < 100) {
            setAllWindowsOpacity(settings.opacity / 100f);
        }

        gameTimer = new Timer(GameConstants.GAME_TICK_MS, e -> gameLoop());
        gameTimer.start();
    }

    private void gameLoop() {
        // Tick all windows (animation continues even when paused)
        for (HamsterWindow w : hamsterWindows) {
            w.tick();
        }

        // Check poop for each hamster (even when paused)
        for (int i = 0; i < hamsters.size(); i++) {
            Hamster h = hamsters.get(i);
            if (!h.isDead() && h.shouldPoop()) {
                spawnPoop(hamsterWindows.get(i));
            }
        }

        // Refresh control panel (always update UI)
        int poopCount = poopWindows.size();
        controlPanel.refresh(poopCount, money, metaProgress.sunflowerSeeds);

        // Skip all game logic when paused
        if (allFrozen) return;

        totalFrames++;
        statistics.totalPlayTimeFrames++;

        // Collect pending coins from ongoing actions
        for (Hamster h : hamsters) {
            int coins = h.collectPendingCoins();
            if (coins > 0) {
                addMoney(coins, h);
            }
        }

        // Apply poop penalty
        for (Hamster h : hamsters) {
            h.applyPoopPenalty(poopCount);
        }

        // Track max stat reached (check every frame so we don't miss it)
        if (!achievementManager.maxStatReached) {
            for (Hamster h : hamsters) {
                if (!h.isDead() && (h.getHunger() >= h.getMaxHunger()
                        || h.getHappiness() >= h.getMaxHappiness()
                        || h.getEnergy() >= h.getMaxEnergy())) {
                    achievementManager.maxStatReached = true;
                    break;
                }
            }
        }

        // Check deaths
        checkDeaths();

        // Random event check (skip when hidden)
        eventTimer++;
        if (!hidden && eventTimer >= metaProgress.getEventInterval() && !hamsters.isEmpty()) {
            eventTimer = 0;
            triggerRandomEvent();
            statistics.totalEventsTriggered++;
            achievementManager.totalEventsTriggered++;
        }

        // Hamster interactions (skip when hidden)
        interactionTimer++;
        if (!hidden && interactionTimer >= GameConstants.INTERACTION_CHECK_INTERVAL && hamsterWindows.size() >= 2) {
            interactionTimer = 0;
            tryInteraction();
        }

        // Passive income
        if (totalFrames % GameConstants.PASSIVE_INCOME_INTERVAL == 0) {
            for (Hamster h : hamsters) {
                if (!h.isDead()) {
                    addMoney(GameConstants.PASSIVE_INCOME_AMOUNT, h);
                }
            }
        }

        // Achievement check (skip when hidden)
        achievementCheckTimer++;
        if (!hidden && achievementCheckTimer >= GameConstants.ACHIEVEMENT_CHECK_INTERVAL) {
            achievementCheckTimer = 0;
            checkAchievements();
        }

        // Track longest lifespan
        for (Hamster h : hamsters) {
            if (!h.isDead()) {
                int days = h.getAgeDays();
                if (days > statistics.longestLifespanDays) {
                    statistics.longestLifespanDays = days;
                }
                if (h.getGeneration() > statistics.maxGenerationReached) {
                    statistics.maxGenerationReached = h.getGeneration();
                }
            }
        }

        // Auto-save
        if (totalFrames % GameConstants.AUTO_SAVE_INTERVAL == 0) {
            autoSave();
        }
    }

    private void spawnPoop(HamsterWindow window) {
        Point pos = window.getHamsterScreenPosition();
        int offsetX = random.nextInt(21) - 10;
        int offsetY = random.nextInt(11) - 5;
        Poop poop = new Poop(pos.x - 15 + offsetX, pos.y - 30 + offsetY, 0);
        final PoopWindow[] holder = new PoopWindow[1];
        holder[0] = new PoopWindow(poop, () -> {
            poopWindows.remove(holder[0]);
            addMoney(GameConstants.POOP_CLICK_REWARD, null);
        });
        poopWindows.add(holder[0]);
        applySentBackState(holder[0]);
        if (!hidden) {
            holder[0].setVisible(true);
        }
    }

    private void checkDeaths() {
        boolean anyDied = false;
        for (int i = hamsters.size() - 1; i >= 0; i--) {
            Hamster h = hamsters.get(i);
            if (h.isDead()) {
                String name = h.getName();
                int gen = h.getGeneration();
                int[] legacy = h.computeLegacy();
                if (pendingLegacy == null) {
                    pendingLegacy = legacy;
                } else {
                    // Keep the best legacy values from multiple deaths
                    for (int j = 0; j < legacy.length; j++) {
                        pendingLegacy[j] = Math.max(pendingLegacy[j], legacy[j]);
                    }
                }
                savePendingLegacy();

                // Determine cause of death
                String cause = hamsterManager.getCauseOfDeath(h);
                journal.addEntry(h, cause);
                statistics.totalDeaths++;

                int lifeDays = h.getAgeDays();
                if (lifeDays > statistics.longestLifespanDays) {
                    statistics.longestLifespanDays = lifeDays;
                }

                HamsterWindow w = hamsterWindows.get(i);
                w.dispose();
                hamsters.remove(i);
                hamsterWindows.remove(i);
                anyDied = true;

                String deathMsg = name + "\uC774(\uAC00) \uBB34\uC9C0\uAC1C \uB2E4\uB9AC\uB97C \uAC74\uB110\uC2B5\uB2C8\uB2E4. (" + gen + "\uC138\uB300)\n\uC0AC\uC778: " + cause;

                // Show legacy earned message
                int avgStat = (h.getHunger() + h.getHappiness() + h.getEnergy()) / 3;
                if (avgStat > 30) {
                    deathMsg += "\n\uB808\uAC70\uC2DC \uD68D\uB4DD! \uBC30\uACE0\uD514+5, \uD589\uBCF5+5, \uCCB4\uB825+5, \uC218\uBA85+1\uC77C, \uCD5C\uB300\uC2A4\uD0EF+5";
                }

                if (!hidden) {
                    JOptionPane.showMessageDialog(controlPanel, deathMsg,
                            "\uC548\uB155\uD788...", JOptionPane.INFORMATION_MESSAGE);
                }
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
        hamsterPurchaseCount = 0;
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
        String name = JOptionPane.showInputDialog(controlPanel,
                "\uC544\uAE30 \uD584\uC2A4\uD130\uC758 \uC774\uB984\uC744 \uC785\uB825\uD558\uC138\uC694:",
                "\uC0C8 \uD584\uC2A4\uD130");
        if (name == null || name.trim().isEmpty()) {
            name = "\uC544\uAE30 \uD584\uC2A4\uD130";
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int groundY = hamsterManager.getGroundY(screenSize);

        Hamster baby = hamsterManager.createBabyHamster(screenSize.width, parent1, parent2, name.trim());
        hamsterManager.registerWithAchievements(baby, achievementManager);
        hamsters.add(baby);
        hamstersRaised++;
        statistics.totalHamstersRaised++;
        statistics.totalBreeds++;
        achievementManager.totalBreeds++;

        int offsetX = (hamsters.size() - 1) * 80;
        HamsterWindow w = new HamsterWindow(baby);
        w.setContextMenuCallback(contextMenuCallback);
        w.setLocation(screenSize.width / 2 - 40 + offsetX, groundY);
        hamsterWindows.add(w);
        applySentBackState(w);

        // Apply cooldown to both parents
        parent1.startBreedCooldown();
        parent2.startBreedCooldown();

        controlPanel.rebuild(hamsters);
        autoSave();

        JOptionPane.showMessageDialog(controlPanel,
                "\uC544\uAE30 \uD584\uC2A4\uD130 \"" + baby.getName() + "\"\uC774(\uAC00) \uD0DC\uC5B4\uB0AC\uC2B5\uB2C8\uB2E4! (" + baby.getGeneration() + "\uC138\uB300)\n" +
                "\uBD80\uBAA8: " + parent1.getName() + " \u00D7 " + parent2.getName(),
                "\uAD50\uBC30 \uC131\uACF5!", JOptionPane.INFORMATION_MESSAGE);
    }

    // applyMetaValues and applyStartingStats are now in HamsterManager

    private void autoSave() {
        GameState state = GameState.capture(money, totalFrames, hamsters, hamsterWindows, poopWindows, hamstersRaised, foodInventory, hamsterPurchaseCount);
        SaveManager.saveAuto(state);
        statistics.save();
        achievementManager.save();
        journal.save();
    }

    private void showUpgradeInfoDialog() {
        if (activeUpgradeDialog != null && activeUpgradeDialog.isVisible()) {
            activeUpgradeDialog.toFront();
            return;
        }
        JDialog dialog = new JDialog((Frame) null, "\uC5C5\uADF8\uB808\uC774\uB4DC \uC815\uBCF4", false);
        activeUpgradeDialog = dialog;
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { activeUpgradeDialog = null; }
        });
        dialog.setResizable(false);
        dialog.setIconImages(HamsterIcon.createIcons());
        UIHelper.addEscapeClose(dialog);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(new Color(255, 250, 240));

        JLabel titleLabel = new JLabel(ControlPanel.wrapEmoji("\uD83D\uDCCA \uC5C5\uADF8\uB808\uC774\uB4DC \uC815\uBCF4"));
        titleLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 16));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));

        String[][] rows = {
            {"\uC218\uBA85 \uBC94\uC704", "Lv." + metaProgress.lifespanLevel + "/" + MetaProgress.MAX_LIFESPAN_LEVEL,
                metaProgress.getMinLifespanDays() + "~" + metaProgress.getMaxLifespanDays() + "\uC77C"},
            {"\uB178\uD654 \uC18D\uB3C4", "Lv." + metaProgress.agingLevel + "/" + MetaProgress.MAX_AGING_LEVEL,
                String.format("%.2fx", metaProgress.getAgingSpeed())},
            {"\uD589\uB3D9 \uD6A8\uACFC", "Lv." + metaProgress.actionGainLevel + "/" + MetaProgress.MAX_ACTION_GAIN_LEVEL,
                "+" + metaProgress.getActionGain()},
            {"\uAC10\uC18C\uB7C9", "Lv." + metaProgress.drainLevel + "/" + MetaProgress.MAX_DRAIN_LEVEL,
                String.format("%.2fx", metaProgress.getDrainMultiplier())},
            {"\uAC10\uC18C \uC8FC\uAE30", "Lv." + metaProgress.drainIntervalLevel + "/" + MetaProgress.MAX_DRAIN_INTERVAL_LEVEL,
                String.format("%.1f\uCD08", metaProgress.getDrainInterval() / 30.0)},
            {"\uD584\uC2A4\uD130 \uC2AC\uB86F", "Lv." + metaProgress.hamsterSlotLevel + "/" + MetaProgress.MAX_HAMSTER_SLOT_LEVEL,
                metaProgress.getMaxHamsterSlots() + "\uB9C8\uB9AC"},
            {"\uAD50\uBC30 \uB098\uC774", "Lv." + metaProgress.breedAgeLevel + "/" + MetaProgress.MAX_BREED_AGE_LEVEL,
                metaProgress.getBreedAgeDaysText()},
            {"\uCF54\uC778 \uD68D\uB4DD\uB7C9", "Lv." + metaProgress.coinBonusLevel + "/" + MetaProgress.MAX_COIN_BONUS_LEVEL,
                "+" + metaProgress.getCoinBonus()},
            {"\uC751\uAC00 \uBE48\uB3C4", "Lv." + metaProgress.poopFreqLevel + "/" + MetaProgress.MAX_POOP_FREQ_LEVEL,
                (int)(metaProgress.getPoopChanceMultiplier() * 100) + "%"},
            {"\uC751\uAC00 \uD398\uB110\uD2F0", "Lv." + metaProgress.poopPenaltyLevel + "/" + MetaProgress.MAX_POOP_PENALTY_LEVEL,
                (int)(metaProgress.getPoopPenaltyMultiplier() * 100) + "%"},
            {"\uC774\uBCA4\uD2B8 \uC8FC\uAE30", "Lv." + metaProgress.eventIntervalLevel + "/" + MetaProgress.MAX_EVENT_INTERVAL_LEVEL,
                String.format("%.0f\uCD08", metaProgress.getEventInterval() / 30.0)},
            {"\uBC84\uD504 \uC9C0\uC18D", "Lv." + metaProgress.buffDurationLevel + "/" + MetaProgress.MAX_BUFF_DURATION_LEVEL,
                String.format("%.1fx", metaProgress.getBuffDurationMultiplier())},
            {"\uCD08\uAE30 \uC2A4\uD0EF", "Lv." + metaProgress.startingStatsLevel + "/" + MetaProgress.MAX_STARTING_STATS_LEVEL,
                String.valueOf(metaProgress.getStartingStats())}
        };

        for (String[] row : rows) {
            JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
            rowPanel.setOpaque(false);
            rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            rowPanel.setMaximumSize(new Dimension(350, 24));

            JLabel nameLabel = new JLabel(row[0]);
            nameLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 12));
            nameLabel.setForeground(new Color(80, 50, 20));
            rowPanel.add(nameLabel, BorderLayout.WEST);

            JLabel valueLabel = new JLabel(row[1] + "  " + row[2]);
            valueLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
            valueLabel.setForeground(new Color(100, 80, 50));
            valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            rowPanel.add(valueLabel, BorderLayout.EAST);

            panel.add(rowPanel);
            panel.add(Box.createVerticalStrut(4));
        }

        panel.add(Box.createVerticalStrut(10));

        JButton closeBtn = new JButton("\uB2EB\uAE30");
        closeBtn.setFont(new Font("Noto Sans KR", Font.BOLD, 13));
        closeBtn.setFocusPainted(false);
        closeBtn.setBackground(new Color(220, 220, 220));
        closeBtn.setForeground(new Color(80, 50, 20));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.setMaximumSize(new Dimension(200, 32));
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());
        panel.add(closeBtn);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setAlwaysOnTop(true);

        // Position next to control panel (right side if space, otherwise left)
        Point cpLoc = controlPanel.getLocationOnScreen();
        int cpWidth = controlPanel.getWidth();
        int dialogWidth = dialog.getWidth();
        int dialogHeight = dialog.getHeight();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());
        int screenRight = screenSize.width - screenInsets.right;
        int screenTop = screenInsets.top;

        int x, y;
        if (cpLoc.x + cpWidth + dialogWidth + 5 <= screenRight) {
            x = cpLoc.x + cpWidth + 5;
        } else if (cpLoc.x - dialogWidth - 5 >= screenInsets.left) {
            x = cpLoc.x - dialogWidth - 5;
        } else {
            x = cpLoc.x + cpWidth + 5;
        }
        y = Math.max(screenTop, Math.min(cpLoc.y, screenSize.height - screenInsets.bottom - dialogHeight));
        dialog.setLocation(x, y);
        dialog.setVisible(true);
    }

    private void openShop() {
        int maxSlots = metaProgress.getMaxHamsterSlots();
        ShopDialog.showAndBuy(money, hamsters.size(), maxSlots,
                foodInventory, achievementManager.accessoriesBought, hamsterPurchaseCount,
                new ShopDialog.ShopCallback() {
                    @Override
                    public void onShopClosed(ShopDialog.ShopResult shopResult) {
                        // Deduct money spent on food/accessories
                        if (shopResult.totalSpent > 0) {
                            spendMoney(shopResult.totalSpent);
                        }

                        // Track new accessories globally
                        for (String accName : shopResult.newAccessories) {
                            achievementManager.accessoriesBought.add(accName);
                            for (Hamster h : hamsters) {
                                h.getOwnedAccessories().add(accName);
                            }
                        }

                        // Handle hamster purchase
                        if (shopResult.boughtHamster) {
                            hamsterPurchaseCount++;
                            hamstersRaised++;
                            statistics.totalHamstersRaised++;

                            HamsterColor[] colors = HamsterColor.values();
                            HamsterColor purchased = colors[random.nextInt(colors.length)];
                            achievementManager.colorsSeen.add(purchased.name());

                            String name = JOptionPane.showInputDialog(controlPanel,
                                    purchased.getDisplayName() + " \uD584\uC2A4\uD130\uAC00 \uD0DC\uC5B4\uB0AC\uC2B5\uB2C8\uB2E4!\n\uC774\uB984\uC744 \uC785\uB825\uD558\uC138\uC694:",
                                    "\uC0C8 \uD584\uC2A4\uD130");
                            if (name == null || name.trim().isEmpty()) {
                                name = purchased.getDisplayName() + " \uD584\uC2A4\uD130";
                            }

                            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                            int groundY = hamsterManager.getGroundY(screenSize);

                            Hamster h = hamsterManager.createHamster(screenSize.width, purchased, name.trim());
                            hamsterManager.registerWithAchievements(h, achievementManager);
                            hamsters.add(h);

                            int offsetX = (hamsters.size() - 1) * 80;
                            HamsterWindow w = new HamsterWindow(h);
                            w.setContextMenuCallback(contextMenuCallback);
                            w.setLocation(screenSize.width / 2 - 40 + offsetX, groundY);
                            hamsterWindows.add(w);
                            applySentBackState(w);

                            controlPanel.rebuild(hamsters);
                        }

                        autoSave();
                    }
                });
    }

    private void killSingleHamster(Hamster target) {
        if (target == null || target.isDead()) return;
        int confirm = JOptionPane.showConfirmDialog(controlPanel,
                target.getName() + "\uC744(\uB97C) \uBCF4\uB0B4\uC2DC\uACA0\uC2B5\uB2C8\uAE4C?",
                "\uD584\uC2A4\uD130 \uBCF4\uB0B4\uAE30", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        target.kill();
        // checkDeaths in next gameLoop tick will handle the rest
    }

    public void addMoney(int amount, Hamster source) {
        int actual;
        if (source != null) {
            actual = (int)(amount * source.getCoinMultiplier());
        } else {
            actual = amount;
        }
        money += actual;
        if (money < 0) money = 0;
        if (money > 999_999_999) money = 999_999_999;
        if (actual > 0) {
            statistics.totalCoinsEarned += actual;
        }
    }

    public void spendMoney(int amount) {
        money -= amount;
        if (money < 0) money = 0;
        statistics.totalCoinsSpent += amount;
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

    private void toggleControlPanel() {
        if (controlPanel == null) return;
        controlPanel.setVisible(!controlPanel.isVisible());
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

    private void sendBackAllWindows() {
        sentBack = !sentBack;
        if (sentBack) {
            for (HamsterWindow w : hamsterWindows) { w.setAlwaysOnTop(false); w.toBack(); }
            for (PoopWindow pw : poopWindows) { pw.setAlwaysOnTop(false); pw.toBack(); }
            controlPanel.setAlwaysOnTop(false);
            controlPanel.toBack();
        } else {
            for (HamsterWindow w : hamsterWindows) { w.setAlwaysOnTop(true); w.toFront(); }
            for (PoopWindow pw : poopWindows) { pw.setAlwaysOnTop(true); pw.toFront(); }
            controlPanel.setAlwaysOnTop(true);
            controlPanel.toFront();
        }
    }

    private void killAllHamsters() {
        if (hamsters.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(controlPanel,
                "\uC815\uB9D0\uB85C \uBAA8\uB4E0 \uD584\uC2A4\uD130\uB97C \uBCF4\uB0B4\uC2DC\uACA0\uC2B5\uB2C8\uAE4C?\n\uAC8C\uC784\uC774 \uC989\uC2DC \uC885\uB8CC\uB429\uB2C8\uB2E4.",
                "\uC804\uCCB4 \uC0AD\uC81C", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        for (Hamster h : hamsters) {
            h.kill();
        }
        // checkDeaths in next gameLoop tick will handle the rest
    }

    private void gatherAllHamsters() {
        if (hamsterWindows.isEmpty()) return;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());

        // Position above the system tray (bottom-right corner, above taskbar)
        int taskbarHeight = screenInsets.bottom;
        int baseX = screenSize.width;
        int baseY = screenSize.height - taskbarHeight;

        for (int i = 0; i < hamsterWindows.size(); i++) {
            HamsterWindow w = hamsterWindows.get(i);
            int wWidth = w.getWidth();
            int wHeight = w.getHeight();
            int col = i % 3;
            int row = i / 3;
            int x = baseX - wWidth - col * 85;
            int y = baseY - wHeight - row * 105;
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            w.setLocation(x, y);
        }
    }

    private void freezeAllHamsters() {
        allFrozen = !allFrozen;
        for (Hamster h : hamsters) {
            h.setFrozen(allFrozen);
        }
    }

    private void tryInteraction() {
        if (hamsterWindows.size() < 2) return;
        for (int i = 0; i < hamsterWindows.size(); i++) {
            for (int j = i + 1; j < hamsterWindows.size(); j++) {
                Hamster a = hamsters.get(i);
                Hamster b = hamsters.get(j);
                if (a.isDead() || b.isDead()) continue;
                if (!a.canInteract() || !b.canInteract()) continue;
                if (HamsterInteraction.areClose(hamsterWindows.get(i), hamsterWindows.get(j))) {
                    HamsterInteraction.Type type = HamsterInteraction.pickRandom(random);
                    String result = HamsterInteraction.interact(a, b, type);
                    a.startInteractionCooldown();
                    b.startInteractionCooldown();
                    statistics.totalInteractions++;
                    achievementManager.totalInteractions++;

                    JOptionPane.showMessageDialog(controlPanel, result,
                            "\uC0C1\uD638\uC791\uC6A9!", JOptionPane.INFORMATION_MESSAGE);
                    return; // only one interaction per check
                }
            }
        }
    }

    private void checkAchievements() {
        int maxGen = 0;
        for (Hamster h : hamsters) {
            if (h.getGeneration() > maxGen) maxGen = h.getGeneration();
        }
        if (maxGen > statistics.maxGenerationReached) {
            statistics.maxGenerationReached = maxGen;
        }

        java.util.List<Achievement> newAchievements = achievementManager.checkAndUnlock(
                statistics.totalHamstersRaised,
                statistics.maxGenerationReached,
                statistics.totalCoinsEarned,
                statistics.longestLifespanDays,
                hamsters
        );

        if (!newAchievements.isEmpty()) {
            achievementManager.save();
        }

        for (Achievement ach : newAchievements) {
            String reward;
            if (ach.getRewardType() == Achievement.RewardType.COINS) {
                addMoney(ach.getRewardAmount(), null);
                reward = ach.getRewardAmount() + " \uCF54\uC778";
            } else {
                metaProgress.addSeeds(ach.getRewardAmount());
                reward = ach.getRewardAmount() + " \uD574\uBC14\uB77C\uAE30\uC528";
            }
            JOptionPane.showMessageDialog(controlPanel,
                    "\uD83C\uDFC6 \uC5C5\uC801 \uD574\uAE08!\n\n" + ach.getDisplayName() + "\n" + ach.getDescription()
                            + "\n\uBCF4\uC0C1: " + reward,
                    "\uC5C5\uC801", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 2.0 callback methods for ControlPanel
    private void onFeedWithFood(Hamster h) {
        if (foodInventory == null || foodInventory.isEmpty()) {
            h.feed();
            statistics.totalFeedActions++;
            return;
        }
        java.util.Map<FoodItem, Integer> items = foodInventory.getAllItems();
        if (items.isEmpty()) {
            h.feed();
            statistics.totalFeedActions++;
            return;
        }

        // Build available food list
        final java.util.List<FoodItem> available = new java.util.ArrayList<>();
        for (java.util.Map.Entry<FoodItem, Integer> entry : items.entrySet()) {
            if (entry.getValue() > 0) available.add(entry.getKey());
        }
        if (available.isEmpty()) {
            h.feed();
            statistics.totalFeedActions++;
            return;
        }

        // Configure tooltips for long display
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        int origInitial = ttm.getInitialDelay();
        int origDismiss = ttm.getDismissDelay();
        ttm.setInitialDelay(200);
        ttm.setDismissDelay(30000);

        // Create inventory-style popup dialog
        if (activeFeedPopup != null && activeFeedPopup.isVisible()) {
            activeFeedPopup.toFront();
            return;
        }
        final JDialog popup = new JDialog(controlPanel, h.getName() + " \uBC25\uC8FC\uAE30", false);
        activeFeedPopup = popup;
        popup.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { activeFeedPopup = null; }
        });
        popup.setUndecorated(true);
        popup.setAlwaysOnTop(true);
        UIHelper.addEscapeClose(popup);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(255, 248, 235));
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 150, 100), 2),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        // Title bar
        JLabel title = new JLabel(ControlPanel.wrapEmoji("\uD83C\uDF7D\uFE0F " + h.getName() + "\uC758 \uBA39\uC774"));
        title.setFont(new Font("Noto Sans KR", Font.BOLD, 12));
        title.setForeground(new Color(80, 50, 20));
        title.setBorder(BorderFactory.createEmptyBorder(0, 4, 6, 0));
        outer.add(title, BorderLayout.NORTH);

        // Grid of food slots
        int cols = 5;
        int rows = (FoodItem.values().length + cols - 1) / cols;
        JPanel grid = new JPanel(new GridLayout(rows, cols, 3, 3));
        grid.setOpaque(false);

        final int SLOT_SIZE = 52;
        final Color slotActive = new Color(255, 250, 230);
        final Color slotEmpty = new Color(220, 215, 200);
        final Color borderActive = new Color(200, 170, 100);
        final Color borderEmpty = new Color(180, 175, 165);
        for (final FoodItem food : FoodItem.values()) {
            final int count = foodInventory.getCount(food);
            JPanel slot = new JPanel(new BorderLayout());
            slot.setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
            slot.setBackground(count > 0 ? slotActive : slotEmpty);
            slot.setBorder(BorderFactory.createLineBorder(
                    count > 0 ? borderActive : borderEmpty, 1));

            // Emoji icon centered
            JLabel emojiLabel = new JLabel(ControlPanel.wrapEmoji(food.getEmoji()), SwingConstants.CENTER);
            emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            if (count <= 0) emojiLabel.setEnabled(false);
            slot.add(emojiLabel, BorderLayout.CENTER);

            // Count label at bottom-right
            if (count > 0) {
                JLabel countLabel = new JLabel(String.valueOf(count));
                countLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 10));
                countLabel.setForeground(new Color(100, 70, 30));
                countLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                countLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 3));
                slot.add(countLabel, BorderLayout.SOUTH);
            }

            // Tooltip with description
            String tip = "<html><b>" + food.getDisplayName() + "</b><br>"
                    + food.getDescription() + "<br>"
                    + "<font color='#448844'>" + food.getEffectText() + "</font>";
            if (count > 0) tip += "<br>\uBCF4\uC720: " + count + "\uAC1C";
            else tip += "<br><font color='#CC4444'>\uC5C6\uC74C</font>";
            tip += "</html>";
            slot.setToolTipText(tip);

            // Click to feed
            if (count > 0) {
                slot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                slot.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        foodInventory.remove(food);
                        h.feed(food);
                        statistics.totalFeedActions++;
                        achievementManager.foodsTried.add(food.name());
                        popup.dispose();
                    }
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        slot.setBackground(new Color(255, 240, 190));
                        slot.setBorder(BorderFactory.createLineBorder(new Color(230, 180, 60), 2));
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        slot.setBackground(slotActive);
                        slot.setBorder(BorderFactory.createLineBorder(borderActive, 1));
                    }
                });
            }

            grid.add(slot);
        }

        // Wrap grid to keep cells square
        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gridWrapper.setOpaque(false);
        gridWrapper.add(grid);
        outer.add(gridWrapper, BorderLayout.CENTER);

        // Close hint
        JLabel hint = new JLabel("ESC: \uB2EB\uAE30", SwingConstants.CENTER);
        hint.setFont(new Font("Noto Sans KR", Font.PLAIN, 10));
        hint.setForeground(new Color(150, 130, 100));
        hint.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        outer.add(hint, BorderLayout.SOUTH);

        popup.setContentPane(outer);
        popup.getRootPane().registerKeyboardAction(
                e -> popup.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        popup.pack();
        popup.setLocationRelativeTo(controlPanel);
        popup.setVisible(true);

        // Restore tooltip settings
        ttm.setInitialDelay(origInitial);
        ttm.setDismissDelay(origDismiss);
    }

    private void onShowAchievements_() {
        AchievementDialog.show(controlPanel, achievementManager);
    }

    private void onShowJournal_() {
        JournalDialog.show(controlPanel, journal);
    }

    private void onShowStatistics_() {
        StatisticsDialog.show(controlPanel, statistics);
    }

    private void onEquipAccessory_(Hamster h) {
        java.util.Set<String> owned = h.getOwnedAccessories();
        if (owned.isEmpty()) {
            JOptionPane.showMessageDialog(controlPanel,
                    "\uBCF4\uC720 \uC911\uC778 \uC545\uC138\uC11C\uB9AC\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4.\n\uC0C1\uC810\uC5D0\uC11C \uAD6C\uB9E4\uD574\uC8FC\uC138\uC694!",
                    "\uCE58\uC7A5", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Configure tooltips
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        int origInitial = ttm.getInitialDelay();
        int origDismiss = ttm.getDismissDelay();
        ttm.setInitialDelay(200);
        ttm.setDismissDelay(30000);

        if (activeEquipPopup != null && activeEquipPopup.isVisible()) {
            activeEquipPopup.toFront();
            return;
        }
        final JDialog popup = new JDialog(controlPanel, h.getName() + " \uCE58\uC7A5", false);
        activeEquipPopup = popup;
        popup.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { activeEquipPopup = null; }
        });
        popup.setUndecorated(true);
        popup.setAlwaysOnTop(true);
        UIHelper.addEscapeClose(popup);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(255, 248, 235));
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 150, 100), 2),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        JLabel title = new JLabel(ControlPanel.wrapEmoji("\uD83C\uDFA8 " + h.getName() + "\uC758 \uCE58\uC7A5"));
        title.setFont(new Font("Noto Sans KR", Font.BOLD, 12));
        title.setForeground(new Color(80, 50, 20));
        title.setBorder(BorderFactory.createEmptyBorder(0, 4, 6, 0));
        outer.add(title, BorderLayout.NORTH);

        // Build grid of owned accessories
        java.util.List<Accessory> ownedList = new java.util.ArrayList<>();
        for (Accessory acc : Accessory.values()) {
            if (owned.contains(acc.name())) {
                ownedList.add(acc);
            }
        }

        int cols = 5;
        int rows = (ownedList.size() + cols - 1) / cols;
        JPanel grid = new JPanel(new GridLayout(Math.max(1, rows), cols, 4, 4));
        grid.setOpaque(false);

        final int SLOT_SIZE = 56;
        final Color slotNormal = new Color(255, 250, 230);
        final Color borderNormal = new Color(200, 170, 100);
        final Color slotEquipped = new Color(255, 245, 200);
        final Color borderEquipped = new Color(218, 165, 32);

        for (final Accessory acc : ownedList) {
            final boolean equipped = h.getEquippedAccessories().contains(acc);

            final JPanel slot = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    java.awt.image.BufferedImage icon = ItemIcon.getAccessoryIcon(acc);
                    int x = (getWidth() - icon.getWidth()) / 2;
                    int y = 2;
                    g2.drawImage(icon, x, y, null);
                }
            };
            slot.setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));

            if (equipped) {
                slot.setBackground(slotEquipped);
                slot.setBorder(BorderFactory.createLineBorder(borderEquipped, 2));
            } else {
                slot.setBackground(slotNormal);
                slot.setBorder(BorderFactory.createLineBorder(borderNormal, 1));
            }

            // Bottom label
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);
            if (equipped) {
                JLabel eqLabel = new JLabel("\uC7A5\uCC29", SwingConstants.CENTER);
                eqLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 9));
                eqLabel.setForeground(borderEquipped);
                eqLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
                bottomPanel.add(eqLabel, BorderLayout.CENTER);
            } else {
                String slotName = com.hamster.ui.UIHelper.getSlotDisplayName(acc.getSlot());
                JLabel nameLabel = new JLabel(slotName, SwingConstants.CENTER);
                nameLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 8));
                nameLabel.setForeground(new Color(120, 100, 60));
                nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
                bottomPanel.add(nameLabel, BorderLayout.CENTER);
            }
            slot.add(bottomPanel, BorderLayout.SOUTH);

            // Tooltip
            String coinText = String.format("\uCF54\uC778+%d%%", (int)(acc.getCoinBonus() * 100));
            String tip = "<html><b>" + acc.getDisplayName() + "</b><br>"
                    + acc.getDescription() + "<br>"
                    + "<font color='#448844'>" + coinText + "</font>";
            if (equipped) tip += "<br><b><font color='#B8860B'>\uC7A5\uCC29\uC911</font></b>";
            tip += "<br>\uD074\uB9AD: " + (equipped ? "\uD574\uC81C" : "\uC7A5\uCC29");
            tip += "</html>";
            slot.setToolTipText(tip);

            // Click to toggle equip
            slot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            slot.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (h.getEquippedAccessories().contains(acc)) {
                        h.unequipAccessory(acc);
                    } else {
                        h.equipAccessory(acc);
                    }
                    popup.dispose();
                    // Re-open to refresh state
                    onEquipAccessory_(h);
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    slot.setBackground(new Color(255, 240, 190));
                    slot.setBorder(BorderFactory.createLineBorder(new Color(230, 180, 60), 2));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (h.getEquippedAccessories().contains(acc)) {
                        slot.setBackground(slotEquipped);
                        slot.setBorder(BorderFactory.createLineBorder(borderEquipped, 2));
                    } else {
                        slot.setBackground(slotNormal);
                        slot.setBorder(BorderFactory.createLineBorder(borderNormal, 1));
                    }
                }
            });

            grid.add(slot);
        }

        // Fill empty slots
        int remainder = (cols - (ownedList.size() % cols)) % cols;
        for (int i = 0; i < remainder; i++) {
            JPanel empty = new JPanel();
            empty.setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
            empty.setBackground(new Color(235, 230, 220));
            empty.setBorder(BorderFactory.createLineBorder(new Color(190, 185, 175), 1));
            grid.add(empty);
        }

        // Wrap grid to keep cells square
        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gridWrapper.setOpaque(false);
        gridWrapper.add(grid);
        outer.add(gridWrapper, BorderLayout.CENTER);

        // Close hint
        JLabel hint = new JLabel("ESC: \uB2EB\uAE30", SwingConstants.CENTER);
        hint.setFont(new Font("Noto Sans KR", Font.PLAIN, 10));
        hint.setForeground(new Color(150, 130, 100));
        hint.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        outer.add(hint, BorderLayout.SOUTH);

        popup.setContentPane(outer);
        popup.getRootPane().registerKeyboardAction(
                e -> popup.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        popup.pack();
        popup.setLocationRelativeTo(controlPanel);
        popup.setVisible(true);

        // Restore tooltip settings
        ttm.setInitialDelay(origInitial);
        ttm.setDismissDelay(origDismiss);

        autoSave();
    }

    // Accessor for food inventory (used by shop)
    public FoodInventory getFoodInventory() { return foodInventory; }

    private void onSettingsSaved(Settings newSettings) {
        this.settings = newSettings;
        hotkeyManager.updateHotkeys(newSettings);
    }

    private float currentOpacity = 1.0f;

    private void setAllWindowsOpacity(float opacity) {
        currentOpacity = opacity;
        for (HamsterWindow w : hamsterWindows) w.setOpacity(opacity);
        for (PoopWindow pw : poopWindows) pw.setOpacity(opacity);
        controlPanel.setOpacity(opacity);
    }

    private void applySentBackState(java.awt.Window w) {
        if (sentBack) {
            w.setAlwaysOnTop(false);
            w.toBack();
        }
        if (currentOpacity < 1.0f) {
            w.setOpacity(currentOpacity);
        }
    }

    private void savePendingLegacy() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(LEGACY_FILE);
        if (pendingLegacy == null) {
            file.delete();
            return;
        }
        Properties props = new Properties();
        props.setProperty("generation", String.valueOf(pendingLegacy[0]));
        props.setProperty("hungerBonus", String.valueOf(pendingLegacy[1]));
        props.setProperty("happinessBonus", String.valueOf(pendingLegacy[2]));
        props.setProperty("energyBonus", String.valueOf(pendingLegacy[3]));
        props.setProperty("lifespanBonus", String.valueOf(pendingLegacy[4]));
        props.setProperty("maxStatBonus", String.valueOf(pendingLegacy[5]));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "Pending Legacy");
        } catch (IOException e) {
            GameLogger.error("Failed to save pending legacy", e);
        }
    }

    private void loadPendingLegacy() {
        File file = new File(LEGACY_FILE);
        if (!file.exists()) return;
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException e) {
            return;
        }
        pendingLegacy = new int[] {
            Integer.parseInt(props.getProperty("generation", "1")),
            Integer.parseInt(props.getProperty("hungerBonus", "0")),
            Integer.parseInt(props.getProperty("happinessBonus", "0")),
            Integer.parseInt(props.getProperty("energyBonus", "0")),
            Integer.parseInt(props.getProperty("lifespanBonus", "0")),
            Integer.parseInt(props.getProperty("maxStatBonus", "0"))
        };
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

            JMenuItem sendBackItem = new JMenuItem("\uB4A4\uB85C \uBCF4\uB0B4\uAE30/\uC55E\uC73C\uB85C (ALT+W)");
            sendBackItem.addActionListener(e -> sendBackAllWindows());

            JMenuItem exitItem = new JMenuItem("\uC885\uB8CC");
            exitItem.addActionListener(e -> {
                if (hotkeyManager != null) hotkeyManager.stop();
                System.exit(0);
            });

            popup.add(showItem);
            popup.add(toggleItem);
            popup.add(sendBackItem);
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
            GameLogger.error("Failed to setup tray icon", e);
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
