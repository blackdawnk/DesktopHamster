package com.hamster;

import java.awt.*;
import java.awt.geom.*;

public class HamsterRenderer {

    private static final Color EYE_COLOR = new Color(30, 30, 30);
    private static final Color SEED_COLOR = new Color(160, 130, 60);

    private static final String FONT_NAME = "Noto Sans KR";

    public static void draw(Graphics2D g2, Hamster hamster) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int dir = hamster.getDirection();
        int frame = hamster.getAnimFrame();
        Hamster.State state = hamster.getState();
        HamsterColor palette = hamster.getColor();

        AffineTransform original = g2.getTransform();

        // Wheel state has its own complete rendering
        if (state == Hamster.State.RUNNING_WHEEL) {
            drawWheelScene(g2, hamster);
            g2.setTransform(original);
            return;
        }

        // flip horizontally if facing left
        if (dir == -1) {
            g2.translate(60, 0);
            g2.scale(-1, 1);
        }

        // bounce animation for walking
        int bounceY = 0;
        if (state == Hamster.State.WALKING) {
            bounceY = (int)(Math.sin(frame * 0.3) * 3);
        }

        int baseY = bounceY;

        // --- shadow ---
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillOval(5, 52 - bounceY, 50, 10);

        // --- tail ---
        g2.setColor(palette.getTailAndLegs());
        g2.fillOval(-2, 30 + baseY, 12, 10);

        // --- body ---
        g2.setColor(palette.getBody());
        g2.fillOval(5, 18 + baseY, 50, 38);

        // belly
        g2.setColor(palette.getBelly());
        g2.fillOval(15, 28 + baseY, 30, 26);

        // --- legs ---
        drawLegs(g2, frame, state, baseY, palette);

        // --- ears ---
        // left ear
        g2.setColor(palette.getBody());
        g2.fillOval(12, 4 + baseY, 14, 16);
        g2.setColor(palette.getEarInner());
        g2.fillOval(15, 7 + baseY, 8, 10);

        // right ear
        g2.setColor(palette.getBody());
        g2.fillOval(34, 4 + baseY, 14, 16);
        g2.setColor(palette.getEarInner());
        g2.fillOval(37, 7 + baseY, 8, 10);

        // --- head ---
        g2.setColor(palette.getBody());
        g2.fillOval(10, 10 + baseY, 40, 30);

        // --- face ---
        drawFace(g2, state, frame, baseY, palette);

        // --- eating: seed ---
        if (state == Hamster.State.EATING) {
            drawSeed(g2, frame, baseY);
        }

        // --- sleeping: zzz ---
        if (state == Hamster.State.SLEEPING) {
            drawZzz(g2, frame, baseY);
        }

        // --- happy: sparkles ---
        if (state == Hamster.State.HAPPY) {
            drawSparkles(g2, frame, baseY);
        }

        g2.setTransform(original);

        // --- name above head (drawn after transform reset so text is never flipped) ---
        drawName(g2, hamster.getName(), 30, baseY - 10);
    }

    private static void drawLegs(Graphics2D g2, int frame, Hamster.State state, int baseY, HamsterColor palette) {
        g2.setColor(palette.getTailAndLegs());
        int legAnim = 0;
        if (state == Hamster.State.WALKING) {
            legAnim = (int)(Math.sin(frame * 0.4) * 4);
        } else if (state == Hamster.State.RUNNING_WHEEL) {
            legAnim = (int)(Math.sin(frame * 0.8) * 5);
        }
        // front legs
        g2.fillRoundRect(15 + legAnim, 46 + baseY, 8, 10, 4, 4);
        g2.fillRoundRect(37 - legAnim, 46 + baseY, 8, 10, 4, 4);
        // back legs
        g2.fillRoundRect(10 - legAnim, 44 + baseY, 10, 10, 5, 5);
        g2.fillRoundRect(40 + legAnim, 44 + baseY, 10, 10, 5, 5);

        // paws
        g2.setColor(palette.getBelly());
        g2.fillOval(15 + legAnim, 52 + baseY, 8, 5);
        g2.fillOval(37 - legAnim, 52 + baseY, 8, 5);
    }

    private static void drawFace(Graphics2D g2, Hamster.State state, int frame, int baseY, HamsterColor palette) {
        if (state == Hamster.State.SLEEPING) {
            // closed eyes (lines)
            g2.setColor(EYE_COLOR);
            g2.setStroke(new BasicStroke(2));
            g2.drawArc(19, 20 + baseY, 8, 6, 0, 180);
            g2.drawArc(35, 20 + baseY, 8, 6, 0, 180);
            g2.setStroke(new BasicStroke(1));
        } else if (state == Hamster.State.HAPPY) {
            // happy eyes (^_^)
            g2.setColor(EYE_COLOR);
            g2.setStroke(new BasicStroke(2));
            g2.drawArc(19, 18 + baseY, 8, 8, 0, 180);
            g2.drawArc(35, 18 + baseY, 8, 8, 0, 180);
            g2.setStroke(new BasicStroke(1));
        } else if (state == Hamster.State.RUNNING_WHEEL) {
            // determined/focused eyes with eyebrows
            g2.setColor(EYE_COLOR);
            g2.fillOval(21, 19 + baseY, 6, 7);
            g2.fillOval(37, 19 + baseY, 6, 7);
            g2.setColor(Color.WHITE);
            g2.fillOval(23, 20 + baseY, 3, 3);
            g2.fillOval(39, 20 + baseY, 3, 3);
            // small determined eyebrows
            g2.setColor(palette.getEyebrow());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(20, 17 + baseY, 27, 16 + baseY);
            g2.drawLine(42, 17 + baseY, 35, 16 + baseY);
            g2.setStroke(new BasicStroke(1));
        } else {
            // normal eyes with blink
            g2.setColor(EYE_COLOR);
            boolean blink = (frame % 120 > 115);
            if (blink) {
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(20, 23 + baseY, 26, 23 + baseY);
                g2.drawLine(36, 23 + baseY, 42, 23 + baseY);
                g2.setStroke(new BasicStroke(1));
            } else {
                g2.fillOval(21, 19 + baseY, 6, 7);
                g2.fillOval(37, 19 + baseY, 6, 7);
                // eye highlights
                g2.setColor(Color.WHITE);
                g2.fillOval(23, 20 + baseY, 3, 3);
                g2.fillOval(39, 20 + baseY, 3, 3);
            }
        }

        // cheeks
        g2.setColor(palette.getCheek());
        g2.fillOval(12, 25 + baseY, 10, 8);
        g2.fillOval(38, 25 + baseY, 10, 8);

        // cheek stuffed when eating
        if (state == Hamster.State.EATING) {
            g2.setColor(palette.getStuffedCheek());
            g2.fillOval(10, 22 + baseY, 14, 14);
            g2.fillOval(36, 22 + baseY, 14, 14);
        }

        // nose
        g2.setColor(palette.getNose());
        g2.fillOval(28, 27 + baseY, 5, 4);

        // mouth
        g2.setColor(palette.getNose().darker());
        g2.setStroke(new BasicStroke(1));
        if (state == Hamster.State.EATING) {
            // open mouth
            g2.drawOval(27, 31 + baseY, 6, 4);
        } else {
            g2.drawArc(26, 29 + baseY, 4, 4, 200, 140);
            g2.drawArc(30, 29 + baseY, 4, 4, 200, 140);
        }

        // whiskers
        g2.setColor(palette.getWhisker());
        g2.setStroke(new BasicStroke(0.8f));
        // left whiskers
        g2.drawLine(14, 28 + baseY, 2, 25 + baseY);
        g2.drawLine(14, 30 + baseY, 2, 30 + baseY);
        g2.drawLine(14, 32 + baseY, 2, 35 + baseY);
        // right whiskers
        g2.drawLine(46, 28 + baseY, 58, 25 + baseY);
        g2.drawLine(46, 30 + baseY, 58, 30 + baseY);
        g2.drawLine(46, 32 + baseY, 58, 35 + baseY);
        g2.setStroke(new BasicStroke(1));
    }

    private static void drawSeed(Graphics2D g2, int frame, int baseY) {
        int seedY = baseY + 32;
        if (frame % 20 < 10) seedY -= 1;

        g2.setColor(SEED_COLOR);
        g2.fillOval(28, seedY, 6, 8);
        g2.setColor(SEED_COLOR.darker());
        g2.drawLine(30, seedY, 32, seedY + 4);
    }

    private static void drawZzz(Graphics2D g2, int frame, int baseY) {
        g2.setColor(new Color(100, 150, 255, 180));
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        int drift = (frame / 30) % 3;
        int alpha1 = 180 - drift * 40;
        int alpha2 = 140 - drift * 30;
        int alpha3 = 100 - drift * 20;

        g2.setColor(new Color(100, 150, 255, Math.max(0, alpha1)));
        g2.drawString("z", 48, 14 + baseY - drift * 2);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(100, 150, 255, Math.max(0, alpha2)));
        g2.drawString("z", 54, 6 + baseY - drift * 3);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(new Color(100, 150, 255, Math.max(0, alpha3)));
        g2.drawString("Z", 58, -3 + baseY - drift * 4);
    }

    private static void drawSparkles(Graphics2D g2, int frame, int baseY) {
        g2.setColor(new Color(255, 220, 50, 200));
        int sparkle = frame % 30;
        double scale = sparkle < 15 ? sparkle / 15.0 : (30 - sparkle) / 15.0;

        drawStar(g2, -2, 5 + baseY, (int)(6 * scale));
        drawStar(g2, 55, 3 + baseY, (int)(5 * scale));
        drawStar(g2, 25, -5 + baseY, (int)(7 * scale));
    }

    private static void drawStar(Graphics2D g2, int cx, int cy, int size) {
        if (size <= 0) return;
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(cx - size, cy, cx + size, cy);
        g2.drawLine(cx, cy - size, cx, cy + size);
        g2.drawLine(cx - size/2, cy - size/2, cx + size/2, cy + size/2);
        g2.drawLine(cx + size/2, cy - size/2, cx - size/2, cy + size/2);
        g2.setStroke(new BasicStroke(1));
    }

    private static void drawWheelScene(Graphics2D g2, Hamster hamster) {
        int frame = hamster.getAnimFrame();
        HamsterColor palette = hamster.getColor();

        // Wheel parameters
        int cx = 70, cy = 45;
        int outerR = 42;
        int innerR = 38;
        double rotation = frame * 0.15;

        // === STAND (behind everything) ===
        g2.setColor(new Color(160, 140, 120));
        g2.setStroke(new BasicStroke(3.5f));
        // Support legs from axle to ground
        g2.drawLine(cx - 3, cy + 2, cx - 20, cy + outerR + 14);
        g2.drawLine(cx + 3, cy + 2, cx + 20, cy + outerR + 14);
        // Base bar
        g2.setStroke(new BasicStroke(4f));
        g2.drawLine(cx - 28, cy + outerR + 14, cx + 28, cy + outerR + 14);
        g2.setStroke(new BasicStroke(1));

        // === WHEEL ===
        // Spokes
        g2.setColor(new Color(170, 150, 130));
        g2.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 8; i++) {
            double angle = rotation + i * Math.PI / 4;
            int x1 = cx + (int)(Math.cos(angle) * 6);
            int y1 = cy + (int)(Math.sin(angle) * 6);
            int x2 = cx + (int)(Math.cos(angle) * (innerR - 1));
            int y2 = cy + (int)(Math.sin(angle) * (innerR - 1));
            g2.drawLine(x1, y1, x2, y2);
        }

        // Rungs along inner rim
        g2.setColor(new Color(190, 170, 150));
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 24; i++) {
            double angle = rotation + i * Math.PI / 12;
            int x1 = cx + (int)(Math.cos(angle) * (innerR - 3));
            int y1 = cy + (int)(Math.sin(angle) * (innerR - 3));
            int x2 = cx + (int)(Math.cos(angle) * (innerR + 2));
            int y2 = cy + (int)(Math.sin(angle) * (innerR + 2));
            g2.drawLine(x1, y1, x2, y2);
        }

        // Center hub/axle
        g2.setColor(new Color(150, 130, 110));
        g2.fillOval(cx - 5, cy - 5, 10, 10);
        g2.setColor(new Color(120, 100, 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - 5, cy - 5, 10, 10);
        g2.setStroke(new BasicStroke(1));

        // === HAMSTER inside wheel ===
        AffineTransform saved = g2.getTransform();
        // Position hamster at bottom of wheel
        g2.translate(40, 26);

        int bounceY = (int)(Math.sin(frame * 0.5) * 2);
        int baseY = bounceY;

        // Tail
        g2.setColor(palette.getTailAndLegs());
        g2.fillOval(-2, 30 + baseY, 12, 10);

        // Body
        g2.setColor(palette.getBody());
        g2.fillOval(5, 18 + baseY, 50, 38);

        // Belly
        g2.setColor(palette.getBelly());
        g2.fillOval(15, 28 + baseY, 30, 26);

        // Legs - fast running
        drawLegs(g2, frame, Hamster.State.RUNNING_WHEEL, baseY, palette);

        // Ears
        g2.setColor(palette.getBody());
        g2.fillOval(12, 4 + baseY, 14, 16);
        g2.setColor(palette.getEarInner());
        g2.fillOval(15, 7 + baseY, 8, 10);
        g2.setColor(palette.getBody());
        g2.fillOval(34, 4 + baseY, 14, 16);
        g2.setColor(palette.getEarInner());
        g2.fillOval(37, 7 + baseY, 8, 10);

        // Head
        g2.setColor(palette.getBody());
        g2.fillOval(10, 10 + baseY, 40, 30);

        // Face - determined expression
        drawFace(g2, Hamster.State.RUNNING_WHEEL, frame, baseY, palette);

        // Sweat drops
        drawSweatDrops(g2, frame, baseY);

        g2.setTransform(saved);

        // === WHEEL outer rim (drawn last, in front of hamster for depth) ===
        g2.setColor(new Color(180, 160, 140));
        g2.setStroke(new BasicStroke(4.5f));
        g2.drawOval(cx - outerR, cy - outerR, outerR * 2, outerR * 2);
        g2.setStroke(new BasicStroke(1));

        // --- name above wheel ---
        drawName(g2, hamster.getName(), cx, cy - outerR - 12);
    }

    private static void drawName(Graphics2D g2, String name, int cx, int y) {
        Font font = new Font(FONT_NAME, Font.BOLD, 11);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(name);
        int x = cx - textWidth / 2;

        // background bubble
        int pad = 4;
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillRoundRect(x - pad, y - fm.getAscent() - pad + 2, textWidth + pad * 2, fm.getHeight() + pad, 10, 10);
        g2.setColor(new Color(200, 160, 100, 120));
        g2.drawRoundRect(x - pad, y - fm.getAscent() - pad + 2, textWidth + pad * 2, fm.getHeight() + pad, 10, 10);

        // text
        g2.setColor(new Color(80, 50, 20));
        g2.drawString(name, x, y);
    }

    private static void drawSweatDrops(Graphics2D g2, int frame, int baseY) {
        // Sweat drop 1
        int cycle1 = frame % 25;
        if (cycle1 < 12) {
            float progress = cycle1 / 12.0f;
            int alpha = (int)(200 * (1 - progress));
            g2.setColor(new Color(120, 190, 255, alpha));
            int dx = (int)(-5 * progress);
            int dy = (int)(-8 * progress);
            g2.fillOval(12 + dx, 10 + baseY + dy, 3, 5);
        }
        // Sweat drop 2
        int cycle2 = (frame + 13) % 25;
        if (cycle2 < 12) {
            float progress = cycle2 / 12.0f;
            int alpha = (int)(180 * (1 - progress));
            g2.setColor(new Color(120, 190, 255, alpha));
            int dx = (int)(5 * progress);
            int dy = (int)(-10 * progress);
            g2.fillOval(48 + dx, 8 + baseY + dy, 3, 5);
        }
    }
}
