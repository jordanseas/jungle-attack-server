package logic.systems;

import constants.IConstants;
import model.Animal;
import model.GameWorld;

public class TargetSelector {

    public void update(GameWorld world, long now) {
        // 1) Se sueltan los objetivos muertos o que ya quedaron fuera de ENCOUNTER_RADIUS.
        //    (Se hace primero para que un bot cuyo objetivo murió pueda elegir otro en este mismo tic.)
        for (Animal animal : world.livingAnimals()) {
            if (animal.getTargetId() == IConstants.NO_TARGET) {
                continue;
            }
            Animal target = world.getAnimal(animal.getTargetId());
            if (target == null || !target.isAlive() || !animal.isNear(target, IConstants.ENCOUNTER_RADIUS)) {
                animal.clearTarget();
            }
        }

        // 2) Los bots sin objetivo eligen al enemigo vivo más cercano dentro del radio.
        for (Animal bot : world.livingAnimals()) {
            if (!bot.isAutoAttacker() || bot.getTargetId() != IConstants.NO_TARGET) {
                continue;
            }
            Animal nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (Animal other : world.livingAnimals()) {
                if (!bot.isEnemyOf(other) || !bot.isNear(other, IConstants.ENCOUNTER_RADIUS)) {
                    continue;
                }
                double distance = bot.distanceTo(other);
                if (distance < nearestDistance) {
                    nearest = other;
                    nearestDistance = distance;
                }
            }
            if (nearest != null) {
                bot.engage(nearest.getId(), now);
            }
        }
    }
}
