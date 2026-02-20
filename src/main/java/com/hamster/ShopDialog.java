package com.hamster;

import javax.swing.*;
import java.awt.*;

public class ShopDialog extends JDialog {

    private HamsterColor purchased = null;

    private ShopDialog(int money, int hamsterCount, int maxSlots) {
        super((Frame) null, "햄스터 상점", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setIconImages(HamsterIcon.createIcons());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(255, 250, 240));

        // Title
        JLabel titleLabel = new JLabel("\uD83D\uDCB0 햄스터 상점");
        titleLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 50, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Money display
        JLabel moneyLabel = new JLabel("보유: " + money + " 코인");
        moneyLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 14));
        moneyLabel.setForeground(new Color(100, 80, 50));
        moneyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(moneyLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Max hamster warning
        if (hamsterCount >= maxSlots) {
            JLabel maxLabel = new JLabel("햄스터가 최대입니다! (" + maxSlots + "마리)");
            maxLabel.setFont(new Font("Noto Sans KR", Font.BOLD, 13));
            maxLabel.setForeground(new Color(200, 80, 80));
            maxLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(maxLabel);
            mainPanel.add(Box.createVerticalStrut(10));
        }

        // Color options
        for (HamsterColor color : HamsterColor.values()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.CENTER_ALIGNMENT);
            row.setMaximumSize(new Dimension(300, 40));

            // Color swatch
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
            row.add(swatch);

            JLabel colorLabel = new JLabel(color.getDisplayName() + " - " + color.getShopPrice() + " 코인");
            colorLabel.setFont(new Font("Noto Sans KR", Font.PLAIN, 13));
            colorLabel.setForeground(new Color(80, 50, 20));
            row.add(colorLabel);

            JButton buyBtn = new JButton("구매");
            buyBtn.setFont(new Font("Noto Sans KR", Font.BOLD, 12));
            buyBtn.setFocusPainted(false);
            buyBtn.setBackground(new Color(180, 230, 180));
            buyBtn.setForeground(new Color(40, 80, 40));
            buyBtn.setPreferredSize(new Dimension(60, 28));
            buyBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(120, 180, 120), 1, true),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
            buyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            buyBtn.setEnabled(money >= color.getShopPrice() && hamsterCount < maxSlots);
            buyBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        color.getDisplayName() + " 햄스터를 " + color.getShopPrice() + " 코인에 구매할까요?",
                        "구매 확인",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    purchased = color;
                    dispose();
                }
            });
            row.add(buyBtn);

            mainPanel.add(row);
            mainPanel.add(Box.createVerticalStrut(5));
        }

        mainPanel.add(Box.createVerticalStrut(10));

        // Close button
        JButton closeBtn = new JButton("닫기");
        closeBtn.setFont(new Font("Noto Sans KR", Font.BOLD, 13));
        closeBtn.setFocusPainted(false);
        closeBtn.setBackground(new Color(220, 220, 220));
        closeBtn.setForeground(new Color(80, 50, 20));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.setMaximumSize(new Dimension(280, 36));
        closeBtn.setPreferredSize(new Dimension(280, 36));
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

    public static HamsterColor showAndBuy(int money, int hamsterCount, int maxSlots) {
        ShopDialog dialog = new ShopDialog(money, hamsterCount, maxSlots);
        dialog.setVisible(true);
        return dialog.purchased;
    }
}
