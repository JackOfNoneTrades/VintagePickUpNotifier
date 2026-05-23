package org.fentanylsolutions.vintagepickupnotifier.config;

public enum DisplayAmount {

    OFF(false, false),
    SPRITE(true, false),
    TEXT(false, true),
    BOTH(true, true);

    private final boolean sprite;
    private final boolean text;

    DisplayAmount(boolean sprite, boolean text) {
        this.sprite = sprite;
        this.text = text;
    }

    public boolean isSprite() {
        return this.sprite;
    }

    public boolean isText() {
        return this.text;
    }
}
