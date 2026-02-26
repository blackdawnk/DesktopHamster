package com.hamster.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Common UI utilities shared across dialogs and panels.
 */
public final class UIHelper {

    public static final String FONT_NAME = "Noto Sans KR";
    public static final Color BG_COLOR = new Color(255, 250, 240);
    public static final Color TEXT_COLOR = new Color(80, 50, 20);
    public static final Color SUB_TEXT_COLOR = new Color(100, 80, 50);
    public static final Color GOLD_COLOR = new Color(218, 165, 32);
    public static final Color CURRENCY_COLOR = new Color(180, 140, 20);
    public static final Color SLOT_ACTIVE = new Color(255, 250, 230);
    public static final Color SLOT_EMPTY = new Color(235, 230, 220);
    public static final Color BORDER_ACTIVE = new Color(200, 170, 100);
    public static final Color BORDER_EMPTY = new Color(190, 185, 175);
    public static final Color HOVER_BG = new Color(255, 240, 190);
    public static final Color HOVER_BORDER = new Color(230, 180, 60);

    private UIHelper() {}

    /** Create a standard action button (90x30). */
    public static JButton createButton(String text, Color color, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(TEXT_COLOR);
        btn.setPreferredSize(new Dimension(90, 30));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(action);
        return btn;
    }

    /** Create a small utility button (80x26). */
    public static JButton createSmallButton(String text, Color color, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(TEXT_COLOR);
        btn.setPreferredSize(new Dimension(80, 26));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) btn.addActionListener(action);
        return btn;
    }

    /** Create a horizontal separator panel. */
    public static JPanel createSeparator() {
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

    /** Create a labeled progress bar panel. */
    public static JPanel createBarPanel(String label, JProgressBar bar) {
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

    /** Create a styled progress bar. */
    public static JProgressBar createBar(Color color) {
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

    /** Bind ESC key to close/dispose a JDialog. */
    public static void addEscapeClose(JDialog dialog) {
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    /** Get slot display name in Korean. */
    public static String getSlotDisplayName(com.hamster.model.Accessory.Slot slot) {
        switch (slot) {
            case HEAD: return "\uBA38\uB9AC";
            case FACE: return "\uC5BC\uAD74";
            case NECK: return "\uBAA9";
            case BODY: return "\uBAB8";
            case SHOES: return "\uBC1C";
            case HANDS: return "\uC190";
            default: return slot.name();
        }
    }
}
