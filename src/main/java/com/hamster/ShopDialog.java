package com.hamster;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ShopDialog extends JDialog {

    public static class ShopResult {
        public boolean boughtHamster;
        public int totalSpent;
        public List<String> newAccessories = new ArrayList<>();
    }

    private ShopResult result = new ShopResult();
    private int currentMoney;
    private final FoodInventory foodInventory;
    private final Set<String> globalOwnedAccessories;
    private final JLabel moneyLabel;

    private ShopDialog(int money, int hamsterCount, int maxSlots,
                       FoodInventory foodInventory, Set<String> globalOwnedAccessories,
                       int hamsterPurchaseCount) {
        super((Frame) null, "\uC0C1\uC810", true);
        this.currentMoney = money;
        this.foodInventory = foodInventory;
        this.globalOwnedAccessories = globalOwnedAccessories;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setIconImages(HamsterIcon.createIcons());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(255, 250, 240));

        // Title
        JLabel titleLabel = new JLabel(ControlPanel.wrapEmoji("\uD83D\uDCB0 \uC0C1\uC810"));
        titleLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));

        // Money display
        moneyLabel = new JLabel("\uBCF4\uC720: " + currentMoney + " \uCF54\uC778");
        moneyLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 14));
        moneyLabel.setForeground(new Color(100, 80, 50));
        moneyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(moneyLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Noto Sans KR", Font.BOLD, 13));

        // Tab 1: Hamsters (single buy, escalating price, random color)
        tabs.addTab("\uD584\uC2A4\uD130", createHamsterTab(hamsterCount, maxSlots, hamsterPurchaseCount));

        // Tab 2: Food
        tabs.addTab("\uC74C\uC2DD", createFoodTab());

        // Tab 3: Accessories
        tabs.addTab("\uC545\uC138\uC11C\uB9AC", createAccessoryTab());

        mainPanel.add(tabs);
        mainPanel.add(Box.createVerticalStrut(10));

        // Close button
        JButton closeBtn = new JButton("\uB2EB\uAE30");
        closeBtn.setFont(new Font("Noto Sans KR", Font.BOLD, 13));
        closeBtn.setFocusPainted(false);
        closeBtn.setBackground(new Color(220, 220, 220));
        closeBtn.setForeground(new Color(80, 50, 20));
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
        panel.setBackground(new Color(255, 250, 240));

        int price = 100 * (hamsterPurchaseCount + 1);
        boolean slotFull = hamsterCount >= maxSlots;

        if (slotFull) {
            JLabel maxLabel = new JLabel("\uD584\uC2A4\uD130\uAC00 \uCD5C\uB300\uC785\uB2C8\uB2E4! (" + maxSlots + "\uB9C8\uB9AC)");
            maxLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 13));
            maxLabel.setForeground(new Color(200, 80, 80));
            maxLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(maxLabel);
            panel.add(Box.createVerticalStrut(10));
        }

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel("\uAD6C\uB9E4 \uBE44\uC6A9: " + price + " \uCF54\uC778");
        priceLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 14));
        priceLabel.setForeground(new Color(80, 50, 20));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(5));

        JLabel descLabel = new JLabel("\uB79C\uB364 \uC0C9\uC0C1\uC758 \uD584\uC2A4\uD130\uAC00 \uD0DC\uC5B4\uB0A9\uB2C8\uB2E4!");
        descLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 80, 50));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(5));

        JLabel nextLabel = new JLabel("\uB2E4\uC74C \uAD6C\uB9E4: " + (100 * (hamsterPurchaseCount + 2)) + " \uCF54\uC778");
        nextLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 11));
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
        buyBtn.setFont(new Font("Noto Sans KR", Font.BOLD, 14));
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
        scroll.setPreferredSize(new Dimension(340, 300));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(255, 250, 240));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFoodTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panel.setBackground(new Color(255, 250, 240));

        for (final FoodItem food : FoodItem.values()) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.CENTER_ALIGNMENT);
            row.setMaximumSize(new Dimension(320, 45));

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);
            JLabel nameLabel = new JLabel(food.getDisplayName() + " (" + food.getCost() + "\uCF54\uC778)");
            nameLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 12));
            nameLabel.setForeground(new Color(80, 50, 20));
            infoPanel.add(nameLabel);
            JLabel effectLabel = new JLabel(food.getEffectText());
            effectLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 10));
            effectLabel.setForeground(new Color(100, 80, 50));
            infoPanel.add(effectLabel);
            row.add(infoPanel, BorderLayout.CENTER);

            int owned = foodInventory != null ? foodInventory.getCount(food) : 0;
            final JLabel ownedLabel = new JLabel("x" + owned);
            ownedLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 11));
            ownedLabel.setForeground(new Color(120, 100, 60));

            JButton buyBtn = new JButton("\uAD6C\uB9E4");
            buyBtn.setFont(new Font("Noto Sans KR", Font.BOLD, 11));
            buyBtn.setFocusPainted(false);
            buyBtn.setBackground(new Color(180, 230, 180));
            buyBtn.setForeground(new Color(40, 80, 40));
            buyBtn.setPreferredSize(new Dimension(55, 26));
            buyBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(120, 180, 120), 1, true),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));
            buyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            buyBtn.setEnabled(currentMoney >= food.getCost());
            buyBtn.addActionListener(e -> {
                if (trySpend(food.getCost()) && foodInventory != null) {
                    foodInventory.add(food, 1);
                    ownedLabel.setText("x" + foodInventory.getCount(food));
                    buyBtn.setEnabled(currentMoney >= food.getCost());
                }
            });

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(ownedLabel);
            rightPanel.add(buyBtn);
            row.add(rightPanel, BorderLayout.EAST);

            panel.add(row);
            panel.add(Box.createVerticalStrut(4));
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(340, 300));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(255, 250, 240));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createAccessoryTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panel.setBackground(new Color(255, 250, 240));

        String currentSlot = "";
        for (final Accessory acc : Accessory.values()) {
            String slotName = acc.getSlot().name();
            if (!slotName.equals(currentSlot)) {
                currentSlot = slotName;
                String slotDisplay;
                switch (acc.getSlot()) {
                    case HEAD: slotDisplay = "\uBA38\uB9AC"; break;
                    case FACE: slotDisplay = "\uC5BC\uAD74"; break;
                    case NECK: slotDisplay = "\uBAA9"; break;
                    case BODY: slotDisplay = "\uBAB8"; break;
                    default: slotDisplay = slotName;
                }
                JLabel slotLabel = new JLabel("\u2500 " + slotDisplay + " \u2500");
                slotLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 12));
                slotLabel.setForeground(new Color(120, 90, 40));
                slotLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(slotLabel);
                panel.add(Box.createVerticalStrut(4));
            }

            boolean alreadyOwned = globalOwnedAccessories != null && globalOwnedAccessories.contains(acc.name());

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.CENTER_ALIGNMENT);
            row.setMaximumSize(new Dimension(320, 35));

            String coinText = String.format(" (\uCF54\uC778+%d%%)", (int)(acc.getCoinBonus() * 100));
            JLabel nameLabel = new JLabel(acc.getDisplayName() + " - " + acc.getCost() + "\uCF54\uC778" + coinText);
            nameLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 12));
            nameLabel.setForeground(new Color(80, 50, 20));
            row.add(nameLabel, BorderLayout.CENTER);

            if (alreadyOwned) {
                JLabel ownedLabel = new JLabel("\uBCF4\uC720\uC911");
                ownedLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 11));
                ownedLabel.setForeground(new Color(60, 140, 60));
                row.add(ownedLabel, BorderLayout.EAST);
            } else {
                JButton buyBtn = new JButton("\uAD6C\uB9E4");
                buyBtn.setFont(new Font("Noto Sans KR", Font.BOLD, 11));
                buyBtn.setFocusPainted(false);
                buyBtn.setBackground(new Color(200, 210, 255));
                buyBtn.setForeground(new Color(40, 40, 100));
                buyBtn.setPreferredSize(new Dimension(55, 26));
                buyBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(140, 150, 200), 1, true),
                        BorderFactory.createEmptyBorder(2, 6, 2, 6)
                ));
                buyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                buyBtn.setEnabled(currentMoney >= acc.getCost());
                buyBtn.addActionListener(e -> {
                    if (trySpend(acc.getCost())) {
                        result.newAccessories.add(acc.name());
                        if (globalOwnedAccessories != null) {
                            globalOwnedAccessories.add(acc.name());
                        }
                        buyBtn.setEnabled(false);
                        buyBtn.setText("\uBCF4\uC720");
                    }
                });
                row.add(buyBtn, BorderLayout.EAST);
            }

            panel.add(row);
            panel.add(Box.createVerticalStrut(3));
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(340, 300));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(255, 250, 240));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    public static ShopResult showAndBuy(int money, int hamsterCount, int maxSlots,
                                        FoodInventory foodInventory,
                                        Set<String> globalOwnedAccessories,
                                        int hamsterPurchaseCount) {
        ShopDialog dialog = new ShopDialog(money, hamsterCount, maxSlots,
                foodInventory, globalOwnedAccessories, hamsterPurchaseCount);
        dialog.setVisible(true);
        return dialog.result;
    }
}
