package model;

public abstract class Entity {
    protected int id;
    protected double x; // coordenadas globales
    protected double y;

    public Entity(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() { return id; }

    public double getX() { return x; }

    public double getY() { return y; }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** GameMap.zoneOf(x, y) */
    public int getZoneId() {
        return GameMap.zoneOf(x, y);
    }

    public double distanceTo(Entity other) {
        return Math.hypot(other.x - x, other.y - y);
    }

    /** Misma zona y distancia ≤ radio. */
    public boolean isNear(Entity other, double radius) {
        if (other == null || getZoneId() != other.getZoneId()) {
            return false;
        }
        return distanceTo(other) <= radius;
    }
}
