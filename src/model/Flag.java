package model;

import constants.IConstants;

public class Flag extends Entity {
    private PlayerPack owner;

    public Flag(int id, double x, double y, PlayerPack owner) {
        super(id, x, y);
        this.owner = owner;
    }

    public PlayerPack getOwner() { return owner; }

    /** a vivo, de un jugador distinto al dueño y a ≤ FLAG_RADIUS. */
    public boolean isTouchedBy(Animal a) {
        if (a == null || !a.isAlive()) {
            return false;
        }
        Pack pack = a.getPack();
        if (pack.isBot() || pack == owner) {
            return false; // solo cuenta un jugador distinto al dueño
        }
        return a.isNear(this, IConstants.FLAG_RADIUS);
    }
}
