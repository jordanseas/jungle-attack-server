package model;

import constants.IConstants;

public class CoffeeCup extends Entity {
    private boolean consumed;

    public CoffeeCup(int id, double x, double y) {
        super(id, x, y);
    }

    /** Si no está consumida y a.isNear(this, CUP_RADIUS): la consume y devuelve true. */
    public boolean tryCollect(Animal a) {
        if (consumed || a == null || !a.isAlive() || !a.isNear(this, IConstants.CUP_RADIUS)) {
            return false;
        }
        consumed = true;
        return true;
    }

    public boolean isConsumed() { return consumed; }
}
