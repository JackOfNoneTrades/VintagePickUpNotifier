package org.fentanylsolutions.vintagepickupnotifier.config;

public enum MoveOut {

    DISABLED {

        @Override
        public boolean moveHorizontally(AnchorPoint position) {
            return false;
        }

        @Override
        public boolean moveVertically(AnchorPoint position) {
            return false;
        }
    },
    HORIZONTALLY_ONLY {

        @Override
        public boolean moveHorizontally(AnchorPoint position) {
            return !position.isHorizontalCenter();
        }

        @Override
        public boolean moveVertically(AnchorPoint position) {
            return false;
        }
    },
    VERTICALLY_ONLY {

        @Override
        public boolean moveHorizontally(AnchorPoint position) {
            return false;
        }

        @Override
        public boolean moveVertically(AnchorPoint position) {
            return !position.isVerticalCenter();
        }
    },
    ENABLED {

        @Override
        public boolean moveHorizontally(AnchorPoint position) {
            return position != AnchorPoint.CENTER;
        }

        @Override
        public boolean moveVertically(AnchorPoint position) {
            return position != AnchorPoint.CENTER;
        }
    };

    public abstract boolean moveHorizontally(AnchorPoint position);

    public abstract boolean moveVertically(AnchorPoint position);
}
