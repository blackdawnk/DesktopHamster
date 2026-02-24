package com.hamster.model;

public class Poop {

    private final int screenX;
    private final int screenY;
    private final int createdFrame;

    public Poop(int screenX, int screenY, int createdFrame) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.createdFrame = createdFrame;
    }

    public int getScreenX() { return screenX; }
    public int getScreenY() { return screenY; }
    public int getCreatedFrame() { return createdFrame; }
}
