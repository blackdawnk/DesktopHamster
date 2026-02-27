package com.hamster.ui;
import com.hamster.model.Buff;
import com.hamster.model.Hamster;
import com.hamster.model.Poop;
import com.hamster.model.TimeOfDay;
import com.hamster.model.UITheme;
import com.hamster.render.HamsterIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ControlPanel extends JFrame {

    private static final String FONT_NAME = "Noto Sans KR";
    public static final String VERSION = "2.2.0";

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
    private UITheme currentTheme = UITheme.CLASSIC;

    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    public ControlPanel(List<Hamster> hamsters, Callbacks callbacks, int savedOpacity) {
        this(hamsters, callbacks, savedOpacity, UITheme.CLASSIC);
    }

    public ControlPanel(List<Hamster> hamsters, Callbacks callbacks, int savedOpacity, UITheme theme) {
        super("데스크탑 햄스터");
        this.hamsters = hamsters;
        this.callbacks = callbacks;
        this.initialOpacity = savedOpacity;
        this.currentTheme = theme != null ? theme : UITheme.CLASSIC;

        setUndecorated(true);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImages(HamsterIcon.createIcons());

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        applyThemeToMainPanel();

        buildUI();

        add(mainPanel);
        pack();

        // Position at bottom-left of screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());
        setLocation(10, screenSize.height - insets.bottom - getHeight() - 10);

        setupDrag(mainPanel);
        setVisible(true);
    }

    private static final int F_TITLE = 14;
    private static final int F_LABEL = 12;
    private static final int F_BTN = 12;

    private void buildUI() {
        mainPanel.removeAll();
        hamsterUIs.clear();

        // Title bar
        JPanel titleBar = new JPanel(new BorderLayout(0, 0));
        titleBar.setOpaque(false);
        titleBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        TimeOfDay tod = TimeOfDay.getCurrentPeriod();
        JLabel titleLabel = new JLabel(wrapEmoji("\uD83D\uDC39 " + tod.getEmoji() + tod.getDisplayName()));
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, F_TITLE));
        titleLabel.setForeground(currentTheme.textPrimary);
        titleBar.add(titleLabel, BorderLayout.CENTER);

        JPanel windowBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));
        windowBtns.setOpaque(false);
        JButton minimizeBtn = createWindowButton("\u2013", new Color(255, 190, 60));
        minimizeBtn.addActionListener(e -> setVisible(false));
        JButton closeBtn = createWindowButton("\u00D7", new Color(240, 100, 100));
        closeBtn.addActionListener(e -> { dispose(); System.exit(0); });
        windowBtns.add(minimizeBtn);
        windowBtns.add(closeBtn);
        titleBar.add(windowBtns, BorderLayout.EAST);
        mainPanel.add(titleBar);

        // Currency (single row)
        mainPanel.add(createSeparator());
        JPanel currencyPanel = new JPanel(new BorderLayout(2, 0));
        currencyPanel.setOpaque(false);
        currencyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        moneyLabel = new JLabel();
        moneyLabel.setFont(new Font(FONT_NAME, Font.BOLD, F_LABEL));
        moneyLabel.setForeground(currentTheme.accent);

        seedLabel = new JLabel();
        seedLabel.setFont(new Font(FONT_NAME, Font.BOLD, F_LABEL));
        seedLabel.setForeground(currentTheme.textSecondary);

        JPanel currRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        currRow.setOpaque(false);
        currRow.add(moneyLabel);
        currRow.add(seedLabel);
        currencyPanel.add(currRow, BorderLayout.CENTER);

        JButton settingsBtn = new JButton(wrapEmoji("\u2699"));
        settingsBtn.setFont(new Font(FONT_NAME, Font.PLAIN, 15));
        settingsBtn.setBackground(currentTheme.buttonBg);
        settingsBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(currentTheme.buttonBorder, 1, true),
                BorderFactory.createEmptyBorder(1, 3, 1, 3)
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

        // Poop + Clean
        mainPanel.add(createSeparator());
        JPanel poopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        poopPanel.setOpaque(false);
        poopPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        poopLabel = new JLabel("0");
        poopLabel.setFont(new Font(FONT_NAME, Font.PLAIN, F_LABEL));
        poopLabel.setForeground(currentTheme.textSecondary);
        poopPanel.add(poopLabel);
        poopPanel.add(createButton("\uCCAD\uC18C", new Color(180, 220, 255), e -> callbacks.onCleanAll()));
        mainPanel.add(poopPanel);

        // All function buttons in compact grid (4 columns)
        mainPanel.add(createSeparator());
        JPanel grid = new JPanel(new GridLayout(0, 4, 2, 2));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(createButton("\uC0C1\uC810", new Color(255, 220, 180), e -> callbacks.onOpenShop()));
        grid.add(createButton("\uAD50\uBC30", new Color(255, 200, 220), e -> callbacks.onBreed()));
        grid.add(createButton("\uC5C5\uADF8", new Color(220, 210, 255), e -> callbacks.onShowUpgradeInfo()));
        grid.add(createButton("\uC5C5\uC801", new Color(255, 230, 180), e -> callbacks.onShowAchievements()));
        grid.add(createButton("\uD1B5\uACC4", new Color(230, 255, 230), e -> callbacks.onShowStatistics()));
        grid.add(createButton("\uB3C4\uAC10", new Color(220, 240, 255), e -> callbacks.onShowJournal()));
        grid.add(createButton("\uBAA8\uC73C\uAE30", new Color(200, 220, 200), e -> callbacks.onGatherAll()));
        JButton freezeBtn = createButton("\uC815\uC9C0", new Color(180, 210, 240), null);
        freezeBtn.addActionListener(e -> {
            callbacks.onFreezeAll();
            if (freezeBtn.getText().equals("\uC815\uC9C0")) {
                freezeBtn.setText("\uC7AC\uAC1C");
                freezeBtn.setBackground(new Color(255, 220, 150));
            } else {
                freezeBtn.setText("\uC815\uC9C0");
                freezeBtn.setBackground(new Color(180, 210, 240));
            }
        });
        grid.add(freezeBtn);
        grid.add(createButton("\uD3EC\uAE30", new Color(255, 170, 170), e -> callbacks.onKillAll()));
        mainPanel.add(grid);

        // Opacity slider (compact)
        JPanel opacityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        opacityPanel.setOpaque(false);
        opacityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSlider opacitySlider = new JSlider(20, 100, initialOpacity);
        opacitySlider.setPreferredSize(new Dimension(80, 14));
        opacitySlider.setOpaque(false);
        opacitySlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                stopSliderAnimation();
                int tw = opacitySlider.getWidth();
                int mn = opacitySlider.getMinimum(), mx = opacitySlider.getMaximum();
                opacitySlider.setValue(Math.max(mn, Math.min(mx, mn + (int)((double)e.getX()/tw*(mx-mn)))));
            }
        });
        opacitySlider.addChangeListener(e -> callbacks.onOpacityChanged(opacitySlider.getValue() / 100f));
        JLabel opLabel = new JLabel("\uD22C\uBA85");
        opLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        opLabel.setForeground(currentTheme.textSecondary);
        opacityPanel.add(opLabel);
        opacityPanel.add(opacitySlider);

        JLabel versionLabel = new JLabel(VERSION);
        versionLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        versionLabel.setForeground(currentTheme.buttonBorder);
        opacityPanel.add(versionLabel);
        mainPanel.add(opacityPanel);
    }

    private JPanel createHamsterSection(HamsterUI ui, Hamster hamster) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(Box.createVerticalStrut(1));

        ui.nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ui.nameLabel);

        ui.infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ui.infoLabel);

        ui.legacyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ui.legacyLabel);

        ui.buffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ui.buffLabel);

        section.add(Box.createVerticalStrut(1));
        section.add(createBarPanel("\uBC30", ui.hungerBar));
        section.add(Box.createVerticalStrut(1));
        section.add(createBarPanel("\uD589", ui.happinessBar));
        section.add(Box.createVerticalStrut(1));
        section.add(createBarPanel("\uCCB4", ui.energyBar));
        section.add(Box.createVerticalStrut(2));

        // Action buttons: 2 rows of 3
        JPanel btnRow1 = new JPanel(new GridLayout(1, 3, 2, 0));
        btnRow1.setOpaque(false);
        btnRow1.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow1.add(createButton("\uBC25", new Color(255, 180, 120), e -> callbacks.onFeed(hamster)));
        btnRow1.add(createButton("\uB180\uAE30", new Color(255, 230, 120), e -> callbacks.onPlay(hamster)));
        btnRow1.add(createButton("\uCCC7\uBC14\uD034", new Color(200, 185, 165), e -> callbacks.onRunWheel(hamster)));
        section.add(btnRow1);
        section.add(Box.createVerticalStrut(1));

        JPanel btnRow2 = new JPanel(new GridLayout(1, 3, 2, 0));
        btnRow2.setOpaque(false);
        btnRow2.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow2.add(createButton("\uC7A0", new Color(150, 185, 255), e -> callbacks.onSleep(hamster)));
        btnRow2.add(createButton("\uCE58\uC7A5", new Color(255, 220, 180), e -> callbacks.onEquipAccessory(hamster)));
        btnRow2.add(createButton("X", new Color(255, 180, 180), e -> callbacks.onKillHamster(hamster)));
        section.add(btnRow2);

        return section;
    }

    private JPanel createSeparator() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalStrut(1));
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
        poopLabel.setText(wrapEmoji("\uD83D\uDCA9" + poopCount));
        moneyLabel.setText(wrapEmoji("\uD83D\uDCB0" + money));
        seedLabel.setText(wrapEmoji("\uD83C\uDF3B" + seeds));
        if (sizeChanged) {
            pack();
        }
    }

    private void applyThemeToMainPanel() {
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(currentTheme.border, 1, true),
                BorderFactory.createEmptyBorder(2, 4, 4, 4)
        ));
        mainPanel.setBackground(currentTheme.bg);
    }

    public void applyTheme(UITheme theme) {
        this.currentTheme = theme != null ? theme : UITheme.CLASSIC;
        applyThemeToMainPanel();
        Point loc = getLocation();
        buildUI();
        mainPanel.revalidate();
        mainPanel.repaint();
        pack();
        setLocation(loc);
    }

    public UITheme getCurrentTheme() {
        return currentTheme;
    }

    public void rebuild(List<Hamster> hamsters) {
        this.hamsters = hamsters;
        Point loc = getLocation();
        buildUI();
        mainPanel.revalidate();
        mainPanel.repaint();
        pack();
        setLocation(loc);
    }

    private void refreshHamsterUI(HamsterUI ui) {
        Hamster h = ui.hamster;
        String age = h.getAgeDays() + "\uC77C";
        String gen = h.getGeneration() > 1 ? " " + h.getGeneration() + "\uC138\uB300" : "";
        String dead = h.isDead() ? " \u2620" : "";
        ui.nameLabel.setText(wrapEmoji("\uD83D\uDC39" + h.getName() + " " + age + gen + dead));

        ui.hungerBar.setValue(Math.min(100, h.getHunger()));
        ui.hungerBar.setString(h.getHunger() + "/" + h.getMaxHunger());
        ui.happinessBar.setValue(Math.min(100, h.getHappiness()));
        ui.happinessBar.setString(h.getHappiness() + "/" + h.getMaxHappiness());
        ui.energyBar.setValue(Math.min(100, h.getEnergy()));
        ui.energyBar.setString(h.getEnergy() + "/" + h.getMaxEnergy());

        // Info: personality + state
        String pName = h.getPersonality() != null ? h.getPersonality().getDisplayName() : "";
        String stateText;
        switch (h.getState()) {
            case WALKING:       stateText = "\uAC77\uB294\uC911"; break;
            case EATING:        stateText = "\uBA39\uB294\uC911"; break;
            case SLEEPING:      stateText = "zzZ"; break;
            case HAPPY:         stateText = "\uD589\uBCF5!"; break;
            case RUNNING_WHEEL: stateText = "\uCCC7\uBC14\uD034"; break;
            default:            stateText = "\uB300\uAE30"; break;
        }
        ui.infoLabel.setText(pName + " | " + stateText);

        // Legacy
        if (h.getGeneration() > 1) {
            StringBuilder leg = new StringBuilder();
            if (h.getLegacyHungerBonus() > 0) leg.append("\uBC30+").append(h.getLegacyHungerBonus()).append(" ");
            if (h.getLegacyHappinessBonus() > 0) leg.append("\uD589+").append(h.getLegacyHappinessBonus()).append(" ");
            if (h.getLegacyEnergyBonus() > 0) leg.append("\uCCB4+").append(h.getLegacyEnergyBonus()).append(" ");
            if (h.getLegacyLifespanBonus() > 0) leg.append("\uC218\uBA85+").append(h.getLegacyLifespanBonus() / Hamster.FRAMES_PER_DAY).append("\uC77C");
            ui.legacyLabel.setText(leg.toString());
            ui.legacyLabel.setVisible(true);
        } else {
            ui.legacyLabel.setVisible(false);
        }

        // Buff
        java.util.List<Buff> buffs = h.getBuffs();
        if (!buffs.isEmpty()) {
            StringBuilder bt = new StringBuilder();
            for (int i = 0; i < buffs.size(); i++) {
                Buff b = buffs.get(i);
                int sec = b.getRemainingFrames() / 30;
                if (i > 0) bt.append("|");
                bt.append(b.getDescription()).append(sec / 60).append(":").append(String.format("%02d", sec % 60));
            }
            ui.buffLabel.setText(bt.toString());
            ui.buffLabel.setVisible(true);
        } else {
            ui.buffLabel.setVisible(false);
        }
    }

    // --- inner class for per-hamster UI elements ---
    private class HamsterUI {
        final Hamster hamster;
        final JLabel nameLabel;
        final JLabel infoLabel;  // personality + state combined
        final JLabel legacyLabel;
        final JLabel buffLabel;
        final JProgressBar hungerBar;
        final JProgressBar happinessBar;
        final JProgressBar energyBar;

        HamsterUI(Hamster hamster) {
            this.hamster = hamster;
            nameLabel = new JLabel(wrapEmoji("\uD83D\uDC39 " + hamster.getName()));
            nameLabel.setFont(new Font(FONT_NAME, Font.BOLD, F_LABEL));
            nameLabel.setForeground(currentTheme.textPrimary);

            String pName = hamster.getPersonality() != null ? hamster.getPersonality().getDisplayName() : "";
            infoLabel = new JLabel(pName + " | \uB300\uAE30");
            infoLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            infoLabel.setForeground(currentTheme.textSecondary);

            legacyLabel = new JLabel("");
            legacyLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            legacyLabel.setForeground(currentTheme.accent);
            legacyLabel.setVisible(false);

            buffLabel = new JLabel("");
            buffLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
            buffLabel.setForeground(new Color(60, 100, 160));
            buffLabel.setVisible(false);

            hungerBar = createBar(new Color(255, 150, 80));
            happinessBar = createBar(new Color(255, 200, 80));
            energyBar = createBar(new Color(100, 200, 120));
        }

        private JProgressBar createBar(Color color) {
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(70);
            bar.setStringPainted(true);
            bar.setForeground(color);
            bar.setBackground(new Color(240, 240, 240));
            bar.setPreferredSize(new Dimension(90, 12));
            bar.setMaximumSize(new Dimension(Short.MAX_VALUE, 12));
            bar.setFont(new Font(FONT_NAME, Font.BOLD, 11));
            bar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
                @Override protected Color getSelectionForeground() { return new Color(50, 30, 10); }
                @Override protected Color getSelectionBackground() { return new Color(80, 60, 40); }
            });
            bar.setForeground(color);
            return bar;
        }
    }

    // --- helper methods ---

    private JButton createWindowButton(String symbol, Color color) {
        JButton btn = new JButton(symbol);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        btn.setForeground(color.darker());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createBarPanel(String label, JProgressBar bar) {
        JPanel p = new JPanel(new BorderLayout(2, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 14));
        JLabel l = new JLabel(label);
        l.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        l.setForeground(currentTheme.textPrimary);
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
        return createButton(text, color, action);
    }

    private JButton createButton(String text, Color color, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, F_BTN));
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(currentTheme.textPrimary);
        btn.setMargin(new java.awt.Insets(1, 3, 1, 3));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                BorderFactory.createEmptyBorder(1, 3, 1, 3)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(action);
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
