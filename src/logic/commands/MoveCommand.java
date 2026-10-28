package logic.commands;

import constants.IConstants;
import java.util.ArrayList;
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

    private static final int EXACT_POINT_ID = -1; // punto final de la ruta que no es un nodo del mapa

    @Override
    public ErrorCode execute(GameWorld world, long now) {
        // 1) Solo se puede mandar a moverse desde una zona donde hay animales vivos del jugador.
        if (!world.visibleZones(pack).contains(zoneId)) {
            return ErrorCode.ZONE_NOT_VISIBLE;
        }

        // 2) El clic viene en coordenadas de la zona: se pasa a globales, se valida que caiga dentro
        //    del ancho del camino y se busca el nodo más cercano (la ruta va por nodos y al final al punto exacto).
        GameMap map = world.getMap();
        double globalX = GameMap.originX(zoneId) + localX;
        double globalY = GameMap.originY(zoneId) + localY;
        if (map.distanceToPath(globalX, globalY) > IConstants.PATH_SNAP_TOLERANCE) {
            return ErrorCode.INVALID_DESTINATION;
        }
        Node goal = crossingGoal(map);
        // Sin cruce de zona el animal termina justo donde se hizo clic (cualquier parte café del camino),
        // no en el nodo de la cuadrícula.
        Node exact = goal == null ? new Node(EXACT_POINT_ID, globalX, globalY) : null;
        if (goal == null) {
            goal = map.nearestNode(globalX, globalY);
        }
        if (goal == null) {
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
                if (exact != null) {
                    path = new ArrayList<>(path);
                    if (path.size() == 1) {
                        path.clear(); // mismo nodo de salida y llegada: va directo al punto, sin pasar por el nodo
                    }
                    path.add(exact);
                }
                mine.setRoute(path);
                moved = true;
            }
        }
        return moved ? null : ErrorCode.NO_ANIMALS;
    }

    /**
     * Si el clic cae cerca de un borde que tiene zona vecina, el destino pasa a ser un nodo
     * ZONE_CROSS_DEPTH adentro de esa zona (sobre el mismo camino). Si no, null.
     * Sin esto el animal se queda en el borde: el nodo más cercano casi siempre es de la zona actual.
     */
    private Node crossingGoal(GameMap map) {
        double toLeft = localX;
        double toRight = IConstants.ZONE_WIDTH - localX;
        double toTop = localY;
        double toBottom = IConstants.ZONE_HEIGHT - localY;
        double nearest = Math.min(Math.min(toLeft, toRight), Math.min(toTop, toBottom));
        if (nearest > IConstants.ZONE_EDGE_MARGIN) {
            return null;
        }

        int column = zoneId % IConstants.MAP_COLUMNS;
        int row = zoneId / IConstants.MAP_COLUMNS;
        double ox = GameMap.originX(zoneId);
        double oy = GameMap.originY(zoneId);
        double depth = IConstants.ZONE_CROSS_DEPTH;

        // Se cruza solo por el borde más cercano, sin moverse en el otro eje.
        if (nearest == toLeft && column > 0) {
            return map.nearestNode(ox - depth, oy + localY);
        }
        if (nearest == toRight && column < IConstants.MAP_COLUMNS - 1) {
            return map.nearestNode(ox + IConstants.ZONE_WIDTH + depth, oy + localY);
        }
        if (nearest == toTop && row > 0) {
            return map.nearestNode(ox + localX, oy - depth);
        }
        if (nearest == toBottom && row < IConstants.MAP_ROWS - 1) {
            return map.nearestNode(ox + localX, oy + IConstants.ZONE_HEIGHT + depth);
        }
        return null; // borde del mapa: no hay zona vecina
    }
}
