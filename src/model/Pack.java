package model;

import constants.IConstants;
import java.util.ArrayList;
import java.util.List;

/** Pack de bots. */
public class Pack {
    protected int id;
    protected AnimalSpecies species;
    protected List<Animal> members = new ArrayList<>();

    public Pack(int id, AnimalSpecies species) {
        this.id = id;
        this.species = species;
    }

    /** Copia con los miembros vivos; modificarla no afecta al pack. */
    public List<Animal> living() {
        List<Animal> alive = new ArrayList<>();
        for (Animal member : members) {
            if (member.isAlive()) {
                alive.add(member);
            }
        }
        return alive;
    }

    public int countDead() {
        int dead = 0;
        for (Animal member : members) {
            if (!member.isAlive()) {
                dead++;
            }
        }
        return dead;
    }

    /** living().isEmpty() */
    public boolean isWiped() {
        return living().isEmpty();
    }

    public int getDamage() { return IConstants.BOT_DAMAGE; }

    public boolean isBot() { return true; }

    public int getId() { return id; }

    public AnimalSpecies getSpecies() { return species; }

    public List<Animal> getMembers() { return members; }
}
