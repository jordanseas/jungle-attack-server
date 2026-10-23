package logic.systems;

import constants.IConstants;
import model.Animal;
import model.GameWorld;
import model.PlayerAnimal;

public class MovementSystem implements ITickSystem {

    @Override
    public void update(GameWorld world, long now, long deltaMs) {
        double distance = IConstants.ANIMAL_SPEED * deltaMs / 1000.0;
        for (Animal animal : world.livingAnimals()) {
            if (!(animal instanceof PlayerAnimal player) || player.getRoute().isEmpty()) {
                continue; // los bots no se mueven y sin ruta no hay nada que avanzar
            }
            if (player.getTargetId() != IConstants.NO_TARGET) {
                player.getRoute().clear(); // si está peleando, se queda quieto y olvida su ruta
                continue;
            }
            player.advance(distance);
        }
    }
}
