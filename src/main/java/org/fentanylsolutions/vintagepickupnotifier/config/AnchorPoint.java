package org.fentanylsolutions.vintagepickupnotifier.config;

public enum AnchorPoint {

    TOP_LEFT(-1, -1),
    TOP_CENTER(0, -1),
    TOP_RIGHT(1, -1),
    CENTER_LEFT(-1, 0),
    CENTER(0, 0),
    CENTER_RIGHT(1, 0),
    BOTTOM_LEFT(-1, 1),
    BOTTOM_CENTER(0, 1),
    BOTTOM_RIGHT(1, 1);

    private final int normalX;
    private final int normalY;

    AnchorPoint(int normalX, int normalY) {
        this.normalX = normalX;
        this.normalY = normalY;
    }

    public int getNormalX() {
        return this.normalX;
    }

    public int getNormalY() {
        return this.normalY;
    }

    public boolean isLeft() {
        return this.normalX == -1;
    }

    public boolean isRight() {
        return this.normalX == 1;
    }

    public boolean isTop() {
        return this.normalY == -1;
    }

    public boolean isBottom() {
        return this.normalY == 1;
    }

    public boolean isHorizontalCenter() {
        return this.normalX == 0;
    }

    public boolean isVerticalCenter() {
        return this.normalY == 0;
    }

    public Positioner createPositioner(int guiWidth, int guiHeight, int elementWidth, int elementHeight) {
        return new Positioner(this.normalX, this.normalY, guiWidth, guiHeight, elementWidth, elementHeight);
    }

    public static class Positioner {

        private final int normalX;
        private final int normalY;
        private final int guiWidth;
        private final int guiHeight;
        private final int elementWidth;
        private final int elementHeight;

        public Positioner(int normalX, int normalY, int guiWidth, int guiHeight, int elementWidth, int elementHeight) {
            this.normalX = normalX;
            this.normalY = normalY;
            this.guiWidth = guiWidth;
            this.guiHeight = guiHeight;
            this.elementWidth = elementWidth;
            this.elementHeight = elementHeight;
        }

        public int getPosX(int posX) {
            return Math.round(
                this.guiWidth / 2.0F + this.normalX * (this.guiWidth / 2.0F - posX)
                    - (this.normalX + 1) * this.elementWidth / 2.0F);
        }

        public int getPosY(int posY) {
            return Math.round(
                this.guiHeight / 2.0F + this.normalY * (this.guiHeight / 2.0F - posY)
                    - (this.normalY + 1) * this.elementHeight / 2.0F);
        }
    }
}
