package com.hamster.ui;
import com.hamster.model.Accessory;
import com.hamster.model.FoodInventory;
import com.hamster.model.FoodItem;
import com.hamster.model.HamsterColor;
import com.hamster.render.HamsterIcon;
import com.hamster.render.ItemIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ShopDialog extends JDialog {

    public static class ShopResult {
        public boolean boughtHamster;
        public int totalSpent;
        public List<String> newAccessories = new ArrayList<>();
    }

    public interface ShopCallback {
        void onShopClosed(ShopResult result);
    }

    private static ShopDialog activeInstance = null;

    private ShopResult result = new ShopResult();
    private int currentMoney;
    private final FoodInventory foodInventory;
    private final Set<String> globalOwnedAccessories;
    private final JLabel moneyLabel;

    private static final Color BG_COLOR = new Color(255, 248, 235);
    private static final Color SLOT_ACTIVE = new Color(255, 250, 230);
    private static final Color SLOT_EMPTY = new Color(235, 230, 220);
    private static final Color BORDER_ACTIVE = new Color(200, 170, 100);
    private static final Color BORDER_EMPTY = new Color(190, 185, 175);
    private static final Color GOLD_BORDER = new Color(218, 165, 32);
    private static final Color TEXT_COLOR = new Color(80, 50, 20);
    private static final Color SUB_TEXT_COLOR = new Color(100, 80, 50);
    private static final String FONT_NAME = "Noto Sans KR";
    private static final int SLOT_SIZE = 60;
    private static final int GRID_COLS = 5;

    private ShopDialog(int money, int hamsterCount, int maxSlots,
                       FoodInventory foodInventory, Set<String> globalOwnedAccessories,
                       int hamsterPurchaseCount) {
        super((Frame) null, "\uC0C1\uC810", false);
        this.currentMoney = money;
        this.foodInventory = foodInventory;
        this.globalOwnedAccessories = globalOwnedAccessories;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setIconImages(HamsterIcon.createIcons());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 150, 100), 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        mainPanel.setBackground(BG_COLOR);

        // Title
        JLabel titleLabel = new JLabel(ControlPanel.wrapEmoji("\uD83D\uDCB0 \uC0C1\uC810"));
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(4));

        // Money display
        moneyLabel = new JLabel("\uBCF4\uC720: " + currentMoney + " \uCF54\uC778");
        moneyLabel.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        moneyLabel.setForeground(new Color(180, 140, 20));
        moneyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(moneyLabel);
        mainPanel.add(Box.createVerticalStrut(8));

        // Configure tooltips
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setInitialDelay(200);
        ttm.setDismissDelay(30000);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        tabs.setBackground(BG_COLOR);

        tabs.addTab("\uD584\uC2A4\uD130", createHamsterTab(hamsterCount, maxSlots, hamsterPurchaseCount));
        tabs.addTab("\uC74C\uC2DD", createFoodTab());
        tabs.addTab("\uC545\uC138\uC11C\uB9AC", createAccessoryTab());

        mainPanel.add(tabs);
        mainPanel.add(Box.createVerticalStrut(8));

        // Close button
        JButton closeBtn = new JButton("\uB2EB\uAE30");
        closeBtn.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        closeBtn.setFocusPainted(false);
        closeBtn.setBackground(new Color(220, 220, 220));
        closeBtn.setForeground(TEXT_COLOR);
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.setMaximumSize(new Dimension(300, 36));
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        mainPanel.add(closeBtn);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void updateMoneyDisplay() {
        moneyLabel.setText("\uBCF4\uC720: " + currentMoney + " \uCF54\uC778");
    }

    private boolean trySpend(int amount) {
        if (currentMoney >= amount) {
            currentMoney -= amount;
            result.totalSpent += amount;
            updateMoneyDisplay();
            return true;
        }
        return false;
    }

    private JPanel createHamsterTab(int hamsterCount, int maxSlots, int hamsterPurchaseCount) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panel.setBackground(BG_COLOR);

        int price = 100 * (hamsterPurchaseCount + 1);
        boolean slotFull = hamsterCount >= maxSlots;

        if (slotFull) {
            JLabel maxLabel = new JLabel("\uD584\uC2A4\uD130\uAC00 \uCD5C\uB300\uC785\uB2C8\uB2E4! (" + maxSlots + "\uB9C8\uB9AC)");
            maxLabel.setFont(new Font(FONT_NAME, Font.BOLD, 13));
            maxLabel.setForeground(new Color(200, 80, 80));
            maxLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(maxLabel);
            panel.add(Box.createVerticalStrut(10));
        }

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel("\uAD6C\uB9E4 \uBE44\uC6A9: " + price + " \uCF54\uC778");
        priceLabel.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        priceLabel.setForeground(TEXT_COLOR);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(5));

        JLabel descLabel = new JLabel("\uB79C\uB364 \uC0C9\uC0C1\uC758 \uD584\uC2A4\uD130\uAC00 \uD0DC\uC5B4\uB0A9\uB2C8\uB2E4!");
        descLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        descLabel.setForeground(SUB_TEXT_COLOR);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(5));

        JLabel nextLabel = new JLabel("\uB2E4\uC74C \uAD6C\uB9E4: " + (100 * (hamsterPurchaseCount + 2)) + " \uCF54\uC778");
        nextLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
        nextLabel.setForeground(new Color(140, 120, 80));
        nextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(nextLabel);
        infoPanel.add(Box.createVerticalStrut(5));

        // Color swatches preview
        JPanel colorPreview = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        colorPreview.setOpaque(false);
        for (final HamsterColor color : HamsterColor.values()) {
            JPanel swatch = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color.getBody());
                    g2.fillOval(2, 2, 16, 16);
                    g2.setColor(color.getBody().darker());
                    g2.drawOval(2, 2, 16, 16);
                }
            };
            swatch.setOpaque(false);
            swatch.setPreferredSize(new Dimension(20, 20));
            swatch.setToolTipText(color.getDisplayName());
            colorPreview.add(swatch);
        }
        infoPanel.add(colorPreview);
        infoPanel.add(Box.createVerticalStrut(10));
        panel.add(infoPanel);

        // Buy button
        JButton buyBtn = new JButton("\uD584\uC2A4\uD130 \uAD6C\uB9E4 (" + price + "\uCF54\uC778)");
        buyBtn.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        buyBtn.setFocusPainted(false);
        buyBtn.setBackground(new Color(180, 230, 180));
        buyBtn.setForeground(new Color(40, 80, 40));
        buyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyBtn.setMaximumSize(new Dimension(260, 40));
        buyBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 180, 120), 1, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        buyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buyBtn.setEnabled(currentMoney >= price && !slotFull);
        buyBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "\uD584\uC2A4\uD130\uB97C " + price + " \uCF54\uC778\uC5D0 \uAD6C\uB9E4\uD560\uAE4C\uC694?\n(\uB79C\uB364 \uC0C9\uC0C1)",
                    "\uAD6C\uB9E4 \uD655\uC778", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (trySpend(price)) {
                    result.boughtHamster = true;
                    dispose();
                }
            }
        });
        panel.add(buyBtn);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(360, 300));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFoodTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setBackground(BG_COLOR);

        // Grid
        FoodItem[] foods = FoodItem.values();
        int rows = (foods.length + GRID_COLS - 1) / GRID_COLS;
        JPanel grid = new JPanel(new GridLayout(rows, GRID_COLS, 4, 4));
        grid.setOpaque(false);

        for (final FoodItem food : foods) {
            grid.add(createFoodSlot(food));
        }

        // Fill remaining empty slots
        int remainder = (GRID_COLS - (foods.length % GRID_COLS)) % GRID_COLS;
        for (int i = 0; i < remainder; i++) {
            JPanel empty = new JPanel();
            empty.setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
            empty.setBackground(SLOT_EMPTY);
            empty.setBorder(BorderFactory.createLineBorder(BORDER_EMPTY, 1));
            grid.add(empty);
        }

        // Wrap grid to prevent GridLayout stretching (keep cells square)
        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gridWrapper.setOpaque(false);
        gridWrapper.add(grid);
        panel.add(gridWrapper);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(360, 300));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFoodSlot(final FoodItem food) {
        final int count = foodInventory != null ? foodInventory.getCount(food) : 0;
        final boolean canBuy = currentMoney >= food.getCost();

        final JPanel slot = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw food icon centered
                BufferedImage icon = ItemIcon.getFoodIcon(food);
                int x = (getWidth() - icon.getWidth()) / 2;
                int y = 2;
                g2.drawImage(icon, x, y, null);
            }
        };
        slot.setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
        slot.setBackground(SLOT_ACTIVE);
        slot.setBorder(BorderFactory.createLineBorder(BORDER_ACTIVE, 1));

        // Bottom panel: price on left, count on right
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JLabel priceLabel = new JLabel(food.getCost() + "\uCF54\uC778");
        priceLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 8));
        priceLabel.setForeground(canBuy ? new Color(60, 120, 60) : new Color(180, 80, 80));
        priceLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 1, 0));
        bottomPanel.add(priceLabel, BorderLayout.WEST);

        if (count > 0) {
            JLabel countLabel = new JLabel("x" + count);
            countLabel.setFont(new Font(FONT_NAME, Font.BOLD, 9));
            countLabel.setForeground(new Color(100, 70, 30));
            countLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 2));
            bottomPanel.add(countLabel, BorderLayout.EAST);
        }

        slot.add(bottomPanel, BorderLayout.SOUTH);

        // Tooltip
        String tip = "<html><b>" + food.getDisplayName() + "</b> (" + food.getCost() + "\uCF54\uC778)<br>"
                + food.getDescription() + "<br>"
                + "<font color='#448844'>" + food.getEffectText() + "</font>";
        if (count > 0) tip += "<br>\uBCF4\uC720: " + count + "\uAC1C";
        tip += "</html>";
        slot.setToolTipText(tip);

        // Click to buy
        slot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        slot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentMoney >= food.getCost()) {
                    if (trySpend(food.getCost()) && foodInventory != null) {
                        foodInventory.add(food, 1);
                        // Refresh the entire food tab
                        refreshFoodTab();
                    }
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                slot.setBackground(new Color(255, 240, 190));
                slot.setBorder(BorderFactory.createLineBorder(new Color(230, 180, 60), 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                slot.setBackground(SLOT_ACTIVE);
                slot.setBorder(BorderFactory.createLineBorder(BORDER_ACTIVE, 1));
            }
        });

        return slot;
    }

    private JTabbedPane findTabbedPane() {
        for (Component c : ((JPanel) getContentPane()).getComponents()) {
            if (c instanceof JTabbedPane) return (JTabbedPane) c;
        }
        return null;
    }

    private void refreshFoodTab() {
        JTabbedPane tabs = findTabbedPane();
        if (tabs == null) return;
        int idx = tabs.indexOfTab("\uC74C\uC2DD");
        if (idx >= 0) {
            tabs.setComponentAt(idx, createFoodTab());
            tabs.revalidate();
            tabs.repaint();
        }
    }

    private void refreshAccessoryTab() {
        JTabbedPane tabs = findTabbedPane();
        if (tabs == null) return;
        int idx = tabs.indexOfTab("\uC545\uC138\uC11C\uB9AC");
        if (idx >= 0) {
            tabs.setComponentAt(idx, createAccessoryTab());
            tabs.revalidate();
            tabs.repaint();
        }
    }

    private JPanel createAccessoryTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setBackground(BG_COLOR);

        // Group accessories by slot
        Accessory.Slot currentSlot = null;
        List<Accessory> slotItems = new ArrayList<>();

        for (Accessory acc : Accessory.values()) {
            if (currentSlot != null && acc.getSlot() != currentSlot) {
                // Add header + grid for previous slot
                addSlotSection(panel, currentSlot, slotItems);
                slotItems = new ArrayList<>();
            }
            currentSlot = acc.getSlot();
            slotItems.add(acc);
        }
        // Add last slot
        if (currentSlot != null && !slotItems.isEmpty()) {
            addSlotSection(panel, currentSlot, slotItems);
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(360, 300));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void addSlotSection(JPanel parent, Accessory.Slot slot, List<Accessory> items) {
        String slotDisplay = UIHelper.getSlotDisplayName(slot);
        JLabel slotLabel = new JLabel("\u2500 " + slotDisplay + " \u2500");
        slotLabel.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        slotLabel.setForeground(new Color(120, 90, 40));
        slotLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        parent.add(slotLabel);
        parent.add(Box.createVerticalStrut(3));

        int cols = Math.min(GRID_COLS, items.size());
        int rows = (items.size() + cols - 1) / cols;
        JPanel grid = new JPanel(new GridLayout(rows, GRID_COLS, 4, 4));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (Accessory acc : items) {
            grid.add(createAccessorySlot(acc));
        }

        // Fill remaining
        int remainder = (GRID_COLS - (items.size() % GRID_COLS)) % GRID_COLS;
        for (int i = 0; i < remainder; i++) {
            JPanel empty = new JPanel();
            empty.setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
            empty.setBackground(SLOT_EMPTY);
            empty.setBorder(BorderFactory.createLineBorder(BORDER_EMPTY, 1));
            grid.add(empty);
        }

        // Wrap grid to prevent stretching
        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gridWrapper.setOpaque(false);
        gridWrapper.add(grid);
        parent.add(gridWrapper);
        parent.add(Box.createVerticalStrut(6));
    }

    private JPanel createAccessorySlot(final Accessory acc) {
        final boolean owned = globalOwnedAccessories != null && globalOwnedAccessories.contains(acc.name());
        final boolean canBuy = currentMoney >= acc.getCost();

        final JPanel slot = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                BufferedImage icon = ItemIcon.getAccessoryIcon(acc);
                int x = (getWidth() - icon.getWidth()) / 2;
                int y = 2;
                g2.drawImage(icon, x, y, null);
            }
        };
        slot.setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));

        if (owned) {
            slot.setBackground(new Color(255, 248, 220));
            slot.setBorder(BorderFactory.createLineBorder(GOLD_BORDER, 2));
        } else {
            slot.setBackground(SLOT_ACTIVE);
            slot.setBorder(BorderFactory.createLineBorder(BORDER_ACTIVE, 1));
        }

        // Bottom
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        if (owned) {
            JLabel ownedLabel = new JLabel("\uBCF4\uC720", SwingConstants.CENTER);
            ownedLabel.setFont(new Font(FONT_NAME, Font.BOLD, 9));
            ownedLabel.setForeground(GOLD_BORDER);
            ownedLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
            bottomPanel.add(ownedLabel, BorderLayout.CENTER);
        } else {
            JLabel priceLabel = new JLabel(acc.getCost() + "\uCF54\uC778", SwingConstants.CENTER);
            priceLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 8));
            priceLabel.setForeground(canBuy ? new Color(60, 120, 60) : new Color(180, 80, 80));
            priceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
            bottomPanel.add(priceLabel, BorderLayout.CENTER);
        }

        slot.add(bottomPanel, BorderLayout.SOUTH);

        // Tooltip
        String coinText = String.format("\uCF54\uC778+%d%%", (int)(acc.getCoinBonus() * 100));
        String tip = "<html><b>" + acc.getDisplayName() + "</b><br>"
                + acc.getDescription() + "<br>"
                + "<font color='#448844'>" + coinText + "</font><br>"
                + "\uAC00\uACA9: " + acc.getCost() + "\uCF54\uC778";
        if (owned) tip += "<br><b><font color='#B8860B'>\uBCF4\uC720\uC911</font></b>";
        tip += "</html>";
        slot.setToolTipText(tip);

        // Click to buy (if not owned)
        if (!owned) {
            slot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            slot.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (currentMoney >= acc.getCost()) {
                        if (trySpend(acc.getCost())) {
                            result.newAccessories.add(acc.name());
                            if (globalOwnedAccessories != null) {
                                globalOwnedAccessories.add(acc.name());
                            }
                            refreshAccessoryTab();
                        }
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    slot.setBackground(new Color(255, 240, 190));
                    slot.setBorder(BorderFactory.createLineBorder(new Color(230, 180, 60), 2));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    slot.setBackground(SLOT_ACTIVE);
                    slot.setBorder(BorderFactory.createLineBorder(BORDER_ACTIVE, 1));
                }
            });
        }

        return slot;
    }

    public static void showAndBuy(int money, int hamsterCount, int maxSlots,
                                   FoodInventory foodInventory,
                                   Set<String> globalOwnedAccessories,
                                   int hamsterPurchaseCount,
                                   ShopCallback callback) {
        if (activeInstance != null && activeInstance.isVisible()) {
            activeInstance.toFront();
            return;
        }
        ShopDialog dialog = new ShopDialog(money, hamsterCount, maxSlots,
                foodInventory, globalOwnedAccessories, hamsterPurchaseCount);
        activeInstance = dialog;
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                activeInstance = null;
                if (callback != null) callback.onShopClosed(dialog.result);
            }
        });
        dialog.setVisible(true);
    }
}
