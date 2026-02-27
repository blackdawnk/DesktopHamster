package com.hamster.model;

import java.awt.Color;

public enum UITheme {
    CLASSIC("클래식",
            new Color(255, 250, 240),  // bg
            new Color(200, 160, 100),  // border
            new Color(80, 50, 20),     // textPrimary
            new Color(120, 90, 40),    // textSecondary
            new Color(180, 140, 20),   // accent (money)
            new Color(230, 225, 215),  // buttonBg
            new Color(180, 170, 150),  // buttonBorder
            new Color(200, 230, 180),  // positiveBtn
            new Color(240, 100, 100),  // negativeBtn
            new Color(255, 245, 200),  // highlightBg
            new Color(220, 190, 100)   // highlightBorder
    ),
    DARK("다크 모드",
            new Color(40, 40, 50),
            new Color(70, 70, 90),
            new Color(220, 220, 235),
            new Color(150, 150, 170),
            new Color(255, 200, 60),
            new Color(55, 55, 70),
            new Color(80, 80, 100),
            new Color(60, 120, 80),
            new Color(160, 60, 60),
            new Color(55, 55, 75),
            new Color(90, 90, 120)
    ),
    OCEAN("오션",
            new Color(235, 245, 255),
            new Color(100, 160, 210),
            new Color(20, 50, 80),
            new Color(50, 90, 130),
            new Color(30, 130, 180),
            new Color(210, 230, 245),
            new Color(130, 170, 200),
            new Color(150, 210, 190),
            new Color(200, 100, 100),
            new Color(200, 225, 250),
            new Color(100, 160, 210)
    ),
    SAKURA("벚꽃",
            new Color(255, 240, 245),
            new Color(220, 150, 170),
            new Color(100, 40, 60),
            new Color(160, 80, 110),
            new Color(200, 100, 130),
            new Color(245, 220, 230),
            new Color(210, 160, 180),
            new Color(200, 220, 180),
            new Color(200, 100, 100),
            new Color(255, 225, 235),
            new Color(220, 150, 170)
    ),
    FOREST("숲속",
            new Color(240, 248, 235),
            new Color(120, 170, 100),
            new Color(30, 60, 20),
            new Color(70, 110, 50),
            new Color(140, 170, 40),
            new Color(220, 235, 210),
            new Color(140, 170, 120),
            new Color(170, 210, 150),
            new Color(200, 100, 100),
            new Color(230, 245, 220),
            new Color(120, 170, 100)
    );

    private final String displayName;
    public final Color bg;
    public final Color border;
    public final Color textPrimary;
    public final Color textSecondary;
    public final Color accent;
    public final Color buttonBg;
    public final Color buttonBorder;
    public final Color positiveBtn;
    public final Color negativeBtn;
    public final Color highlightBg;
    public final Color highlightBorder;

    UITheme(String displayName, Color bg, Color border, Color textPrimary, Color textSecondary,
            Color accent, Color buttonBg, Color buttonBorder, Color positiveBtn, Color negativeBtn,
            Color highlightBg, Color highlightBorder) {
        this.displayName = displayName;
        this.bg = bg;
        this.border = border;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.accent = accent;
        this.buttonBg = buttonBg;
        this.buttonBorder = buttonBorder;
        this.positiveBtn = positiveBtn;
        this.negativeBtn = negativeBtn;
        this.highlightBg = highlightBg;
        this.highlightBorder = highlightBorder;
    }

    public String getDisplayName() { return displayName; }
}
