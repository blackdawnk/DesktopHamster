package com.hamster.ui;
import com.hamster.render.HamsterIcon;
import com.hamster.system.Achievement;
import com.hamster.system.AchievementManager;

import javax.swing.*;
import java.awt.*;

public class AchievementDialog {

    private static JDialog activeInstance = null;

    public static void show(Component parent, AchievementManager manager) {
        if (activeInstance != null && activeInstance.isVisible()) {
            activeInstance.toFront();
            return;
        }
        JDialog dialog = new JDialog((Frame) null, "\uC5C5\uC801", false);
        activeInstance = dialog;
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { activeInstance = null; }
        });
        dialog.setResizable(false);
        dialog.setIconImages(HamsterIcon.createIcons());
        UIHelper.addEscapeClose(dialog);

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        outerPanel.setBackground(new Color(255, 250, 240));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 5, 30));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(ControlPanel.wrapEmoji("\uD83C\uDFC6 \uC5C5\uC801"));
        titleLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));

        JLabel countLabel = new JLabel(manager.getUnlockedCount() + " / " + Achievement.values().length + " \uD574\uAE08");
        countLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 13));
        countLabel.setForeground(new Color(120, 90, 40));
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(countLabel);
        outerPanel.add(headerPanel);

        // Achievement grid
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        gridPanel.setBackground(new Color(255, 250, 240));

        for (Achievement ach : Achievement.values()) {
            boolean unlocked = manager.isUnlocked(ach);
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(true);
            row.setBackground(unlocked ? new Color(255, 245, 200) : new Color(230, 230, 230));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            unlocked ? new Color(220, 190, 100) : new Color(200, 200, 200), 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
            row.setMaximumSize(new Dimension(380, 50));
            row.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel icon = new JLabel(unlocked ? "\uD83C\uDFC5" : "\uD83D\uDD12");
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            row.add(icon, BorderLayout.WEST);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(unlocked ? ach.getDisplayName() : "???");
            nameLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 12));
            nameLabel.setForeground(unlocked ? new Color(80, 50, 20) : new Color(150, 150, 150));
            textPanel.add(nameLabel);

            JLabel descLabel = new JLabel(ach.getDescription());
            descLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 10));
            descLabel.setForeground(unlocked ? new Color(100, 80, 50) : new Color(170, 170, 170));
            textPanel.add(descLabel);

            row.add(textPanel, BorderLayout.CENTER);

            if (unlocked) {
                String rewardText = ach.getRewardType() == Achievement.RewardType.COINS
                        ? ach.getRewardAmount() + "\uCF54\uC778"
                        : ach.getRewardAmount() + "\uC528\uC557";
                JLabel rewardLabel = new JLabel(rewardText);
                rewardLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 11));
                rewardLabel.setForeground(new Color(180, 140, 20));
                row.add(rewardLabel, BorderLayout.EAST);
            }

            gridPanel.add(row);
            gridPanel.add(Box.createVerticalStrut(4));
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(420, 450));
        scrollPane.getViewport().setBackground(new Color(255, 250, 240));
        outerPanel.add(scrollPane);

        // Close button
        JPanel footerPanel = new JPanel();
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 30, 15, 30));
        footerPanel.setOpaque(false);
        JButton closeBtn = new JButton("\uB2EB\uAE30");
        closeBtn.setFont(new Font("Noto Sans KR", Font.BOLD, 13));
        closeBtn.setFocusPainted(false);
        closeBtn.setBackground(new Color(220, 220, 220));
        closeBtn.setForeground(new Color(80, 50, 20));
        closeBtn.setPreferredSize(new Dimension(200, 32));
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());
        footerPanel.add(closeBtn);
        outerPanel.add(footerPanel);

        dialog.setContentPane(outerPanel);
        dialog.pack();
        dialog.setAlwaysOnTop(true);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
