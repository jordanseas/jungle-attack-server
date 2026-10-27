package model;

import constants.IConstants;

public abstract class Animal extends Entity {
    private AnimalSpecies species;
    private Pack pack;
    private int energy;
    private boolean alive = true;
    private int targetId = IConstants.NO_TARGET; // objetivo actual o NO_TARGET
    private long lastAttack; // para el cooldown

    @SuppressWarnings("this-escape") // getMaxEnergy() de las subclases solo devuelve una constante
    public Animal(int id, double x, double y, Pack pack) {
        super(id, x, y);
        this.pack = pack;
        this.species = pack.getSpecies();
        this.energy = getMaxEnergy();
    }

    /** Resta energía (nunca baja de 0); devuelve true si murió con ese golpe. */
    public boolean takeDamage(int amount) {
        if (!alive) {
            return false; // un muerto no vuelve a "morir"
        }
        energy = Math.max(0, energy - Math.max(0, amount));
        if (energy == 0) {
            alive = false;
            return true;
        }
        return false;
    }

    /** Suma energía hasta getMaxEnergy(); no revive muertos. */
    public void heal(int amount) {
        if (!alive || amount <= 0) {
            return;
        }
        energy = Math.min(getMaxEnergy(), energy + amount);
    }

    /** Fija el objetivo; el primer golpe será 1 s después. */
    public void engage(int targetId, long now) {
        this.targetId = targetId;
        this.lastAttack = now; // el cooldown empieza ahora: el primer golpe sale en now + ATTACK_COOLDOWN_MS
    }

    /** now - lastAttack >= ATTACK_COOLDOWN_MS */
    public boolean canAttack(long now) {
        return now - lastAttack >= IConstants.ATTACK_COOLDOWN_MS;
    }

    /** lastAttack += ATTACK_COOLDOWN_MS */
    public void registerAttack() {
        lastAttack += IConstants.ATTACK_COOLDOWN_MS;
    }

    public void clearTarget() {
        targetId = IConstants.NO_TARGET;
    }

    public int getTargetId() { return targetId; }

    /** pack.getDamage() */
    public int getDamage() {
        return pack.getDamage();
    }

    /** Pertenecen a packs distintos (dos bots nunca se atacan entre sí). */
    public boolean isEnemyOf(Animal other) {
        if (other == null || pack == other.pack) {
            return false;
        }
        return !(pack.isBot() && other.pack.isBot());
    }

    public AnimalSpecies getSpecies() { return species; }

    public Pack getPack() { return pack; }

    public int getEnergy() { return energy; }

    public boolean isAlive() { return alive; }

    public abstract int getMaxEnergy();

    /** El bot ataca solo, el jugador no. */
    public abstract boolean isAutoAttacker();
}
