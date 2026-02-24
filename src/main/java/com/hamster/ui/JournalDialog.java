package com.hamster.ui;
import com.hamster.render.HamsterIcon;
import com.hamster.system.HamsterJournal;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JournalDialog {

    private static JDialog activeInstance = null;

    public static void show(Component parent, HamsterJournal journal) {
        if (activeInstance != null && activeInstance.isVisible()) {
            activeInstance.toFront();
            return;
        }
        JDialog dialog = new JDialog((Frame) null, "\uB3C4\uAC10", false);
        activeInstance = dialog;
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { activeInstance = null; }
        });
        dialog.setResizable(false);
        dialog.setIconImages(HamsterIcon.createIcons());

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        outerPanel.setBackground(new Color(255, 250, 240));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 5, 30));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(ControlPanel.wrapEmoji("\uD83D\uDCD6 \uD584\uC2A4\uD130 \uB3C4\uAC10"));
        titleLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));

        JLabel countLabel = new JLabel("\uAE30\uB85D\uB41C \uD584\uC2A4\uD130: " + journal.getEntryCount() + "\uB9C8\uB9AC");
        countLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 13));
        countLabel.setForeground(new Color(120, 90, 40));
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(countLabel);
        outerPanel.add(headerPanel);

        // Journal entries
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        listPanel.setBackground(new Color(255, 250, 240));

        List<HamsterJournal.JournalEntry> entries = journal.getEntries();
        if (entries.isEmpty()) {
            JLabel emptyLabel = new JLabel("\uC544\uC9C1 \uAE30\uB85D\uC774 \uC5C6\uC2B5\uB2C8\uB2E4.");
            emptyLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 13));
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(emptyLabel);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            for (HamsterJournal.JournalEntry entry : entries) {
                JPanel card = new JPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBackground(new Color(255, 252, 245));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(210, 190, 150), 1, true),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                card.setMaximumSize(new Dimension(380, 100));
                card.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel nameLabel = new JLabel(ControlPanel.wrapEmoji("\uD83D\uDC39 " + entry.name
                        + " (" + entry.generation + "\uC138\uB300)"));
                nameLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 13));
                nameLabel.setForeground(new Color(80, 50, 20));
                card.add(nameLabel);

                JLabel infoLabel = new JLabel("\uC0C9\uC0C1: " + entry.color
                        + " | \uC131\uACA9: " + entry.personality
                        + " | \uC218\uBA85: " + entry.lifespanDays + "\uC77C");
                infoLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 11));
                infoLabel.setForeground(new Color(100, 80, 50));
                card.add(infoLabel);

                JLabel deathLabel = new JLabel("\uC0AC\uC778: " + entry.causeOfDeath);
                deathLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 11));
                deathLabel.setForeground(new Color(180, 100, 80));
                card.add(deathLabel);

                if (entry.timestamp > 0) {
                    JLabel dateLabel = new JLabel(sdf.format(new Date(entry.timestamp)));
                    dateLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 10));
                    dateLabel.setForeground(new Color(160, 150, 130));
                    card.add(dateLabel);
                }

                listPanel.add(card);
                listPanel.add(Box.createVerticalStrut(6));
            }
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(420, 400));
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
