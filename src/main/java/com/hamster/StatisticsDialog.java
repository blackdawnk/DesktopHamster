package com.hamster;

import javax.swing.*;
import java.awt.*;

public class StatisticsDialog {

    public static void show(Component parent, GameStatistics stats) {
        JDialog dialog = new JDialog((Frame) null, "\uD1B5\uACC4", true);
        dialog.setResizable(false);
        dialog.setIconImages(HamsterIcon.createIcons());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(new Color(255, 250, 240));

        JLabel titleLabel = new JLabel(ControlPanel.wrapEmoji("\uD83D\uDCCA \uD1B5\uACC4"));
        titleLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));

        // General
        addSection(panel, "\uC77C\uBC18");
        long totalMinutes = stats.totalPlayTimeFrames / 30 / 60;
        long hours = totalMinutes / 60;
        long mins = totalMinutes % 60;
        addRow(panel, "\uCD1D \uD50C\uB808\uC774 \uC2DC\uAC04", hours + "\uC2DC\uAC04 " + mins + "\uBD84");
        addRow(panel, "\uCD1D \uAC8C\uC784 \uD68C\uC218", String.valueOf(stats.totalGamesPlayed));
        addRow(panel, "\uCD1D \uC0AC\uB9DD \uD69F\uC218", String.valueOf(stats.totalDeaths));

        // Hamsters
        addSection(panel, "\uD584\uC2A4\uD130");
        addRow(panel, "\uD0A4\uC6B4 \uD584\uC2A4\uD130 \uC218", String.valueOf(stats.totalHamstersRaised));
        addRow(panel, "\uCD5C\uB300 \uC138\uB300", String.valueOf(stats.maxGenerationReached));
        addRow(panel, "\uCD5C\uC7A5 \uC218\uBA85", stats.longestLifespanDays + "\uC77C");
        addRow(panel, "\uCD1D \uAD50\uBC30 \uD69F\uC218", String.valueOf(stats.totalBreeds));

        // Economy
        addSection(panel, "\uACBD\uC81C");
        addRow(panel, "\uCD1D \uD68D\uB4DD \uCF54\uC778", String.valueOf(stats.totalCoinsEarned));
        addRow(panel, "\uCD1D \uC0AC\uC6A9 \uCF54\uC778", String.valueOf(stats.totalCoinsSpent));

        // Actions
        addSection(panel, "\uD65C\uB3D9");
        addRow(panel, "\uBC25\uC8FC\uAE30", String.valueOf(stats.totalFeedActions));
        addRow(panel, "\uB180\uAE30", String.valueOf(stats.totalPlayActions));
        addRow(panel, "\uC7A0\uC7AC\uC6B0\uAE30", String.valueOf(stats.totalSleepActions));
        addRow(panel, "\uCCC7\uBC14\uD034", String.valueOf(stats.totalWheelActions));
        addRow(panel, "\uC751\uAC00 \uCCAD\uC18C", String.valueOf(stats.totalPoopsCleaned));
        addRow(panel, "\uC774\uBCA4\uD2B8 \uBC1C\uC0DD", String.valueOf(stats.totalEventsTriggered));
        addRow(panel, "\uC0C1\uD638\uC791\uC6A9", String.valueOf(stats.totalInteractions));

        panel.add(Box.createVerticalStrut(15));

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

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(350, 500));
        scroll.getViewport().setBackground(new Color(255, 250, 240));

        dialog.setContentPane(scroll);
        dialog.pack();
        dialog.setAlwaysOnTop(true);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void addSection(JPanel panel, String title) {
        panel.add(Box.createVerticalStrut(10));
        JLabel label = new JLabel("\u2500\u2500 " + title + " \u2500\u2500");
        label.setFont(new Font("Noto Sans KR", Font.BOLD, 13));
        label.setForeground(new Color(120, 90, 40));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
    }

    private static void addRow(JPanel panel, String name, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(300, 22));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
        nameLabel.setForeground(new Color(80, 50, 20));
        row.add(nameLabel, BorderLayout.WEST);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 12));
        valueLabel.setForeground(new Color(100, 80, 50));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(valueLabel, BorderLayout.EAST);

        panel.add(row);
    }
}
