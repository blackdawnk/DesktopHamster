package com.hamster.ui;
import com.hamster.model.Buff;
import com.hamster.model.GameState;
import com.hamster.model.GameStatistics;
import com.hamster.model.Hamster;
import com.hamster.model.Poop;
import com.hamster.model.Settings;
import com.hamster.render.HamsterIcon;
import com.hamster.system.GlobalHotkeyManager;
import com.hamster.system.HamsterJournal;
import com.hamster.system.MetaProgress;
import com.hamster.system.SaveManager;

import javax.swing.*;
import java.awt.*;

public class StartDialog extends JDialog {

    public enum Result {
        NEW_GAME, LOAD_GAME, EXIT
    }

    private Result result = Result.EXIT;
    private GameState loadedState = null;
    private final MetaProgress metaProgress;
    private Settings settings;
    private GlobalHotkeyManager hotkeyManager;
    private SettingsDialog.OnSave onSettingsSaved;
    private static JFrame ownerFrame;

    private static String wrapEmoji(String text) {
        boolean hasEmoji = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isHighSurrogate(c) || c == '\u2B50' || c == '\u2600' || c == '\u2699') {
                hasEmoji = true;
                break;
            }
        }
        if (!hasEmoji) return text;
        StringBuilder sb = new StringBuilder("<html><nobr>");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isHighSurrogate(c) && i + 1 < text.length()) {
                sb.append("<span style='font-family:Segoe UI Emoji'>")
                  .append(c).append(text.charAt(i + 1)).append("</span>");
                i++;
            } else if (c == '\u2B50' || c == '\u2600' || c == '\u2699') {
                sb.append("<span style='font-family:Segoe UI Emoji'>").append(c);
                if (i + 1 < text.length() && text.charAt(i + 1) == '\uFE0F') {
                    sb.append(text.charAt(++i));
                }
                sb.append("</span>");
            } else {
                sb.append(c);
            }
        }
        sb.append("</nobr></html>");
        return sb.toString();
    }

    private static JFrame getOwnerFrame() {
        if (ownerFrame == null) {
            ownerFrame = new JFrame("\uB370\uC2A4\uD06C\uD0D1 \uD584\uC2A4\uD130");
            ownerFrame.setUndecorated(true);
            ownerFrame.setSize(0, 0);
            ownerFrame.setLocationRelativeTo(null);
            ownerFrame.setIconImages(HamsterIcon.createIcons());
        }
        return ownerFrame;
    }

    private StartDialog(MetaProgress metaProgress, Settings settings, GlobalHotkeyManager hotkeyManager, SettingsDialog.OnSave onSettingsSaved) {
        super(getOwnerFrame(), "\uB370\uC2A4\uD06C\uD0D1 \uD584\uC2A4\uD130", true);
        this.metaProgress = metaProgress;
        this.settings = settings;
        this.hotkeyManager = hotkeyManager;
        this.onSettingsSaved = onSettingsSaved;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setIconImages(HamsterIcon.createIcons());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(new Color(255, 250, 240));

        // Title
        JLabel titleLabel = new JLabel(wrapEmoji("\uD83D\uDC39 \uB370\uC2A4\uD06C\uD0D1 \uD584\uC2A4\uD130"));
        titleLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 22));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Sunflower seeds display
        JLabel seedLabel = new JLabel(wrapEmoji("\uD83C\uDF3B \uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C"));
        seedLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 14));
        seedLabel.setForeground(new Color(180, 140, 20));
        seedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(seedLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // New Game button
        JButton newGameBtn = createMenuButton("\uC0C8 \uAC8C\uC784");
        newGameBtn.addActionListener(e -> {
            result = Result.NEW_GAME;
            dispose();
        });
        mainPanel.add(newGameBtn);
        mainPanel.add(Box.createVerticalStrut(10));

        // Continue Game button (only enabled when auto-save exists)
        JButton loadBtn = createMenuButton("\uC774\uC5B4\uD558\uAE30");
        loadBtn.setEnabled(SaveManager.autoSaveExists());
        loadBtn.addActionListener(e -> {
            GameState state = SaveManager.loadAuto();
            if (state != null) {
                loadedState = state;
                result = Result.LOAD_GAME;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "\uC790\uB3D9\uC800\uC7A5 \uD30C\uC77C\uC744 \uC77D\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.");
            }
        });
        mainPanel.add(loadBtn);

        // Show save summary if available
        String saveSummary = SaveManager.getAutoSaveSummary();
        if (saveSummary != null) {
            JLabel saveInfoLabel = new JLabel(saveSummary);
            saveInfoLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 11));
            saveInfoLabel.setForeground(new Color(130, 110, 80));
            saveInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(saveInfoLabel);
        }
        mainPanel.add(Box.createVerticalStrut(10));

        // Upgrade button
        JButton upgradeBtn = createMenuButton(wrapEmoji("\uD83C\uDF3B \uC5C5\uADF8\uB808\uC774\uB4DC"));
        upgradeBtn.setBackground(new Color(255, 240, 180));
        upgradeBtn.addActionListener(e -> showUpgradeShop(seedLabel));
        mainPanel.add(upgradeBtn);
        mainPanel.add(Box.createVerticalStrut(10));

        // Journal button
        JButton journalBtn = createMenuButton(wrapEmoji("\uD83D\uDCD6 \uB3C4\uAC10"));
        journalBtn.setBackground(new Color(220, 240, 255));
        journalBtn.addActionListener(e -> {
            HamsterJournal journal = HamsterJournal.load();
            JournalDialog.show(this, journal);
        });
        mainPanel.add(journalBtn);
        mainPanel.add(Box.createVerticalStrut(10));

        // Statistics button
        JButton statsBtn = createMenuButton(wrapEmoji("\uD83D\uDCCA \uD1B5\uACC4"));
        statsBtn.setBackground(new Color(230, 255, 230));
        statsBtn.addActionListener(e -> {
            GameStatistics stats = GameStatistics.load();
            StatisticsDialog.show(this, stats);
        });
        mainPanel.add(statsBtn);
        mainPanel.add(Box.createVerticalStrut(10));

        // Exit button
        JButton exitBtn = createMenuButton("\uC885\uB8CC");
        exitBtn.addActionListener(e -> {
            result = Result.EXIT;
            dispose();
        });
        mainPanel.add(exitBtn);
        mainPanel.add(Box.createVerticalStrut(15));

        // Settings button (gear icon) at bottom-right
        JPanel settingsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        settingsRow.setOpaque(false);
        settingsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton settingsBtn = new JButton(wrapEmoji("\u2699 \uC124\uC815"));
        settingsBtn.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
        settingsBtn.setBackground(new Color(230, 225, 215));
        settingsBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 170, 150), 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        settingsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settingsBtn.addActionListener(e -> {
            if (this.settings != null) {
                SettingsDialog.show(this, this.settings, this.hotkeyManager, this.onSettingsSaved);
            }
        });
        settingsRow.add(settingsBtn);
        mainPanel.add(settingsRow);

        setContentPane(mainPanel);
        pack();
        if (getWidth() < 400) setSize(400, getHeight());
        setLocationRelativeTo(null);
    }

    private void showUpgradeShop(JLabel seedLabel) {
        JDialog shopDialog = new JDialog(this, "\uC5C5\uADF8\uB808\uC774\uB4DC", true);
        shopDialog.setResizable(false);
        shopDialog.setIconImages(HamsterIcon.createIcons());

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        outerPanel.setBackground(new Color(255, 250, 240));

        // Header (outside scroll)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 5, 30));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(wrapEmoji("\uD83C\uDF3B \uC5C5\uADF8\uB808\uC774\uB4DC"));
        titleLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));

        JLabel shopSeedLabel = new JLabel("\uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C");
        shopSeedLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 14));
        shopSeedLabel.setForeground(new Color(180, 140, 20));
        shopSeedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(shopSeedLabel);
        outerPanel.add(headerPanel);

        // Scrollable upgrade list
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        panel.setBackground(new Color(255, 250, 240));

        Runnable refreshShop = () -> {
            shopSeedLabel.setText("\uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C");
            seedLabel.setText(wrapEmoji("\uD83C\uDF3B \uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C"));
            shopDialog.dispose();
            showUpgradeShop(seedLabel);
        };
        String errMsg = "\uD574\uBC14\uB77C\uAE30\uC528\uAC00 \uBD80\uC871\uD569\uB2C8\uB2E4!";

        // 1. Lifespan
        addUpgradeRow(panel, shopDialog, "\uC218\uBA85 \uBC94\uC704",
                "Lv." + metaProgress.lifespanLevel + "/" + MetaProgress.MAX_LIFESPAN_LEVEL,
                metaProgress.getMinLifespanDays() + "~" + metaProgress.getMaxLifespanDays() + "\uC77C",
                metaProgress.canUpgradeLifespan() ?
                        "\u2192 " + (metaProgress.getMinLifespanDays() + 1) + "~" + (metaProgress.getMaxLifespanDays() + 1) + "\uC77C" : "MAX",
                metaProgress.getLifespanCost(), metaProgress.canUpgradeLifespan(),
                () -> { if (metaProgress.upgradeLifespan()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 2. Aging speed
        addUpgradeRow(panel, shopDialog, "\uB178\uD654 \uC18D\uB3C4",
                "Lv." + metaProgress.agingLevel + "/" + MetaProgress.MAX_AGING_LEVEL,
                String.format("%.2fx", metaProgress.getAgingSpeed()),
                metaProgress.canUpgradeAging() ?
                        "\u2192 " + String.format("%.2fx", Math.max(0.5, metaProgress.getAgingSpeed() - 0.05)) : "MAX",
                metaProgress.getAgingCost(), metaProgress.canUpgradeAging(),
                () -> { if (metaProgress.upgradeAging()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 3. Action gain
        addUpgradeRow(panel, shopDialog, "\uD589\uB3D9 \uD6A8\uACFC",
                "Lv." + metaProgress.actionGainLevel + "/" + MetaProgress.MAX_ACTION_GAIN_LEVEL,
                "+" + metaProgress.getActionGain(),
                metaProgress.canUpgradeActionGain() ? "\u2192 +" + (metaProgress.getActionGain() + 1) : "MAX",
                metaProgress.getActionGainCost(), metaProgress.canUpgradeActionGain(),
                () -> { if (metaProgress.upgradeActionGain()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 4. Drain amount
        addUpgradeRow(panel, shopDialog, "\uAC10\uC18C\uB7C9",
                "Lv." + metaProgress.drainLevel + "/" + MetaProgress.MAX_DRAIN_LEVEL,
                String.format("%.2fx", metaProgress.getDrainMultiplier()),
                metaProgress.canUpgradeDrain() ?
                        "\u2192 " + String.format("%.2fx", Math.max(0.2, metaProgress.getDrainMultiplier() - 0.02)) : "MAX",
                metaProgress.getDrainCost(), metaProgress.canUpgradeDrain(),
                () -> { if (metaProgress.upgradeDrain()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 5. Drain interval
        addUpgradeRow(panel, shopDialog, "\uAC10\uC18C \uC8FC\uAE30",
                "Lv." + metaProgress.drainIntervalLevel + "/" + MetaProgress.MAX_DRAIN_INTERVAL_LEVEL,
                String.format("%.1f\uCD08", metaProgress.getDrainInterval() / 30.0),
                metaProgress.canUpgradeDrainInterval() ?
                        "\u2192 " + String.format("%.1f\uCD08", (metaProgress.getDrainInterval() + 15) / 30.0) : "MAX",
                metaProgress.getDrainIntervalCost(), metaProgress.canUpgradeDrainInterval(),
                () -> { if (metaProgress.upgradeDrainInterval()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 6. Hamster slot
        addUpgradeRow(panel, shopDialog, "\uD584\uC2A4\uD130 \uC2AC\uB86F",
                "Lv." + metaProgress.hamsterSlotLevel + "/" + MetaProgress.MAX_HAMSTER_SLOT_LEVEL,
                metaProgress.getMaxHamsterSlots() + "\uB9C8\uB9AC",
                metaProgress.canUpgradeHamsterSlot() ? "\u2192 " + (metaProgress.getMaxHamsterSlots() + 1) + "\uB9C8\uB9AC" : "MAX",
                metaProgress.getHamsterSlotCost(), metaProgress.canUpgradeHamsterSlot(),
                () -> { if (metaProgress.upgradeHamsterSlot()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 7. Breed age
        addUpgradeRow(panel, shopDialog, "\uAD50\uBC30 \uB098\uC774",
                "Lv." + metaProgress.breedAgeLevel + "/" + MetaProgress.MAX_BREED_AGE_LEVEL,
                metaProgress.getBreedAgeDaysText(),
                metaProgress.canUpgradeBreedAge() ?
                        "\u2192 " + String.format("%.1f\uC77C", Math.max(0.1, metaProgress.getBreedAgeDays() - 0.05)) : "MAX",
                metaProgress.getBreedAgeCost(), metaProgress.canUpgradeBreedAge(),
                () -> { if (metaProgress.upgradeBreedAge()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 8. Coin bonus
        addUpgradeRow(panel, shopDialog, "\uCF54\uC778 \uD68D\uB4DD\uB7C9",
                "Lv." + metaProgress.coinBonusLevel + "/" + MetaProgress.MAX_COIN_BONUS_LEVEL,
                "+" + metaProgress.getCoinBonus(),
                metaProgress.canUpgradeCoinBonus() ? "\u2192 +" + (metaProgress.getCoinBonus() + 1) : "MAX",
                metaProgress.getCoinBonusCost(), metaProgress.canUpgradeCoinBonus(),
                () -> { if (metaProgress.upgradeCoinBonus()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 9. Poop frequency
        addUpgradeRow(panel, shopDialog, "\uC751\uAC00 \uBE48\uB3C4",
                "Lv." + metaProgress.poopFreqLevel + "/" + MetaProgress.MAX_POOP_FREQ_LEVEL,
                (int)(metaProgress.getPoopChanceMultiplier() * 100) + "%",
                metaProgress.canUpgradePoopFreq() ?
                        "\u2192 " + (int)(Math.max(0.1, metaProgress.getPoopChanceMultiplier() - 0.02) * 100) + "%" : "MAX",
                metaProgress.getPoopFreqCost(), metaProgress.canUpgradePoopFreq(),
                () -> { if (metaProgress.upgradePoopFreq()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 10. Poop penalty
        addUpgradeRow(panel, shopDialog, "\uC751\uAC00 \uD398\uB110\uD2F0",
                "Lv." + metaProgress.poopPenaltyLevel + "/" + MetaProgress.MAX_POOP_PENALTY_LEVEL,
                (int)(metaProgress.getPoopPenaltyMultiplier() * 100) + "%",
                metaProgress.canUpgradePoopPenalty() ?
                        "\u2192 " + (int)(Math.max(0.1, metaProgress.getPoopPenaltyMultiplier() - 0.03) * 100) + "%" : "MAX",
                metaProgress.getPoopPenaltyCost(), metaProgress.canUpgradePoopPenalty(),
                () -> { if (metaProgress.upgradePoopPenalty()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 11. Event interval
        addUpgradeRow(panel, shopDialog, "\uC774\uBCA4\uD2B8 \uC8FC\uAE30",
                "Lv." + metaProgress.eventIntervalLevel + "/" + MetaProgress.MAX_EVENT_INTERVAL_LEVEL,
                String.format("%.0f\uCD08", metaProgress.getEventInterval() / 30.0),
                metaProgress.canUpgradeEventInterval() ?
                        "\u2192 " + String.format("%.0f\uCD08", Math.max(750, metaProgress.getEventInterval() - 75) / 30.0) : "MAX",
                metaProgress.getEventIntervalCost(), metaProgress.canUpgradeEventInterval(),
                () -> { if (metaProgress.upgradeEventInterval()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 12. Buff duration
        addUpgradeRow(panel, shopDialog, "\uBC84\uD504 \uC9C0\uC18D",
                "Lv." + metaProgress.buffDurationLevel + "/" + MetaProgress.MAX_BUFF_DURATION_LEVEL,
                String.format("%.1fx", metaProgress.getBuffDurationMultiplier()),
                metaProgress.canUpgradeBuffDuration() ?
                        "\u2192 " + String.format("%.1fx", metaProgress.getBuffDurationMultiplier() + 0.1) : "MAX",
                metaProgress.getBuffDurationCost(), metaProgress.canUpgradeBuffDuration(),
                () -> { if (metaProgress.upgradeBuffDuration()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        // 13. Starting stats
        addUpgradeRow(panel, shopDialog, "\uCD08\uAE30 \uC2A4\uD0EF",
                "Lv." + metaProgress.startingStatsLevel + "/" + MetaProgress.MAX_STARTING_STATS_LEVEL,
                String.valueOf(metaProgress.getStartingStats()),
                metaProgress.canUpgradeStartingStats() ? "\u2192 " + (metaProgress.getStartingStats() + 1) : "MAX",
                metaProgress.getStartingStatsCost(), metaProgress.canUpgradeStartingStats(),
                () -> { if (metaProgress.upgradeStartingStats()) refreshShop.run(); else JOptionPane.showMessageDialog(shopDialog, errMsg); });

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(255, 250, 240));
        scrollPane.setPreferredSize(new Dimension(520, 450));
        outerPanel.add(scrollPane);

        // Close button (outside scroll)
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 30, 15, 30));
        footerPanel.setOpaque(false);
        JButton closeBtn = createMenuButton("\uB2EB\uAE30");
        closeBtn.addActionListener(e -> shopDialog.dispose());
        footerPanel.add(closeBtn);
        outerPanel.add(footerPanel);

        shopDialog.setContentPane(outerPanel);
        shopDialog.pack();
        shopDialog.setLocationRelativeTo(this);
        shopDialog.setVisible(true);
    }

    private void addUpgradeRow(JPanel panel, JDialog shopDialog, String name, String level,
                                String current, String next, int cost, boolean canUpgrade, Runnable onUpgrade) {
        panel.add(createUpgradeRow(name, level, current, next, cost, canUpgrade, onUpgrade));
        panel.add(Box.createVerticalStrut(6));
    }

    private JPanel createUpgradeRow(String name, String level, String current, String next,
                                     int cost, boolean canUpgrade, Runnable onUpgrade) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(480, 35));

        JLabel infoLabel = new JLabel(name + " " + level + "  " + current + " " + next);
        infoLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(80, 50, 20));
        infoLabel.setPreferredSize(new Dimension(340, 28));
        row.add(infoLabel);

        row.add(Box.createHorizontalGlue());

        if (canUpgrade) {
            JButton btn = new JButton(cost + "\uC528");
            btn.setFont(new Font("Noto Sans KR", Font.BOLD, 11));
            btn.setFocusPainted(false);
            btn.setBackground(new Color(255, 220, 140));
            btn.setForeground(new Color(80, 50, 20));
            btn.setPreferredSize(new Dimension(70, 28));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 160, 80), 1, true),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setEnabled(metaProgress.sunflowerSeeds >= cost);
            btn.addActionListener(e -> onUpgrade.run());
            row.add(btn);
        } else {
            JLabel maxLabel = new JLabel("MAX");
            maxLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 11));
            maxLabel.setForeground(new Color(180, 140, 20));
            row.add(maxLabel);
        }

        return row;
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Noto Sans KR", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(255, 230, 200));
        btn.setForeground(new Color(80, 50, 20));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(320, 40));
        btn.setPreferredSize(new Dimension(320, 40));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 160, 100), 1, true),
                BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public Result getResult() { return result; }
    public GameState getLoadedState() { return loadedState; }

    public static StartDialog showAndWait(MetaProgress metaProgress, GlobalHotkeyManager hotkey,
                                             Settings settings, SettingsDialog.OnSave onSettingsSaved) {
        JFrame owner = getOwnerFrame();
        owner.setVisible(true);
        StartDialog dialog = new StartDialog(metaProgress, settings, hotkey, onSettingsSaved);

        if (hotkey != null) {
            final Point[] savedLoc = {null};
            final boolean[] hidden = {false};
            hotkey.setCallback(() -> {
                if (!hidden[0]) {
                    savedLoc[0] = dialog.getLocation();
                    dialog.setLocation(-32000, -32000);
                    hidden[0] = true;
                } else {
                    dialog.setLocation(savedLoc[0]);
                    dialog.toFront();
                    hidden[0] = false;
                }
            });
        }

        dialog.setVisible(true);
        owner.setVisible(false);
        return dialog;
    }
}
