package logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.GameMap;
import model.Node;

public final class PathFinder {

    private PathFinder() {
    }

    /**
     * BFS con un mapa de "padres"; lista vacía si no hay ruta.
     * La ruta incluye el nodo de inicio y el de destino (si start == goal, devuelve solo ese nodo).
     */
    public static List<Node> findPath(GameMap map, Node start, Node goal) {
        if (map == null || start == null || goal == null) {
            return new ArrayList<>();
        }
        // parents: id del nodo → nodo desde el que se llegó a él. También sirve como "visitados".
        Map<Integer, Node> parents = new HashMap<>();
        Deque<Node> queue = new ArrayDeque<>();
        parents.put(start.id(), null);
        queue.add(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.id() == goal.id()) {
                return rebuild(parents, current);
            }
            for (Node next : map.getNeighbors(current)) {
                if (!parents.containsKey(next.id())) {
                    parents.put(next.id(), current);
                    queue.add(next);
                }
            }
        }
        return new ArrayList<>(); // el destino no es alcanzable
    }

    /**
     * BFS limitado; sirve para colocar a los 10 animales del jugador.
     * Devuelve hasta count nodos, empezando por start y de los más cercanos a los más lejanos.
     */
    public static List<Node> closestNodes(GameMap map, Node start, int count) {
        List<Node> result = new ArrayList<>();
        if (map == null || start == null || count <= 0) {
            return result;
        }
        Set<Integer> visited = new HashSet<>();
        Deque<Node> queue = new ArrayDeque<>();
        visited.add(start.id());
        queue.add(start);

        while (!queue.isEmpty() && result.size() < count) {
            Node current = queue.poll();
            result.add(current);
            for (Node next : map.getNeighbors(current)) {
                if (visited.add(next.id())) {
                    queue.add(next);
                }
            }
        }
        return result;
    }

    /** Sigue los "padres" desde el destino hasta el inicio y da vuelta la lista. */
    private static List<Node> rebuild(Map<Integer, Node> parents, Node goal) {
        List<Node> path = new ArrayList<>();
        for (Node node = goal; node != null; node = parents.get(node.id())) {
            path.add(node);
        }
        Collections.reverse(path);
        return path;
    }
}
