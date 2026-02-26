package com.hamster.ui;
import com.hamster.system.RandomEvent;

import javax.swing.*;
import java.awt.*;

public class EventDialog {

    private static final String FONT_NAME = "Noto Sans KR";

    /**
     * Shows a modal event dialog with two choices.
     * @return true if choice A was selected, false if choice B
     */
    public static boolean showEvent(RandomEvent event) {
        final boolean[] result = {true};

        JDialog dialog = new JDialog((Frame) null, event.getTitle(), true);
        dialog.setUndecorated(true);
        dialog.setAlwaysOnTop(true);
        UIHelper.addEscapeClose(dialog);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 160, 100), 2, true),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));
        panel.setBackground(new Color(255, 250, 240));

        // Title
        JLabel titleLabel = new JLabel(ControlPanel.wrapEmoji(event.getTitle()));
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(12));

        // Description
        JLabel descLabel = new JLabel("<html><div style='text-align:center;width:200px'>"
                + event.getDescription().replace("\n", "<br>") + "</div></html>");
        descLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        descLabel.setForeground(new Color(60, 50, 30));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(16));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.setOpaque(false);

        JButton btnA = createChoiceButton(event.getChoiceA(), new Color(255, 200, 120));
        btnA.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        JButton btnB = createChoiceButton(event.getChoiceB(), new Color(180, 210, 255));
        btnB.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });

        btnPanel.add(btnA);
        btnPanel.add(btnB);
        panel.add(btnPanel);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        return result[0];
    }

    private static JButton createChoiceButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(new Color(80, 50, 20));
        btn.setPreferredSize(new Dimension(100, 34));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
