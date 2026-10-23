package logic.commands;

import constants.IConstants;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import logic.PathFinder;
import model.Animal;
import model.GameMap;
import model.GameWorld;
import model.Node;
import model.PlayerAnimal;
import model.PlayerPack;
import protocol.ErrorCode;

public record MoveCommand(PlayerPack pack, List<Integer> animalIds, int zoneId,
                          double localX, double localY) implements IGameCommand {

    @Override
    public ErrorCode execute(GameWorld world, long now) {
        // 1) Solo se puede mandar a moverse desde una zona donde hay animales vivos del jugador.
        if (!world.visibleZones(pack).contains(zoneId)) {
            return ErrorCode.ZONE_NOT_VISIBLE;
        }

        // 2) El clic viene en coordenadas de la zona: se pasa a globales y se busca el nodo más cercano.
        GameMap map = world.getMap();
        double globalX = GameMap.originX(zoneId) + localX;
        double globalY = GameMap.originY(zoneId) + localY;
        Node goal = map.nearestNode(globalX, globalY);
        if (goal == null
                || Math.hypot(goal.x() - globalX, goal.y() - globalY) > IConstants.PATH_SNAP_TOLERANCE) {
            return ErrorCode.INVALID_DESTINATION;
        }

        // 3) A cada animal vivo y propio se le calcula su ruta; los demás se ignoran.
        boolean moved = false;
        if (animalIds != null) {
            Set<Integer> seen = new HashSet<>(); // por si el mismo id viene repetido
            for (Integer id : animalIds) {
                if (id == null || !seen.add(id)) {
                    continue;
                }
                Animal animal = world.getAnimal(id);
                if (!(animal instanceof PlayerAnimal mine) || mine.getPack() != pack || !mine.isAlive()) {
                    continue;
                }
                Node start = map.nearestNode(mine.getX(), mine.getY());
                List<Node> path = PathFinder.findPath(map, start, goal);
                if (path.isEmpty()) {
                    continue; // no hay camino hasta el destino
                }
                mine.setRoute(path);
                moved = true;
            }
        }
        return moved ? null : ErrorCode.NO_ANIMALS;
    }
}
