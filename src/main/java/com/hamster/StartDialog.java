package com.hamster;

import javax.swing.*;
import java.awt.*;

public class StartDialog extends JDialog {

    public enum Result {
        NEW_GAME, LOAD_GAME, EXIT
    }

    private Result result = Result.EXIT;
    private GameState loadedState = null;
    private final MetaProgress metaProgress;

    private static String wrapEmoji(String text) {
        boolean hasEmoji = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isHighSurrogate(c) || c == '\u2B50' || c == '\u2600') {
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
            } else if (c == '\u2B50' || c == '\u2600') {
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

    private StartDialog(MetaProgress metaProgress) {
        super((Frame) null, "\uB370\uC2A4\uD06C\uD0D1 \uD584\uC2A4\uD130", true);
        this.metaProgress = metaProgress;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setIconImages(HamsterIcon.createIcons());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
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
        mainPanel.add(Box.createVerticalStrut(10));

        // Upgrade button
        JButton upgradeBtn = createMenuButton(wrapEmoji("\uD83C\uDF3B \uC5C5\uADF8\uB808\uC774\uB4DC"));
        upgradeBtn.setBackground(new Color(255, 240, 180));
        upgradeBtn.addActionListener(e -> showUpgradeShop(seedLabel));
        mainPanel.add(upgradeBtn);
        mainPanel.add(Box.createVerticalStrut(10));

        // Exit button
        JButton exitBtn = createMenuButton("\uC885\uB8CC");
        exitBtn.addActionListener(e -> {
            result = Result.EXIT;
            dispose();
        });
        mainPanel.add(exitBtn);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void showUpgradeShop(JLabel seedLabel) {
        JDialog shopDialog = new JDialog(this, "\uC5C5\uADF8\uB808\uC774\uB4DC", true);
        shopDialog.setResizable(false);
        shopDialog.setIconImages(HamsterIcon.createIcons());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(new Color(255, 250, 240));

        JLabel titleLabel = new JLabel(wrapEmoji("\uD83C\uDF3B \uC5C5\uADF8\uB808\uC774\uB4DC"));
        titleLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));

        JLabel shopSeedLabel = new JLabel("\uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C");
        shopSeedLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 14));
        shopSeedLabel.setForeground(new Color(180, 140, 20));
        shopSeedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(shopSeedLabel);
        panel.add(Box.createVerticalStrut(15));

        // 1. Lifespan upgrade
        panel.add(createUpgradeRow(
                "\uC218\uBA85 \uBC94\uC704",
                "Lv." + metaProgress.lifespanLevel + "/" + MetaProgress.MAX_LIFESPAN_LEVEL,
                metaProgress.getMinLifespanDays() + "~" + metaProgress.getMaxLifespanDays() + "\uC77C",
                metaProgress.canUpgradeLifespan() ?
                        "\u2192 " + (metaProgress.getMinLifespanDays() + 2) + "~" + (metaProgress.getMaxLifespanDays() + 3) + "\uC77C" : "MAX",
                metaProgress.getLifespanCost(),
                metaProgress.canUpgradeLifespan(),
                () -> {
                    if (metaProgress.upgradeLifespan()) {
                        shopSeedLabel.setText("\uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C");
                        seedLabel.setText(wrapEmoji("\uD83C\uDF3B \uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C"));
                        shopDialog.dispose();
                        showUpgradeShop(seedLabel);
                    } else {
                        JOptionPane.showMessageDialog(shopDialog, "\uD574\uBC14\uB77C\uAE30\uC528\uAC00 \uBD80\uC871\uD569\uB2C8\uB2E4!");
                    }
                }
        ));
        panel.add(Box.createVerticalStrut(8));

        // 2. Aging speed upgrade
        String agingCurrent = metaProgress.getAgingSpeed() + "\uBC30\uC18D";
        String agingNext = metaProgress.canUpgradeAging() ?
                "\u2192 " + Math.max(1, metaProgress.getAgingSpeed() - 1) + "\uBC30\uC18D" : "MAX";
        panel.add(createUpgradeRow(
                "\uB178\uD654 \uC18D\uB3C4",
                "Lv." + metaProgress.agingLevel + "/" + MetaProgress.MAX_AGING_LEVEL,
                agingCurrent,
                agingNext,
                metaProgress.getAgingCost(),
                metaProgress.canUpgradeAging(),
                () -> {
                    if (metaProgress.upgradeAging()) {
                        shopSeedLabel.setText("\uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C");
                        seedLabel.setText(wrapEmoji("\uD83C\uDF3B \uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C"));
                        shopDialog.dispose();
                        showUpgradeShop(seedLabel);
                    } else {
                        JOptionPane.showMessageDialog(shopDialog, "\uD574\uBC14\uB77C\uAE30\uC528\uAC00 \uBD80\uC871\uD569\uB2C8\uB2E4!");
                    }
                }
        ));
        panel.add(Box.createVerticalStrut(8));

        // 3. Action gain upgrade
        String actionCurrent = "+" + metaProgress.getActionGain();
        String actionNext = metaProgress.canUpgradeActionGain() ?
                "\u2192 +" + (metaProgress.getActionGain() + 1) : "MAX";
        panel.add(createUpgradeRow(
                "\uD589\uB3D9 \uD6A8\uACFC",
                "Lv." + metaProgress.actionGainLevel + "/" + MetaProgress.MAX_ACTION_GAIN_LEVEL,
                actionCurrent,
                actionNext,
                metaProgress.getActionGainCost(),
                metaProgress.canUpgradeActionGain(),
                () -> {
                    if (metaProgress.upgradeActionGain()) {
                        shopSeedLabel.setText("\uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C");
                        seedLabel.setText(wrapEmoji("\uD83C\uDF3B \uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C"));
                        shopDialog.dispose();
                        showUpgradeShop(seedLabel);
                    } else {
                        JOptionPane.showMessageDialog(shopDialog, "\uD574\uBC14\uB77C\uAE30\uC528\uAC00 \uBD80\uC871\uD569\uB2C8\uB2E4!");
                    }
                }
        ));
        panel.add(Box.createVerticalStrut(8));

        // 4. Drain reduction upgrade
        String drainCurrent = metaProgress.getDrainMultiplier() + "\uBC30";
        String drainNext = metaProgress.canUpgradeDrain() ?
                "\u2192 " + Math.max(1, metaProgress.getDrainMultiplier() - 1) + "\uBC30" : "MAX";
        panel.add(createUpgradeRow(
                "\uC2A4\uD0EF \uAC10\uC18C",
                "Lv." + metaProgress.drainLevel + "/" + MetaProgress.MAX_DRAIN_LEVEL,
                drainCurrent,
                drainNext,
                metaProgress.getDrainCost(),
                metaProgress.canUpgradeDrain(),
                () -> {
                    if (metaProgress.upgradeDrain()) {
                        shopSeedLabel.setText("\uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C");
                        seedLabel.setText(wrapEmoji("\uD83C\uDF3B \uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C"));
                        shopDialog.dispose();
                        showUpgradeShop(seedLabel);
                    } else {
                        JOptionPane.showMessageDialog(shopDialog, "\uD574\uBC14\uB77C\uAE30\uC528\uAC00 \uBD80\uC871\uD569\uB2C8\uB2E4!");
                    }
                }
        ));
        panel.add(Box.createVerticalStrut(8));

        // 5. Hamster slot upgrade
        String slotCurrent = metaProgress.getMaxHamsterSlots() + "\uB9C8\uB9AC";
        String slotNext = metaProgress.canUpgradeHamsterSlot() ?
                "\u2192 " + (metaProgress.getMaxHamsterSlots() + 1) + "\uB9C8\uB9AC" : "MAX";
        panel.add(createUpgradeRow(
                "\uD584\uC2A4\uD130 \uC2AC\uB86F",
                "Lv." + metaProgress.hamsterSlotLevel + "/" + MetaProgress.MAX_HAMSTER_SLOT_LEVEL,
                slotCurrent,
                slotNext,
                metaProgress.getHamsterSlotCost(),
                metaProgress.canUpgradeHamsterSlot(),
                () -> {
                    if (metaProgress.upgradeHamsterSlot()) {
                        shopSeedLabel.setText("\uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C");
                        seedLabel.setText(wrapEmoji("\uD83C\uDF3B \uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C"));
                        shopDialog.dispose();
                        showUpgradeShop(seedLabel);
                    } else {
                        JOptionPane.showMessageDialog(shopDialog, "\uD574\uBC14\uB77C\uAE30\uC528\uAC00 \uBD80\uC871\uD569\uB2C8\uB2E4!");
                    }
                }
        ));
        panel.add(Box.createVerticalStrut(8));

        // 6. Breed age upgrade
        String breedCurrent = metaProgress.getBreedAgeDaysText();
        String breedNext;
        if (metaProgress.canUpgradeBreedAge()) {
            switch (metaProgress.breedAgeLevel + 1) {
                case 1: breedNext = "\u2192 1.5\uC77C"; break;
                case 2: breedNext = "\u2192 1\uC77C"; break;
                default: breedNext = "MAX"; break;
            }
        } else {
            breedNext = "MAX";
        }
        panel.add(createUpgradeRow(
                "\uAD50\uBC30 \uB098\uC774",
                "Lv." + metaProgress.breedAgeLevel + "/" + MetaProgress.MAX_BREED_AGE_LEVEL,
                breedCurrent,
                breedNext,
                metaProgress.getBreedAgeCost(),
                metaProgress.canUpgradeBreedAge(),
                () -> {
                    if (metaProgress.upgradeBreedAge()) {
                        shopSeedLabel.setText("\uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C");
                        seedLabel.setText(wrapEmoji("\uD83C\uDF3B \uD574\uBC14\uB77C\uAE30\uC528: " + metaProgress.sunflowerSeeds + "\uAC1C"));
                        shopDialog.dispose();
                        showUpgradeShop(seedLabel);
                    } else {
                        JOptionPane.showMessageDialog(shopDialog, "\uD574\uBC14\uB77C\uAE30\uC528\uAC00 \uBD80\uC871\uD569\uB2C8\uB2E4!");
                    }
                }
        ));
        panel.add(Box.createVerticalStrut(15));

        // Close button
        JButton closeBtn = createMenuButton("\uB2EB\uAE30");
        closeBtn.addActionListener(e -> shopDialog.dispose());
        panel.add(closeBtn);

        shopDialog.setContentPane(panel);
        shopDialog.pack();
        shopDialog.setLocationRelativeTo(this);
        shopDialog.setVisible(true);
    }

    private JPanel createUpgradeRow(String name, String level, String current, String next,
                                     int cost, boolean canUpgrade, Runnable onUpgrade) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(400, 35));

        JLabel infoLabel = new JLabel(name + " " + level + "  " + current + " " + next);
        infoLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(80, 50, 20));
        infoLabel.setPreferredSize(new Dimension(250, 28));
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
        btn.setMaximumSize(new Dimension(280, 40));
        btn.setPreferredSize(new Dimension(280, 40));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 160, 100), 1, true),
                BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public Result getResult() { return result; }
    public GameState getLoadedState() { return loadedState; }

    public static StartDialog showAndWait(MetaProgress metaProgress) {
        StartDialog dialog = new StartDialog(metaProgress);
        dialog.setVisible(true);
        return dialog;
    }
}
