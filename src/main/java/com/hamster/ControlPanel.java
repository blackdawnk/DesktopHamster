package com.hamster;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ControlPanel extends JFrame {

    private static final String FONT_NAME = "Noto Sans KR";
    public static final String VERSION = "1.2.5";

    /** Wraps emoji characters in HTML with Segoe UI Emoji font for proper rendering. */
    static String wrapEmoji(String text) {
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

    public interface Callbacks {
        void onCleanAll();
        void onFeed(Hamster h);
        void onPlay(Hamster h);
        void onRunWheel(Hamster h);
        void onSleep(Hamster h);
        void onOpenShop();
        void onBreed();
    }

    private List<Hamster> hamsters;
    private final Callbacks callbacks;
    private final JPanel mainPanel;
    private JLabel moneyLabel;
    private JLabel poopLabel;
    private List<HamsterUI> hamsterUIs = new ArrayList<>();

    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    public ControlPanel(List<Hamster> hamsters, Callbacks callbacks) {
        super("데스크탑 햄스터");
        this.hamsters = hamsters;
        this.callbacks = callbacks;

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
        titleBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel titleLabel = new JLabel(wrapEmoji("\uD83D\uDC39 데스크탑 햄스터 v" + VERSION));
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
        moneyLabel = new JLabel(wrapEmoji("\uD83D\uDCB0 0 코인"));
        moneyLabel.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        moneyLabel.setForeground(new Color(180, 140, 20));
        moneyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(moneyLabel);

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

        mainPanel.add(bottomBtns);
    }

    private JPanel createHamsterSection(HamsterUI ui, Hamster hamster) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(Box.createVerticalStrut(4));

        ui.nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(ui.nameLabel);

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

    public void refresh(int poopCount, int money) {
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
        if (sizeChanged) {
            pack();
        }
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
            bar.setPreferredSize(new Dimension(110, 16));
            bar.setMaximumSize(new Dimension(110, 16));
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
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(200, 18));
        JLabel l = new JLabel(label);
        l.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        l.setForeground(new Color(80, 60, 30));
        l.setPreferredSize(new Dimension(48, 16));
        p.add(l, BorderLayout.WEST);
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    private JButton createButton(String text, Color color, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(new Color(80, 50, 20));
        btn.setPreferredSize(new Dimension(78, 28));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
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
