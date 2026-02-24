package com.hamster.ui;
import com.hamster.model.Poop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PoopWindow extends JWindow {

    private static final int SIZE = 30;

    private final Poop poop;

    public PoopWindow(Poop poop, Runnable onClean) {
        this.poop = poop;

        setAlwaysOnTop(true);
        setSize(SIZE, SIZE);
        setLocation(poop.getScreenX(), poop.getScreenY());
        setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            { setOpaque(false); }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawPoop(g2);
                g2.dispose();
            }
        };

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                onClean.run();
            }
        });
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        add(panel);
        setVisible(true);
    }

    private void drawPoop(Graphics2D g2) {
        int cx = SIZE / 2;

        // Bottom blob
        g2.setColor(new Color(139, 90, 43));
        g2.fillOval(cx - 10, 16, 20, 12);

        // Middle blob
        g2.setColor(new Color(149, 100, 53));
        g2.fillOval(cx - 7, 10, 14, 12);

        // Top blob
        g2.setColor(new Color(159, 110, 63));
        g2.fillOval(cx - 4, 5, 9, 10);

        // Tip
        g2.fillOval(cx - 1, 2, 5, 6);

        // Highlight
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillOval(cx - 5, 12, 4, 3);

        // Eyes
        g2.setColor(new Color(30, 30, 30));
        g2.fillOval(cx - 5, 14, 3, 3);
        g2.fillOval(cx + 3, 14, 3, 3);

        // Eye highlights
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 4, 14, 1, 1);
        g2.fillOval(cx + 4, 14, 1, 1);

        // Smile
        g2.setColor(new Color(30, 30, 30));
        g2.setStroke(new BasicStroke(1));
        g2.drawArc(cx - 3, 17, 6, 4, 200, 140);
    }

    public Poop getPoop() { return poop; }
}
