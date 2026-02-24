package com.hamster.model;

import java.awt.Color;

public enum HamsterColor {
    BROWN("갈색",
            new Color(230, 180, 120),  // body
            new Color(255, 235, 205),  // belly
            new Color(240, 160, 160),  // earInner
            new Color(200, 120, 120),  // nose
            new Color(245, 180, 170, 150),  // cheek
            new Color(240, 190, 140),  // stuffedCheek
            new Color(180, 150, 120),  // whisker
            100),
    WHITE("하양",
            new Color(240, 240, 240),  // body
            new Color(255, 255, 255),  // belly
            new Color(255, 190, 190),  // earInner
            new Color(220, 150, 150),  // nose
            new Color(255, 200, 195, 150),  // cheek
            new Color(250, 230, 220),  // stuffedCheek
            new Color(200, 190, 180),  // whisker
            200),
    BLACK("검정",
            new Color(50, 50, 55),     // body
            new Color(90, 90, 95),     // belly
            new Color(120, 70, 70),    // earInner
            new Color(140, 80, 80),    // nose
            new Color(130, 90, 85, 150), // cheek
            new Color(80, 75, 70),     // stuffedCheek
            new Color(100, 95, 90),    // whisker
            300);

    private final String displayName;
    private final Color body;
    private final Color belly;
    private final Color earInner;
    private final Color nose;
    private final Color cheek;
    private final Color stuffedCheek;
    private final Color whisker;
    private final Color tailAndLegs;
    private final Color eyebrow;
    private final int shopPrice;

    HamsterColor(String displayName, Color body, Color belly, Color earInner,
                 Color nose, Color cheek, Color stuffedCheek, Color whisker, int shopPrice) {
        this.displayName = displayName;
        this.body = body;
        this.belly = belly;
        this.earInner = earInner;
        this.nose = nose;
        this.cheek = cheek;
        this.stuffedCheek = stuffedCheek;
        this.whisker = whisker;
        this.tailAndLegs = body.darker();
        this.eyebrow = body.darker().darker();
        this.shopPrice = shopPrice;
    }

    public String getDisplayName() { return displayName; }
    public Color getBody() { return body; }
    public Color getBelly() { return belly; }
    public Color getEarInner() { return earInner; }
    public Color getNose() { return nose; }
    public Color getCheek() { return cheek; }
    public Color getStuffedCheek() { return stuffedCheek; }
    public Color getWhisker() { return whisker; }
    public Color getTailAndLegs() { return tailAndLegs; }
    public Color getEyebrow() { return eyebrow; }
    public int getShopPrice() { return shopPrice; }
}
