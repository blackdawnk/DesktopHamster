package com.hamster.ui;
import com.hamster.model.Buff;
import com.hamster.model.Hamster;
import com.hamster.model.Poop;
import com.hamster.model.TimeOfDay;
import com.hamster.render.HamsterIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ControlPanel extends JFrame {

    private static final String FONT_NAME = "Noto Sans KR";
    public static final String VERSION = "2.1.2";

    /** Wraps emoji characters in HTML with Segoe UI Emoji font for proper rendering. */
    public static String wrapEmoji(String text) {
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
                  .append(c).append(text.charAt(i + 1));
                i++;
                // Include variation selector if present
                if (i + 1 < text.length() && text.charAt(i + 1) == '\uFE0F') {
                    sb.append(text.charAt(++i));
                }
                sb.append("</span>");
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

    public interface Callbacks {
        void onCleanAll();
        void onFeed(Hamster h);
        void onPlay(Hamster h);
        void onRunWheel(Hamster h);
        void onSleep(Hamster h);
        void onOpenShop();
        void onBreed();
        void onShowUpgradeInfo();
        void onOpacityChanged(float opacity);
        void onOpenSettings();
        void onKillAll();
        void onKillHamster(Hamster h);
        void onGatherAll();
        void onFreezeAll();
        void onShowAchievements();
        void onShowJournal();
        void onShowStatistics();
        void onEquipAccessory(Hamster h);
    }

    private List<Hamster> hamsters;
    private final Callbacks callbacks;
    private final JPanel mainPanel;
    private JLabel moneyLabel;
    private JLabel seedLabel;
    private JLabel poopLabel;
    private List<HamsterUI> hamsterUIs = new ArrayList<>();
    private int initialOpacity = 100;

    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    public ControlPanel(List<Hamster> hamsters, Callbacks callbacks, int savedOpacity) {
        super("데스크탑 햄스터");
        this.hamsters = hamsters;
        this.callbacks = callbacks;
        this.initialOpacity = savedOpacity;

        setUndecorated(true);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImages(HamsterIcon.createIcons());

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 160, 100), 2, true),
                BorderFactory.createEmptyBorder(6, 12, 16, 12)
        ));
        mainPanel.setBackground(new Color(255, 250, 240));

        buildUI();

        add(mainPanel);
        pack();
        if (getWidth() < 420) setSize(420, getHeight());

        // Position at bottom-left of screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());
        setLocation(10, screenSize.height - insets.bottom - getHeight() - 10);

        setupDrag(mainPanel);
        setVisible(true);
    }

    private void buildUI() {
        mainPanel.removeAll();
        hamsterUIs.clear();

        // Title bar
        JPanel titleBar = new JPanel(new BorderLayout(0, 0));
        titleBar.setOpaque(false);
        titleBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        TimeOfDay tod = TimeOfDay.getCurrentPeriod();
        JLabel titleLabel = new JLabel(wrapEmoji("\uD83D\uDC39 \uB370\uC2A4\uD06C\uD0D1 \uD584\uC2A4\uD130 " + tod.getEmoji() + tod.getDisplayName()));
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 15));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleBar.add(titleLabel, BorderLayout.CENTER);

        JPanel windowBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        windowBtns.setOpaque(false);
        JButton minimizeBtn = createWindowButton("\u2013", new Color(255, 190, 60));
        minimizeBtn.addActionListener(e -> setVisible(false));
        JButton closeBtn = createWindowButton("\u00D7", new Color(240, 100, 100));
        closeBtn.addActionListener(e -> {
            dispose();
            System.exit(0);
        });
        windowBtns.add(minimizeBtn);
        windowBtns.add(closeBtn);
        titleBar.add(windowBtns, BorderLayout.EAST);

        mainPanel.add(titleBar);

        // Money display
        mainPanel.add(createSeparator());
        JPanel currencyPanel = new JPanel(new BorderLayout());
        currencyPanel.setOpaque(false);
        currencyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel currencyLabels = new JPanel();
        currencyLabels.setLayout(new BoxLayout(currencyLabels, BoxLayout.Y_AXIS));
        currencyLabels.setOpaque(false);

        moneyLabel = new JLabel(wrapEmoji("\uD83D\uDCB0 0 코인"));
        moneyLabel.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        moneyLabel.setForeground(new Color(180, 140, 20));
        currencyLabels.add(moneyLabel);

        seedLabel = new JLabel(wrapEmoji("\uD83C\uDF3B 0 해바라기씨"));
        seedLabel.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        seedLabel.setForeground(new Color(120, 80, 20));
        currencyLabels.add(seedLabel);

        currencyPanel.add(currencyLabels, BorderLayout.CENTER);

        JButton settingsBtn = new JButton(wrapEmoji("\u2699"));
        settingsBtn.setFont(new Font(FONT_NAME, Font.PLAIN, 16));
        settingsBtn.setPreferredSize(new Dimension(32, 32));
        settingsBtn.setBackground(new Color(230, 225, 215));
        settingsBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 170, 150), 1, true),
                BorderFactory.createEmptyBorder(1, 4, 1, 4)
        ));
        settingsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settingsBtn.setToolTipText("\uC124\uC815");
        settingsBtn.addActionListener(e -> callbacks.onOpenSettings());
        currencyPanel.add(settingsBtn, BorderLayout.EAST);

        mainPanel.add(currencyPanel);

        // Hamster sections
        for (Hamster h : hamsters) {
            mainPanel.add(createSeparator());
            HamsterUI ui = new HamsterUI(h);
            hamsterUIs.add(ui);
            mainPanel.add(createHamsterSection(ui, h));
        }

        // Poop section
        mainPanel.add(createSeparator());
        mainPanel.add(Box.createVerticalStrut(4));

        JPanel poopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        poopPanel.setOpaque(false);
        poopPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        poopLabel = new JLabel(wrapEmoji("\uD83D\uDCA9 응가: 0개"));
        poopLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        poopLabel.setForeground(new Color(100, 80, 50));
        poopPanel.add(poopLabel);

        JButton cleanBtn = createButton("청소", new Color(180, 220, 255), e -> callbacks.onCleanAll());
        poopPanel.add(cleanBtn);

        mainPanel.add(poopPanel);

        // Bottom buttons (Save + Shop)
        mainPanel.add(createSeparator());
        mainPanel.add(Box.createVerticalStrut(4));

        JPanel bottomBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        bottomBtns.setOpaque(false);
        bottomBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton shopBtn = createButton("\uC0C1\uC810", new Color(255, 220, 180), e -> callbacks.onOpenShop());
        bottomBtns.add(shopBtn);

        JButton breedBtn = createButton("\uAD50\uBC30", new Color(255, 200, 220), e -> callbacks.onBreed());
        bottomBtns.add(breedBtn);

        JButton upgradeInfoBtn = createButton("\uC5C5\uADF8\uB808\uC774\uB4DC", new Color(220, 210, 255), e -> callbacks.onShowUpgradeInfo());
        bottomBtns.add(upgradeInfoBtn);

        mainPanel.add(bottomBtns);

        // 2.0 buttons row (same size as shop/breed/upgrade)
        JPanel newBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        newBtns.setOpaque(false);
        newBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton achBtn = createButton("\uC5C5\uC801", new Color(255, 230, 180), e -> callbacks.onShowAchievements());
        achBtn.setToolTipText("\uC5C5\uC801 \uBAA9\uB85D \uBCF4\uAE30");
        newBtns.add(achBtn);

        JButton journalBtn = createButton("\uB3C4\uAC10", new Color(220, 240, 255), e -> callbacks.onShowJournal());
        journalBtn.setToolTipText("\uD584\uC2A4\uD130 \uB3C4\uAC10 \uBCF4\uAE30");
        newBtns.add(journalBtn);

        JButton statsBtn = createButton("\uD1B5\uACC4", new Color(230, 255, 230), e -> callbacks.onShowStatistics());
        statsBtn.setToolTipText("\uD1B5\uACC4 \uBCF4\uAE30");
        newBtns.add(statsBtn);

        mainPanel.add(newBtns);

        // Utility buttons (gather, freeze, kill)
        JPanel utilBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        utilBtns.setOpaque(false);
        utilBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton gatherBtn = createSmallButton("\uBAA8\uC73C\uAE30", new Color(200, 220, 200), e -> callbacks.onGatherAll());
        gatherBtn.setToolTipText("\uBAA8\uB4E0 \uD584\uC2A4\uD130\uB97C \uD328\uB110 \uC606\uC73C\uB85C \uBAA8\uC74C");
        utilBtns.add(gatherBtn);

        JButton freezeBtn = createSmallButton("\uC77C\uC2DC\uC815\uC9C0", new Color(180, 210, 240), null);
        freezeBtn.setToolTipText("\uBAA8\uB4E0 \uD584\uC2A4\uD130 \uC77C\uC2DC\uC815\uC9C0/\uC7AC\uAC1C");
        freezeBtn.addActionListener(e -> {
            callbacks.onFreezeAll();
            if (freezeBtn.getText().equals("\uC77C\uC2DC\uC815\uC9C0")) {
                freezeBtn.setText("\uC7AC\uAC1C");
                freezeBtn.setBackground(new Color(255, 220, 150));
            } else {
                freezeBtn.setText("\uC77C\uC2DC\uC815\uC9C0");
                freezeBtn.setBackground(new Color(180, 210, 240));
            }
        });
        utilBtns.add(freezeBtn);

        JButton killBtn = createSmallButton("\uAC8C\uC784\uD3EC\uAE30", new Color(255, 170, 170), e -> callbacks.onKillAll());
        killBtn.setToolTipText("\uBAA8\uB4E0 \uD584\uC2A4\uD130\uB97C \uBCF4\uB0B4\uACE0 \uAC8C\uC784 \uC885\uB8CC");
        utilBtns.add(killBtn);

        mainPanel.add(utilBtns);

        // Opacity slider
        JPanel opacityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        opacityPanel.setOpaque(false);
        opacityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel opacityLabel = new JLabel("\uD22C\uBA85\uB3C4");
        opacityLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        opacityPanel.add(opacityLabel);
        JSlider opacitySlider = new JSlider(20, 100, initialOpacity);
        opacitySlider.setPreferredSize(new Dimension(180, 22));
        opacitySlider.setOpaque(false);

        // Click-to-jump: clicking the track jumps to that position directly
        opacitySlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                stopSliderAnimation();
                int trackWidth = opacitySlider.getWidth();
                int min = opacitySlider.getMinimum();
                int max = opacitySlider.getMaximum();
                int target = min + (int) ((double) e.getX() / trackWidth * (max - min));
                target = Math.max(min, Math.min(max, target));
                opacitySlider.setValue(target);
            }
        });

        opacitySlider.addChangeListener(e -> {
            float val = opacitySlider.getValue() / 100f;
            callbacks.onOpacityChanged(val);
        });
        opacityPanel.add(opacitySlider);
        mainPanel.add(opacityPanel);

        // Version label at bottom-right
        JLabel versionLabel = new JLabel("(" + VERSION + ")");
        versionLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 9));
        versionLabel.setForeground(new Color(180, 170, 150));
        versionLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        versionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel versionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        versionPanel.setOpaque(false);
        versionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        versionPanel.add(versionLabel);
        mainPanel.add(versionPanel);
    }

    private JPanel createHamsterSection(HamsterUI ui, Hamster hamster) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(Box.createVerticalStrut(4));

        ui.nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ui.nameLabel);

        ui.personalityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ui.personalityLabel);

        ui.stateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ui.stateLabel);

        ui.legacyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ui.legacyLabel);

        ui.buffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ui.buffLabel);

        section.add(Box.createVerticalStrut(4));

        section.add(createBarPanel("\uBC30\uACE0\uD514", ui.hungerBar));
        section.add(Box.createVerticalStrut(4));
        section.add(createBarPanel("행복", ui.happinessBar));
        section.add(Box.createVerticalStrut(4));
        section.add(createBarPanel("체력", ui.energyBar));
        section.add(Box.createVerticalStrut(5));

        JPanel btnRow1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        btnRow1.setOpaque(false);
        btnRow1.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow1.add(createButton("밥주기", new Color(255, 180, 120), e -> callbacks.onFeed(hamster)));
        btnRow1.add(createButton("놀기", new Color(255, 230, 120), e -> callbacks.onPlay(hamster)));
        section.add(btnRow1);
        section.add(Box.createVerticalStrut(3));

        JPanel btnRow2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        btnRow2.setOpaque(false);
        btnRow2.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow2.add(createButton("쳇바퀴", new Color(200, 185, 165), e -> callbacks.onRunWheel(hamster)));
        btnRow2.add(createButton("잠자기", new Color(150, 185, 255), e -> callbacks.onSleep(hamster)));
        section.add(btnRow2);

        JPanel btnRow3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnRow3.setOpaque(false);
        btnRow3.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton equipBtn = createSmallButton("\uCE58\uC7A5", new Color(255, 220, 180), e -> callbacks.onEquipAccessory(hamster));
        equipBtn.setToolTipText("\uC545\uC138\uC11C\uB9AC \uC7A5\uCC29");
        btnRow3.add(equipBtn);
        JButton killOneBtn = createSmallButton("\uBCF4\uB0B4\uAE30", new Color(255, 180, 180), e -> callbacks.onKillHamster(hamster));
        killOneBtn.setToolTipText("\uC774 \uD584\uC2A4\uD130\uB97C \uBCF4\uB0B4\uAE30");
        btnRow3.add(killOneBtn);
        section.add(btnRow3);

        return section;
    }

    private JPanel createSeparator() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalStrut(4));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sep);
        return p;
    }

    public void refresh(int poopCount, int money, int seeds) {
        boolean sizeChanged = false;
        for (HamsterUI ui : hamsterUIs) {
            boolean wasLegacy = ui.legacyLabel.isVisible();
            boolean wasBuff = ui.buffLabel.isVisible();
            refreshHamsterUI(ui);
            if (wasLegacy != ui.legacyLabel.isVisible() || wasBuff != ui.buffLabel.isVisible()) {
                sizeChanged = true;
            }
        }
        poopLabel.setText(wrapEmoji("\uD83D\uDCA9 응가: " + poopCount + "개"));
        moneyLabel.setText(wrapEmoji("\uD83D\uDCB0 " + money + " 코인"));
        seedLabel.setText(wrapEmoji("\uD83C\uDF3B " + seeds + " 해바라기씨"));
        if (sizeChanged) {
            pack();
            if (getWidth() < 420) setSize(420, getHeight());
        }
    }

    public void rebuild(List<Hamster> hamsters) {
        this.hamsters = hamsters;
        Point loc = getLocation();
        buildUI();
        mainPanel.revalidate();
        mainPanel.repaint();
        pack();
        if (getWidth() < 420) setSize(420, getHeight());
        setLocation(loc);
    }

    private void refreshHamsterUI(HamsterUI ui) {
        Hamster h = ui.hamster;
        String ageSuffix = " (" + h.getAgeDays() + "\uC77C)";
        String genSuffix = h.getGeneration() > 1 ? " \u2B50 " + h.getGeneration() + "\uC138\uB300" : "";
        if (h.isDead()) {
            ui.nameLabel.setText(wrapEmoji("\uD83D\uDC39 " + h.getName() + ageSuffix + genSuffix + " [\uC0AC\uB9DD]"));
        } else {
            ui.nameLabel.setText(wrapEmoji("\uD83D\uDC39 " + h.getName() + ageSuffix + genSuffix));
        }
        ui.hungerBar.setValue(Math.min(100, h.getHunger()));
        ui.hungerBar.setString(h.getHunger() + " / " + h.getMaxHunger());
        ui.happinessBar.setValue(Math.min(100, h.getHappiness()));
        ui.happinessBar.setString(h.getHappiness() + " / " + h.getMaxHappiness());
        ui.energyBar.setValue(Math.min(100, h.getEnergy()));
        ui.energyBar.setString(h.getEnergy() + " / " + h.getMaxEnergy());

        String stateText;
        switch (h.getState()) {
            case WALKING:       stateText = "\uAC77\uB294 \uC911"; break;
            case EATING:        stateText = "\uBA39\uB294 \uC911"; break;
            case SLEEPING:      stateText = "\uC790\uB294 \uC911 (zzZ)"; break;
            case HAPPY:         stateText = "\uD589\uBCF5!"; break;
            case RUNNING_WHEEL: stateText = "\uCCC7\uBC14\uD034 \uB2EC\uB9AC\uB294 \uC911"; break;
            default:            stateText = "\uB300\uAE30"; break;
        }
        ui.stateLabel.setText(" \uC0C1\uD0DC: " + stateText);

        // Legacy label
        if (h.getGeneration() > 1) {
            StringBuilder legacy = new StringBuilder(" \uB808\uAC70\uC2DC: ");
            if (h.getLegacyHungerBonus() > 0) legacy.append("\uBC30\uACE0\uD514+").append(h.getLegacyHungerBonus()).append(" ");
            if (h.getLegacyHappinessBonus() > 0) legacy.append("\uD589\uBCF5+").append(h.getLegacyHappinessBonus()).append(" ");
            if (h.getLegacyEnergyBonus() > 0) legacy.append("\uCCB4\uB825+").append(h.getLegacyEnergyBonus()).append(" ");
            if (h.getLegacyLifespanBonus() > 0) legacy.append("\uC218\uBA85+").append(h.getLegacyLifespanBonus() / Hamster.FRAMES_PER_DAY).append("\uC77C");
            ui.legacyLabel.setText(legacy.toString());
            ui.legacyLabel.setVisible(true);
        } else {
            ui.legacyLabel.setVisible(false);
        }

        // Buff label
        java.util.List<Buff> buffs = h.getBuffs();
        if (!buffs.isEmpty()) {
            StringBuilder buffText = new StringBuilder(" ");
            for (int i = 0; i < buffs.size(); i++) {
                Buff b = buffs.get(i);
                int seconds = b.getRemainingFrames() / 30;
                int min = seconds / 60;
                int sec = seconds % 60;
                if (i > 0) buffText.append(" | ");
                buffText.append(b.getDescription()).append(" ").append(min).append(":").append(String.format("%02d", sec));
            }
            ui.buffLabel.setText(buffText.toString());
            ui.buffLabel.setVisible(true);
        } else {
            ui.buffLabel.setVisible(false);
        }
    }

    // --- inner class for per-hamster UI elements ---
    private static class HamsterUI {
        final Hamster hamster;
        final JLabel nameLabel;
        final JLabel personalityLabel;
        final JLabel stateLabel;
        final JLabel legacyLabel;
        final JLabel buffLabel;
        final JProgressBar hungerBar;
        final JProgressBar happinessBar;
        final JProgressBar energyBar;

        HamsterUI(Hamster hamster) {
            this.hamster = hamster;
            nameLabel = new JLabel(wrapEmoji("\uD83D\uDC39 " + hamster.getName()));
            nameLabel.setFont(new Font(FONT_NAME, Font.BOLD, 13));
            nameLabel.setForeground(new Color(80, 50, 20));

            String pName = hamster.getPersonality() != null ? hamster.getPersonality().getDisplayName() : "";
            personalityLabel = new JLabel(" \uC131\uACA9: " + pName);
            personalityLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            personalityLabel.setForeground(new Color(130, 90, 60));

            stateLabel = new JLabel(" \uC0C1\uD0DC: \uB300\uAE30");
            stateLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            stateLabel.setForeground(new Color(100, 80, 50));

            legacyLabel = new JLabel("");
            legacyLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            legacyLabel.setForeground(new Color(140, 100, 40));
            legacyLabel.setVisible(false);

            buffLabel = new JLabel("");
            buffLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            buffLabel.setForeground(new Color(60, 100, 160));
            buffLabel.setVisible(false);

            hungerBar = createBar(new Color(255, 150, 80));
            happinessBar = createBar(new Color(255, 200, 80));
            energyBar = createBar(new Color(100, 200, 120));
        }

        private static JProgressBar createBar(Color color) {
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(70);
            bar.setStringPainted(true);
            bar.setForeground(color);
            bar.setBackground(new Color(240, 240, 240));
            bar.setPreferredSize(new Dimension(220, 18));
            bar.setMaximumSize(new Dimension(320, 18));
            bar.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
            return bar;
        }
    }

    // --- helper methods ---

    private JButton createWindowButton(String symbol, Color color) {
        JButton btn = new JButton(symbol);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        btn.setForeground(color.darker());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(22, 22));
        return btn;
    }

    private JPanel createBarPanel(String label, JProgressBar bar) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(380, 22));
        JLabel l = new JLabel(label);
        l.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        l.setForeground(new Color(80, 60, 30));
        l.setPreferredSize(new Dimension(55, 18));
        p.add(l, BorderLayout.WEST);
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    private Timer sliderAnimTimer = null;

    private void stopSliderAnimation() {
        if (sliderAnimTimer != null) {
            sliderAnimTimer.stop();
            sliderAnimTimer = null;
        }
    }

    private JButton createSmallButton(String text, Color color, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(new Color(80, 50, 20));
        btn.setPreferredSize(new Dimension(80, 26));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(action);
        return btn;
    }

    private JButton createButton(String text, Color color, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(new Color(80, 50, 20));
        btn.setPreferredSize(new Dimension(90, 30));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    private void setupDrag(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragging = true;
                dragOffsetX = e.getXOnScreen() - getX();
                dragOffsetY = e.getYOnScreen() - getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    setLocation(
                            e.getXOnScreen() - dragOffsetX,
                            e.getYOnScreen() - dragOffsetY
                    );
                }
            }
        });
    }
}
