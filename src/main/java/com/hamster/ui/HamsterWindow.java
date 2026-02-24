package com.hamster.ui;
import com.hamster.model.Hamster;
import com.hamster.render.HamsterRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HamsterWindow extends JWindow {

    private static final int WINDOW_WIDTH = 80;
    private static final int WINDOW_HEIGHT = 100;
    private static final int WHEEL_WIDTH = 150;
    private static final int WHEEL_HEIGHT = 140;

    private final Hamster hamster;
    private final HamsterPanel hamsterPanel;

    private final Dimension screenSize;
    private final Insets screenInsets;

    private boolean dragging = false;
    private boolean didDrag = false;
    private int dragOffsetX, dragOffsetY;
    private Hamster.State lastState = Hamster.State.IDLE;

    public HamsterWindow(Hamster hamster) {
        this.hamster = hamster;

        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());

        setAlwaysOnTop(true);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setBackground(new Color(0, 0, 0, 0));

        hamsterPanel = new HamsterPanel();
        add(hamsterPanel);

        setupMouse();
        setVisible(true);
    }

    public void tick() {
        if (hamster.isFrozen()) {
            hamsterPanel.repaint();
            return;
        }

        hamster.update();

        // Handle window resize for wheel state transitions
        Hamster.State currentState = hamster.getState();
        if (currentState == Hamster.State.RUNNING_WHEEL && lastState != Hamster.State.RUNNING_WHEEL) {
            Point loc = getLocation();
            setSize(WHEEL_WIDTH, WHEEL_HEIGHT);
            setLocation(
                    loc.x - (WHEEL_WIDTH - WINDOW_WIDTH) / 2,
                    loc.y - (WHEEL_HEIGHT - WINDOW_HEIGHT));
        } else if (currentState != Hamster.State.RUNNING_WHEEL && lastState == Hamster.State.RUNNING_WHEEL) {
            Point loc = getLocation();
            setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            setLocation(
                    loc.x + (WHEEL_WIDTH - WINDOW_WIDTH) / 2,
                    loc.y + (WHEEL_HEIGHT - WINDOW_HEIGHT));
        }
        lastState = currentState;

        // move window when hamster walks
        if (hamster.getState() == Hamster.State.WALKING && !dragging) {
            Point loc = getLocation();
            int newX = loc.x + (int) Math.round(hamster.getMoveX());
            int newY = loc.y + (int) Math.round(hamster.getMoveY());
            int maxX = screenSize.width - getWidth();
            int maxY = screenSize.height - screenInsets.bottom - getHeight();
            if (newX <= 0 || newX >= maxX) {
                hamster.bounceX();
                newX = Math.max(0, Math.min(newX, maxX));
            }
            if (newY <= 0 || newY >= maxY) {
                hamster.bounceY();
                newY = Math.max(0, Math.min(newY, maxY));
            }
            setLocation(newX, newY);
        }

        hamsterPanel.repaint();
    }

    public Hamster getHamster() {
        return hamster;
    }

    public Point getHamsterScreenPosition() {
        Point loc = getLocation();
        return new Point(loc.x + getWidth() / 2, loc.y + getHeight());
    }

    private void setupMouse() {
        hamsterPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragging = true;
                    didDrag = false;
                    dragOffsetX = e.getXOnScreen() - getX();
                    dragOffsetY = e.getYOnScreen() - getY();
                    hamster.wake();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1 && !didDrag) {
                    String newName = JOptionPane.showInputDialog(
                            null, "\ud584\uc2a4\ud130 \uc774\ub984\uc744 \uc785\ub825\ud558\uc138\uc694:", hamster.getName());
                    if (newName != null && !newName.trim().isEmpty()) {
                        hamster.setName(newName.trim());
                    }
                }
            }
        });

        hamsterPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    didDrag = true;
                    setLocation(
                            e.getXOnScreen() - dragOffsetX,
                            e.getYOnScreen() - dragOffsetY
                    );
                }
            }
        });
    }

    // custom panel with transparent background
    private class HamsterPanel extends JPanel {
        HamsterPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            if (hamster.getState() == Hamster.State.RUNNING_WHEEL) {
                g2.translate(5, 15);
            } else {
                g2.translate(10, 28);
            }
            HamsterRenderer.draw(g2, hamster);
            g2.dispose();
        }
    }
}
