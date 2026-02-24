package com.hamster.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public enum Accessory {
    // HEAD
    RED_RIBBON(Slot.HEAD, "\uBE68\uAC04 \uB9AC\uBCF8", 50, "\uADC0\uC5EC\uC6B4 \uBE68\uAC04 \uB9AC\uBCF8", 0.03, "\u2764\uFE0F"),
    BLUE_RIBBON(Slot.HEAD, "\uD30C\uB780 \uB9AC\uBCF8", 50, "\uC0C1\uCFE8\uD55C \uD30C\uB780 \uB9AC\uBCF8", 0.03, "\uD83D\uDC99"),
    TOP_HAT(Slot.HEAD, "\uC815\uC7A5 \uBAA8\uC790", 100, "\uACA9\uC2DD \uC788\uB294 \uC815\uC7A5 \uBAA8\uC790", 0.10, "\uD83C\uDFA9"),
    PARTY_HAT(Slot.HEAD, "\uD30C\uD2F0 \uBAA8\uC790", 80, "\uCD95\uD558 \uD30C\uD2F0 \uBAA8\uC790", 0.05, "\uD83E\uDD73"),
    CROWN(Slot.HEAD, "\uC655\uAD00", 200, "\uD669\uAE08 \uC655\uAD00", 0.20, "\uD83D\uDC51"),
    BANDANA(Slot.HEAD, "\uB450\uAC74", 70, "\uBA4B\uC9C4 \uB450\uAC74", 0.05, "\uD83E\uDDE2"),
    FLOWER(Slot.HEAD, "\uAF43", 30, "\uC608\uC05C \uAF43 \uC7A5\uC2DD", 0.02, "\uD83C\uDF38"),
    STAR_PIN(Slot.HEAD, "\uBCC4 \uD540", 40, "\uBC18\uC9DD\uC774\uB294 \uBCC4 \uD540", 0.03, "\u2B50"),

    // FACE
    GLASSES(Slot.FACE, "\uC548\uACBD", 60, "\uB181\uAE00 \uC548\uACBD", 0.05, "\uD83D\uDC53"),
    SUNGLASSES(Slot.FACE, "\uC120\uAE00\uB77C\uC2A4", 80, "\uBA4B\uC9C4 \uC120\uAE00\uB77C\uC2A4", 0.08, "\uD83D\uDD76\uFE0F"),
    MONOCLE(Slot.FACE, "\uBAA8\uB178\uD074", 120, "\uACE0\uAE09\uC2A4\uB7EC\uC6B4 \uBAA8\uB178\uD074", 0.12, "\uD83E\uDDD0"),

    // NECK
    SCARF(Slot.NECK, "\uBAA9\uB3C4\uB9AC", 90, "\uB530\uB73B\uD55C \uBAA9\uB3C4\uB9AC", 0.07, "\uD83E\uDDE3"),
    BOW_TIE(Slot.NECK, "\uB098\uBE44 \uB125\uD0C0\uC774", 60, "\uC591\uBCF5\uB2EC\uB9B0 \uB098\uBE44 \uB125\uD0C0\uC774", 0.05, "\uD83D\uDC54"),
    BELL(Slot.NECK, "\uBC29\uC6B8", 45, "\uB531\uB7C9\uB531\uB7C9 \uBC29\uC6B8", 0.03, "\uD83D\uDD14"),

    // BODY
    CAPE(Slot.BODY, "\uB9DD\uD1A0", 150, "\uC601\uC6C5\uC801\uC778 \uB9DD\uD1A0", 0.15, "\uD83E\uDDB8");

    public enum Slot {
        HEAD, FACE, NECK, BODY
    }

    private final Slot slot;
    private final String displayName;
    private final int cost;
    private final String description;
    private final double coinBonus;
    private final String emoji;

    Accessory(Slot slot, String displayName, int cost, String description, double coinBonus, String emoji) {
        this.slot = slot;
        this.displayName = displayName;
        this.cost = cost;
        this.description = description;
        this.coinBonus = coinBonus;
        this.emoji = emoji;
    }

    public Slot getSlot() { return slot; }
    public String getDisplayName() { return displayName; }
    public int getCost() { return cost; }
    public String getDescription() { return description; }
    public double getCoinBonus() { return coinBonus; }
    public String getEmoji() { return emoji; }

    public void draw(Graphics2D g2, int direction, int baseY) {
        AffineTransform saved = g2.getTransform();

        // Flip context: if direction is already handled by caller, we draw normally
        switch (this) {
            // HEAD accessories
            case RED_RIBBON:
                g2.setColor(new Color(220, 50, 50));
                g2.fillOval(22, 2 + baseY, 16, 10);
                g2.setColor(new Color(180, 30, 30));
                g2.fillOval(25, 4 + baseY, 10, 6);
                break;
            case BLUE_RIBBON:
                g2.setColor(new Color(50, 100, 220));
                g2.fillOval(22, 2 + baseY, 16, 10);
                g2.setColor(new Color(30, 70, 180));
                g2.fillOval(25, 4 + baseY, 10, 6);
                break;
            case TOP_HAT:
                g2.setColor(new Color(40, 40, 40));
                g2.fillRect(18, -2 + baseY, 24, 14);
                g2.fillRect(14, 10 + baseY, 32, 4);
                g2.setColor(new Color(180, 140, 20));
                g2.fillRect(18, 8 + baseY, 24, 3);
                break;
            case PARTY_HAT:
                int[] px = {30, 20, 40};
                int[] py = {-4 + baseY, 12 + baseY, 12 + baseY};
                g2.setColor(new Color(255, 100, 150));
                g2.fillPolygon(px, py, 3);
                g2.setColor(new Color(255, 220, 50));
                g2.fillOval(27, -7 + baseY, 6, 6);
                break;
            case CROWN:
                g2.setColor(new Color(255, 200, 0));
                int[] crx = {16, 20, 25, 30, 35, 40, 44, 44, 16};
                int[] cry = {12 + baseY, 0 + baseY, 8 + baseY, -2 + baseY, 8 + baseY, 0 + baseY, 12 + baseY, 12 + baseY, 12 + baseY};
                g2.fillPolygon(crx, cry, crx.length);
                g2.setColor(new Color(220, 50, 50));
                g2.fillOval(28, 2 + baseY, 4, 4);
                g2.setColor(new Color(50, 100, 220));
                g2.fillOval(21, 4 + baseY, 3, 3);
                g2.fillOval(36, 4 + baseY, 3, 3);
                break;
            case BANDANA:
                g2.setColor(new Color(100, 50, 150));
                g2.fillRoundRect(10, 8 + baseY, 40, 8, 4, 4);
                g2.setColor(new Color(80, 30, 130));
                int[] bx = {48, 56, 52};
                int[] by = {10 + baseY, 14 + baseY, 18 + baseY};
                g2.fillPolygon(bx, by, 3);
                break;
            case FLOWER:
                g2.setColor(new Color(255, 150, 180));
                for (int i = 0; i < 5; i++) {
                    double angle = i * Math.PI * 2 / 5;
                    int fx = 25 + (int)(5 * Math.cos(angle));
                    int fy = 4 + baseY + (int)(5 * Math.sin(angle));
                    g2.fillOval(fx, fy, 5, 5);
                }
                g2.setColor(new Color(255, 220, 50));
                g2.fillOval(26, 5 + baseY, 4, 4);
                break;
            case STAR_PIN:
                g2.setColor(new Color(255, 220, 50));
                drawMiniStar(g2, 38, 6 + baseY, 5);
                break;

            // FACE accessories
            case GLASSES:
                g2.setColor(new Color(60, 60, 60));
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(17, 18 + baseY, 12, 10);
                g2.drawOval(33, 18 + baseY, 12, 10);
                g2.drawLine(29, 22 + baseY, 33, 22 + baseY);
                g2.drawLine(17, 22 + baseY, 12, 20 + baseY);
                g2.drawLine(45, 22 + baseY, 50, 20 + baseY);
                g2.setStroke(new BasicStroke(1));
                break;
            case SUNGLASSES:
                g2.setColor(new Color(30, 30, 30));
                g2.fillRoundRect(16, 18 + baseY, 14, 10, 4, 4);
                g2.fillRoundRect(32, 18 + baseY, 14, 10, 4, 4);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(30, 22 + baseY, 32, 22 + baseY);
                g2.drawLine(16, 22 + baseY, 10, 20 + baseY);
                g2.drawLine(46, 22 + baseY, 52, 20 + baseY);
                g2.setStroke(new BasicStroke(1));
                break;
            case MONOCLE:
                g2.setColor(new Color(180, 160, 0));
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(33, 17 + baseY, 12, 12);
                g2.setColor(new Color(200, 200, 200, 100));
                g2.fillOval(34, 18 + baseY, 10, 10);
                g2.setColor(new Color(180, 160, 0));
                g2.drawLine(39, 29 + baseY, 39, 38 + baseY);
                g2.setStroke(new BasicStroke(1));
                break;

            // NECK accessories
            case SCARF:
                g2.setColor(new Color(220, 60, 60));
                g2.fillRoundRect(12, 36 + baseY, 36, 8, 4, 4);
                g2.setColor(new Color(200, 40, 40));
                g2.fillRoundRect(38, 38 + baseY, 8, 14, 3, 3);
                break;
            case BOW_TIE:
                g2.setColor(new Color(50, 50, 200));
                int[] btx1 = {24, 30, 24};
                int[] bty1 = {34 + baseY, 38 + baseY, 42 + baseY};
                g2.fillPolygon(btx1, bty1, 3);
                int[] btx2 = {36, 30, 36};
                int[] bty2 = {34 + baseY, 38 + baseY, 42 + baseY};
                g2.fillPolygon(btx2, bty2, 3);
                g2.setColor(new Color(70, 70, 220));
                g2.fillOval(28, 36 + baseY, 4, 4);
                break;
            case BELL:
                g2.setColor(new Color(255, 200, 0));
                g2.fillOval(26, 36 + baseY, 8, 8);
                g2.setColor(new Color(200, 160, 0));
                g2.drawOval(26, 36 + baseY, 8, 8);
                g2.setColor(new Color(80, 80, 80));
                g2.fillOval(29, 40 + baseY, 3, 3);
                break;

            // BODY accessories
            case CAPE:
                g2.setColor(new Color(180, 30, 30, 180));
                int[] cpx = {10, 5, 15, 50, 55, 50};
                int[] cpy = {30 + baseY, 55 + baseY, 58 + baseY, 58 + baseY, 55 + baseY, 30 + baseY};
                g2.fillPolygon(cpx, cpy, cpx.length);
                g2.setColor(new Color(200, 50, 50));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawPolygon(cpx, cpy, cpx.length);
                g2.setStroke(new BasicStroke(1));
                break;
        }

        g2.setTransform(saved);
    }

    private static void drawMiniStar(Graphics2D g2, int cx, int cy, int size) {
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(cx - size, cy, cx + size, cy);
        g2.drawLine(cx, cy - size, cx, cy + size);
        g2.drawLine(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);
        g2.drawLine(cx + size / 2, cy - size / 2, cx - size / 2, cy + size / 2);
        g2.setStroke(new BasicStroke(1));
    }
}
