package com.hamster.ui;
import com.hamster.model.GameConstants;
import com.hamster.model.Hamster;
import com.hamster.render.HamsterRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HamsterWindow extends JWindow {

    private static final int WINDOW_WIDTH = GameConstants.HAMSTER_WINDOW_WIDTH;
    private static final int WINDOW_HEIGHT = GameConstants.HAMSTER_WINDOW_HEIGHT;
    private static final int WHEEL_WIDTH = GameConstants.WHEEL_WINDOW_WIDTH;
    private static final int WHEEL_HEIGHT = GameConstants.WHEEL_WINDOW_HEIGHT;

    /**
     * Callback interface for right-click context menu actions.
     */
    public interface ContextMenuCallback {
        void onFeed(Hamster h);
        void onPlay(Hamster h);
        void onRunWheel(Hamster h);
        void onSleep(Hamster h);
        void onEquipAccessory(Hamster h);
        void onKill(Hamster h);
    }

    private final Hamster hamster;
    private final HamsterPanel hamsterPanel;
    private ContextMenuCallback contextMenuCallback;

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

    public void setContextMenuCallback(ContextMenuCallback callback) {
        this.contextMenuCallback = callback;
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
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragging = false;
                }
                // Right-click context menu
                if (SwingUtilities.isRightMouseButton(e) && contextMenuCallback != null && !hamster.isDead()) {
                    showContextMenu(e);
                }
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

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(255, 250, 240));

        // Header: hamster name
        JMenuItem header = new JMenuItem(ControlPanel.wrapEmoji("\uD83D\uDC39 " + hamster.getName()));
        header.setFont(new Font("Noto Sans KR", Font.BOLD, 12));
        header.setEnabled(false);
        menu.add(header);
        menu.addSeparator();

        JMenuItem feedItem = new JMenuItem(ControlPanel.wrapEmoji("\uD83C\uDF7D\uFE0F \uBC25\uC8FC\uAE30"));
        feedItem.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
        feedItem.addActionListener(ev -> contextMenuCallback.onFeed(hamster));
        menu.add(feedItem);

        JMenuItem playItem = new JMenuItem(ControlPanel.wrapEmoji("\uD83C\uDFB5 \uB180\uAE30"));
        playItem.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
        playItem.addActionListener(ev -> contextMenuCallback.onPlay(hamster));
        menu.add(playItem);

        JMenuItem wheelItem = new JMenuItem(ControlPanel.wrapEmoji("\uD83C\uDFA1 \uCCC7\uBC14\uD034"));
        wheelItem.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
        wheelItem.addActionListener(ev -> contextMenuCallback.onRunWheel(hamster));
        menu.add(wheelItem);

        JMenuItem sleepItem = new JMenuItem(ControlPanel.wrapEmoji("\uD83D\uDCA4 \uC7A0\uC790\uAE30"));
        sleepItem.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
        sleepItem.addActionListener(ev -> contextMenuCallback.onSleep(hamster));
        menu.add(sleepItem);

        menu.addSeparator();

        JMenuItem equipItem = new JMenuItem(ControlPanel.wrapEmoji("\uD83C\uDFA8 \uCE58\uC7A5"));
        equipItem.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
        equipItem.addActionListener(ev -> contextMenuCallback.onEquipAccessory(hamster));
        menu.add(equipItem);

        menu.addSeparator();

        // Info: stats display
        JMenuItem infoItem = new JMenuItem(String.format(
                "\uBC30\uACE0\uD514:%d  \uD589\uBCF5:%d  \uCCB4\uB825:%d  (%d\uC77C)",
                hamster.getHunger(), hamster.getHappiness(), hamster.getEnergy(), hamster.getAgeDays()));
        infoItem.setFont(new Font("Noto Sans KR", Font.PLAIN, 10));
        infoItem.setEnabled(false);
        menu.add(infoItem);

        menu.addSeparator();

        JMenuItem killItem = new JMenuItem("\uBCF4\uB0B4\uAE30");
        killItem.setFont(new Font("Noto Sans KR", Font.PLAIN, 11));
        killItem.setForeground(new Color(200, 80, 80));
        killItem.addActionListener(ev -> contextMenuCallback.onKill(hamster));
        menu.add(killItem);

        // Show the popup using a temporary dialog as anchor (JWindow doesn't support JPopupMenu directly)
        final JDialog anchor = new JDialog();
        anchor.setUndecorated(true);
        anchor.setSize(1, 1);
        anchor.setAlwaysOnTop(true);
        anchor.setLocation(e.getXOnScreen(), e.getYOnScreen());
        anchor.setVisible(true);

        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {}
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) { anchor.dispose(); }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) { anchor.dispose(); }
        });

        menu.show(anchor, 0, 0);
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
