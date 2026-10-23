package logic.commands;

import constants.IConstants;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Animal;
import model.GameWorld;
import model.PlayerPack;
import protocol.ErrorCode;

public record AttackCommand(PlayerPack pack, List<Integer> animalIds, int targetId) implements IGameCommand {

    @Override
    public ErrorCode execute(GameWorld world, long now) {
        // El objetivo tiene que existir, estar vivo y no ser del propio jugador.
        Animal target = world.getAnimal(targetId);
        if (target == null || !target.isAlive() || target.getPack() == pack) {
            return ErrorCode.INVALID_TARGET;
        }

        // Solo atacan los animales propios, vivos y que ya están cerca del objetivo; los demás se ignoran.
        if (animalIds != null) {
            Set<Integer> seen = new HashSet<>(); // por si el mismo id viene repetido
            for (Integer id : animalIds) {
                if (id == null || !seen.add(id)) {
                    continue;
                }
                Animal animal = world.getAnimal(id);
                if (animal != null && animal.getPack() == pack && animal.isAlive()
                        && animal.isNear(target, IConstants.ENCOUNTER_RADIUS)) {
                    animal.engage(targetId, now);
                }
            }
        }
        return null;
    }
}
