package com.hamster.render;
import com.hamster.model.Accessory;
import com.hamster.model.FoodItem;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.EnumMap;

public class ItemIcon {

    private static final int DEFAULT_SIZE = 36;
    private static final EnumMap<FoodItem, BufferedImage> foodCache = new EnumMap<>(FoodItem.class);
    private static final EnumMap<Accessory, BufferedImage> accessoryCache = new EnumMap<>(Accessory.class);

    public static BufferedImage getFoodIcon(FoodItem food, int size) {
        if (size == DEFAULT_SIZE && foodCache.containsKey(food)) {
            return foodCache.get(food);
        }
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        float s = size / 36f;

        switch (food) {
            case SEED:
                drawSeed(g2, s);
                break;
            case CARROT:
                drawCarrot(g2, s);
                break;
            case APPLE:
                drawApple(g2, s);
                break;
            case CHEESE:
                drawCheese(g2, s);
                break;
            case BROCCOLI:
                drawBroccoli(g2, s);
                break;
            case YOGURT:
                drawYogurt(g2, s);
                break;
            case NUT_MIX:
                drawNutMix(g2, s);
                break;
            case DRIED_FRUIT:
                drawDriedFruit(g2, s);
                break;
            case PREMIUM_PELLET:
                drawPremiumPellet(g2, s);
                break;
            case CAKE:
                drawCake(g2, s);
                break;
        }

        g2.dispose();
        if (size == DEFAULT_SIZE) {
            foodCache.put(food, img);
        }
        return img;
    }

    public static BufferedImage getFoodIcon(FoodItem food) {
        return getFoodIcon(food, DEFAULT_SIZE);
    }

    public static BufferedImage getAccessoryIcon(Accessory acc, int size) {
        if (size == DEFAULT_SIZE && accessoryCache.containsKey(acc)) {
            return accessoryCache.get(acc);
        }
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Scale accessory draw() to fit icon size
        // Original draw coordinates are roughly within a 60x60 area
        float scale = size / 60f;
        AffineTransform saved = g2.getTransform();
        g2.scale(scale, scale);
        acc.draw(g2, 1, 0);
        g2.setTransform(saved);

        g2.dispose();
        if (size == DEFAULT_SIZE) {
            accessoryCache.put(acc, img);
        }
        return img;
    }

    public static BufferedImage getAccessoryIcon(Accessory acc) {
        return getAccessoryIcon(acc, DEFAULT_SIZE);
    }

    // --- Food drawing methods ---

    private static void drawSeed(Graphics2D g2, float s) {
        // Brown seed shape
        g2.setColor(new Color(139, 90, 43));
        int[] sx = {si(10, s), si(18, s), si(22, s), si(18, s)};
        int[] sy = {si(18, s), si(8, s), si(18, s), si(28, s)};
        g2.fillPolygon(sx, sy, 4);
        // Highlight
        g2.setColor(new Color(180, 130, 70));
        g2.fillOval(si(13, s), si(14, s), si(6, s), si(8, s));
        // Second seed
        g2.setColor(new Color(120, 75, 35));
        int[] sx2 = {si(20, s), si(28, s), si(32, s), si(28, s)};
        int[] sy2 = {si(20, s), si(12, s), si(22, s), si(30, s)};
        g2.fillPolygon(sx2, sy2, 4);
        g2.setColor(new Color(165, 120, 60));
        g2.fillOval(si(23, s), si(18, s), si(5, s), si(7, s));
    }

    private static void drawCarrot(Graphics2D g2, float s) {
        // Orange carrot body
        g2.setColor(new Color(255, 140, 0));
        int[] cx = {si(18, s), si(14, s), si(22, s)};
        int[] cy = {si(32, s), si(10, s), si(10, s)};
        g2.fillPolygon(cx, cy, 3);
        // Darker lines
        g2.setColor(new Color(220, 110, 0));
        g2.drawLine(si(16, s), si(16, s), si(20, s), si(16, s));
        g2.drawLine(si(17, s), si(22, s), si(19, s), si(22, s));
        // Green leaves
        g2.setColor(new Color(60, 160, 60));
        g2.fillOval(si(12, s), si(4, s), si(8, s), si(10, s));
        g2.fillOval(si(16, s), si(2, s), si(8, s), si(10, s));
        g2.fillOval(si(20, s), si(5, s), si(6, s), si(9, s));
    }

    private static void drawApple(Graphics2D g2, float s) {
        // Red apple body
        g2.setColor(new Color(220, 40, 40));
        g2.fillOval(si(6, s), si(10, s), si(24, s), si(22, s));
        // Highlight
        g2.setColor(new Color(255, 100, 100));
        g2.fillOval(si(10, s), si(13, s), si(8, s), si(8, s));
        // Stem
        g2.setColor(new Color(100, 70, 30));
        g2.fillRect(si(16, s), si(5, s), si(3, s), si(7, s));
        // Green leaf
        g2.setColor(new Color(60, 180, 60));
        g2.fillOval(si(18, s), si(4, s), si(10, s), si(6, s));
    }

    private static void drawCheese(Graphics2D g2, float s) {
        // Yellow cheese triangle
        g2.setColor(new Color(255, 210, 50));
        int[] tx = {si(4, s), si(32, s), si(32, s)};
        int[] ty = {si(28, s), si(8, s), si(28, s)};
        g2.fillPolygon(tx, ty, 3);
        // Cheese border
        g2.setColor(new Color(220, 180, 30));
        g2.drawPolygon(tx, ty, 3);
        // Holes
        g2.setColor(new Color(240, 195, 40));
        g2.fillOval(si(18, s), si(18, s), si(6, s), si(5, s));
        g2.fillOval(si(24, s), si(22, s), si(4, s), si(4, s));
        g2.fillOval(si(14, s), si(24, s), si(5, s), si(3, s));
    }

    private static void drawBroccoli(Graphics2D g2, float s) {
        // Stem
        g2.setColor(new Color(80, 140, 50));
        g2.fillRect(si(15, s), si(22, s), si(6, s), si(12, s));
        // Green crown
        g2.setColor(new Color(40, 160, 60));
        g2.fillOval(si(6, s), si(8, s), si(12, s), si(14, s));
        g2.fillOval(si(14, s), si(4, s), si(12, s), si(16, s));
        g2.fillOval(si(20, s), si(8, s), si(12, s), si(14, s));
        // Darker top bumps
        g2.setColor(new Color(30, 130, 50));
        g2.fillOval(si(10, s), si(6, s), si(6, s), si(6, s));
        g2.fillOval(si(18, s), si(3, s), si(6, s), si(6, s));
        g2.fillOval(si(24, s), si(7, s), si(5, s), si(5, s));
    }

    private static void drawYogurt(Graphics2D g2, float s) {
        // Cup body
        g2.setColor(new Color(240, 240, 245));
        int[] cupX = {si(8, s), si(10, s), si(26, s), si(28, s)};
        int[] cupY = {si(12, s), si(32, s), si(32, s), si(12, s)};
        g2.fillPolygon(cupX, cupY, 4);
        // Cup rim
        g2.setColor(new Color(200, 200, 210));
        g2.fillRect(si(6, s), si(10, s), si(24, s), si(4, s));
        // Lid foil
        g2.setColor(new Color(180, 180, 200));
        g2.fillRect(si(6, s), si(8, s), si(24, s), si(3, s));
        // Label stripe
        g2.setColor(new Color(150, 200, 255));
        g2.fillRect(si(10, s), si(18, s), si(16, s), si(6, s));
        // Small spoon
        g2.setColor(new Color(190, 190, 195));
        g2.fillRect(si(24, s), si(4, s), si(2, s), si(10, s));
        g2.fillOval(si(22, s), si(2, s), si(6, s), si(4, s));
    }

    private static void drawNutMix(Graphics2D g2, float s) {
        // Multiple nut shapes
        // Peanut
        g2.setColor(new Color(180, 130, 60));
        g2.fillOval(si(4, s), si(14, s), si(10, s), si(7, s));
        g2.fillOval(si(10, s), si(13, s), si(8, s), si(8, s));
        // Almond
        g2.setColor(new Color(160, 110, 40));
        g2.fillOval(si(18, s), si(10, s), si(7, s), si(12, s));
        // Walnut
        g2.setColor(new Color(140, 90, 40));
        g2.fillOval(si(8, s), si(22, s), si(10, s), si(9, s));
        g2.setColor(new Color(120, 75, 30));
        g2.drawLine(si(13, s), si(23, s), si(13, s), si(30, s));
        // Cashew
        g2.setColor(new Color(200, 160, 80));
        g2.fillArc(si(20, s), si(20, s), si(12, s), si(10, s), 30, 180);
        // Highlight
        g2.setColor(new Color(220, 180, 100));
        g2.fillOval(si(6, s), si(16, s), si(3, s), si(3, s));
    }

    private static void drawDriedFruit(Graphics2D g2, float s) {
        // Raisin (dark purple)
        g2.setColor(new Color(80, 30, 80));
        g2.fillOval(si(6, s), si(16, s), si(8, s), si(7, s));
        g2.fillOval(si(12, s), si(14, s), si(7, s), si(8, s));
        // Dried apricot (brown-orange)
        g2.setColor(new Color(200, 120, 40));
        g2.fillOval(si(18, s), si(12, s), si(10, s), si(9, s));
        // Dried cranberry (dark red)
        g2.setColor(new Color(150, 30, 30));
        g2.fillOval(si(8, s), si(24, s), si(7, s), si(6, s));
        // Dried mango (gold)
        g2.setColor(new Color(210, 160, 40));
        g2.fillOval(si(18, s), si(22, s), si(11, s), si(7, s));
        // Highlights
        g2.setColor(new Color(240, 180, 80));
        g2.fillOval(si(20, s), si(14, s), si(3, s), si(3, s));
    }

    private static void drawPremiumPellet(Graphics2D g2, float s) {
        // Gold pellet
        g2.setColor(new Color(220, 180, 40));
        g2.fillOval(si(8, s), si(10, s), si(20, s), si(18, s));
        // Lighter center
        g2.setColor(new Color(255, 220, 80));
        g2.fillOval(si(12, s), si(13, s), si(12, s), si(12, s));
        // Shine highlight
        g2.setColor(new Color(255, 255, 200));
        g2.fillOval(si(14, s), si(14, s), si(5, s), si(4, s));
        // Sparkles
        g2.setColor(new Color(255, 240, 100));
        drawSparkle(g2, si(6, s), si(8, s), si(3, s));
        drawSparkle(g2, si(28, s), si(12, s), si(3, s));
        drawSparkle(g2, si(24, s), si(28, s), si(2, s));
        drawSparkle(g2, si(4, s), si(24, s), si(2, s));
    }

    private static void drawCake(Graphics2D g2, float s) {
        // Cake body (pink)
        g2.setColor(new Color(255, 180, 200));
        g2.fillRoundRect(si(4, s), si(16, s), si(28, s), si(16, s), si(4, s), si(4, s));
        // Frosting layer
        g2.setColor(new Color(255, 220, 230));
        g2.fillRect(si(4, s), si(14, s), si(28, s), si(6, s));
        // Icing drips
        g2.setColor(new Color(255, 240, 245));
        g2.fillOval(si(6, s), si(18, s), si(5, s), si(4, s));
        g2.fillOval(si(14, s), si(19, s), si(4, s), si(3, s));
        g2.fillOval(si(24, s), si(17, s), si(5, s), si(5, s));
        // Candle
        g2.setColor(new Color(255, 230, 100));
        g2.fillRect(si(16, s), si(6, s), si(3, s), si(10, s));
        // Flame
        g2.setColor(new Color(255, 120, 20));
        int[] fx = {si(17, s), si(15, s), si(20, s)};
        int[] fy = {si(2, s), si(8, s), si(8, s)};
        g2.fillPolygon(fx, fy, 3);
        g2.setColor(new Color(255, 200, 50));
        g2.fillOval(si(16, s), si(4, s), si(3, s), si(3, s));
        // Cherry on top
        g2.setColor(new Color(220, 30, 30));
        g2.fillOval(si(22, s), si(10, s), si(6, s), si(6, s));
    }

    private static void drawSparkle(Graphics2D g2, int x, int y, int size) {
        g2.drawLine(x - size, y, x + size, y);
        g2.drawLine(x, y - size, x, y + size);
    }

    private static int si(int val, float scale) {
        return (int)(val * scale);
    }
}
