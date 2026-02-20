package com.hamster;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class HamsterIcon {

    public static List<Image> createIcons() {
        return Arrays.asList(
                createIcon(16),
                createIcon(32),
                createIcon(48),
                createIcon(64)
        );
    }

    public static BufferedImage createIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        double scale = size / 16.0;
        g2.scale(scale, scale);

        // Body
        g2.setColor(new Color(230, 180, 120));
        g2.fillOval(1, 3, 14, 12);

        // Belly
        g2.setColor(new Color(255, 235, 205));
        g2.fillOval(4, 6, 8, 7);

        // Ears
        g2.setColor(new Color(230, 180, 120));
        g2.fillOval(2, 0, 5, 5);
        g2.fillOval(9, 0, 5, 5);

        // Ear inner
        g2.setColor(new Color(240, 160, 160));
        g2.fillOval(3, 1, 3, 3);
        g2.fillOval(10, 1, 3, 3);

        // Eyes
        g2.setColor(new Color(30, 30, 30));
        g2.fillOval(4, 6, 2, 2);
        g2.fillOval(10, 6, 2, 2);

        // Eye highlights
        g2.setColor(Color.WHITE);
        g2.fillRect(5, 6, 1, 1);
        g2.fillRect(11, 6, 1, 1);

        // Nose
        g2.setColor(new Color(200, 120, 120));
        g2.fillOval(7, 8, 2, 2);

        // Cheeks
        g2.setColor(new Color(245, 180, 170, 120));
        g2.fillOval(2, 8, 3, 2);
        g2.fillOval(11, 8, 3, 2);

        g2.dispose();
        return img;
    }
}
