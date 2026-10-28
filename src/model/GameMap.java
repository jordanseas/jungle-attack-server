package model;

import constants.IConstants;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMap {
    private static final double MAP_WIDTH = (double) IConstants.MAP_COLUMNS * IConstants.ZONE_WIDTH;
    private static final double MAP_HEIGHT = (double) IConstants.MAP_ROWS * IConstants.ZONE_HEIGHT;

    private List<Node> nodes = new ArrayList<>();
    private Map<Integer, List<Node>> neighbors = new HashMap<>(); // id del nodo → nodos vecinos

    // Índices auxiliares (se llenan en el constructor y no cambian después).
    private final Map<Long, Node> nodeAt = new HashMap<>();             // (x, y) → nodo, para reutilizar intersecciones
    private final Map<Integer, List<Node>> nodesByZone = new HashMap<>(); // zona → sus nodos

    /**
     * Genera la cuadrícula de caminos: un nodo cada NODE_SPACING (las intersecciones se reutilizan).
     * Cada zona tiene un camino horizontal y uno vertical que pasan por su centro y continúan en las
     * zonas vecinas, así que todo el mapa queda conectado. Igual que en las imágenes del cliente
     * (jungla_*.svg), los caminos terminan en el centro de las zonas del borde: no hay tramos
     * que lleven al borde del mapa.
     */
    public GameMap() {
        double step = IConstants.NODE_SPACING;
        double firstX = IConstants.ZONE_WIDTH / 2.0;
        double lastX = MAP_WIDTH - IConstants.ZONE_WIDTH / 2.0;
        double firstY = IConstants.ZONE_HEIGHT / 2.0;
        double lastY = MAP_HEIGHT - IConstants.ZONE_HEIGHT / 2.0;

        // Caminos horizontales: uno por fila de zonas, a la altura del centro de la zona.
        for (int row = 0; row < IConstants.MAP_ROWS; row++) {
            double y = row * IConstants.ZONE_HEIGHT + IConstants.ZONE_HEIGHT / 2.0;
            Node previous = null;
            for (double x = firstX; x <= lastX; x += step) {
                Node current = nodeFor(x, y);
                link(previous, current);
                previous = current;
            }
        }

        // Caminos verticales: uno por columna de zonas, a la mitad del ancho de la zona.
        for (int col = 0; col < IConstants.MAP_COLUMNS; col++) {
            double x = col * IConstants.ZONE_WIDTH + IConstants.ZONE_WIDTH / 2.0;
            Node previous = null;
            for (double y = firstY; y <= lastY; y += step) {
                Node current = nodeFor(x, y); // en las intersecciones devuelve el nodo ya creado
                link(previous, current);
                previous = current;
            }
        }

        for (int zone = 0; zone < IConstants.ZONE_COUNT; zone++) {
            nodesByZone.put(zone, new ArrayList<>());
        }
        for (Node node : nodes) {
            nodesByZone.get(node.zoneId()).add(node);
        }
    }

    /** fila * MAP_COLUMNS + columna */
    public static int zoneOf(double x, double y) {
        int column = clamp((int) Math.floor(x / IConstants.ZONE_WIDTH), IConstants.MAP_COLUMNS - 1);
        int row = clamp((int) Math.floor(y / IConstants.ZONE_HEIGHT), IConstants.MAP_ROWS - 1);
        return row * IConstants.MAP_COLUMNS + column;
    }

    /** Esquina superior izquierda de la zona. */
    public static double originX(int zoneId) {
        checkZone(zoneId);
        return (zoneId % IConstants.MAP_COLUMNS) * (double) IConstants.ZONE_WIDTH;
    }

    public static double originY(int zoneId) {
        checkZone(zoneId);
        return (zoneId / IConstants.MAP_COLUMNS) * (double) IConstants.ZONE_HEIGHT;
    }

    public List<Node> getNeighbors(Node node) {
        if (node == null) {
            return Collections.emptyList();
        }
        List<Node> list = neighbors.get(node.id());
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    /** Nodo más cercano a un punto global. */
    public Node nearestNode(double x, double y) {
        Node best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Node node : nodes) {
            double d = Math.hypot(node.x() - x, node.y() - y);
            if (d < bestDistance) {
                bestDistance = d;
                best = node;
            }
        }
        return best;
    }

    /** Distancia de un punto global al tramo de camino más cercano (no al nodo más cercano). */
    public double distanceToPath(double x, double y) {
        double best = Double.MAX_VALUE;
        for (Node a : nodes) {
            for (Node b : neighbors.get(a.id())) {
                if (a.id() < b.id()) {
                    best = Math.min(best, Line2D.ptSegDist(a.x(), a.y(), b.x(), b.y(), x, y));
                }
            }
        }
        return best;
    }

    public Node centerOf(int zoneId) {
        double x = originX(zoneId) + IConstants.ZONE_WIDTH / 2.0;
        double y = originY(zoneId) + IConstants.ZONE_HEIGHT / 2.0;
        return nodeAt.get(key(x, y));
    }

    public List<Node> nodesIn(int zoneId) {
        checkZone(zoneId);
        return Collections.unmodifiableList(nodesByZone.get(zoneId));
    }

    /** Arriba, abajo, izquierda, derecha. */
    public List<Integer> neighborZones(int zoneId) {
        checkZone(zoneId);
        int column = zoneId % IConstants.MAP_COLUMNS;
        int row = zoneId / IConstants.MAP_COLUMNS;
        List<Integer> result = new ArrayList<>(4);
        if (row > 0) result.add(zoneId - IConstants.MAP_COLUMNS);                       // arriba
        if (row < IConstants.MAP_ROWS - 1) result.add(zoneId + IConstants.MAP_COLUMNS); // abajo
        if (column > 0) result.add(zoneId - 1);                                         // izquierda
        if (column < IConstants.MAP_COLUMNS - 1) result.add(zoneId + 1);                // derecha
        return result;
    }

    /**
     * Segmentos de la zona en coordenadas locales {x1, y1, x2, y2}, para el cliente.
     * Un segmento pertenece a la zona donde queda su punto medio.
     */
    public List<double[]> pathsOf(int zoneId) {
        double ox = originX(zoneId);
        double oy = originY(zoneId);
        List<double[]> segments = new ArrayList<>();
        for (Node a : nodes) {
            for (Node b : neighbors.get(a.id())) {
                if (a.id() < b.id() && zoneOf((a.x() + b.x()) / 2, (a.y() + b.y()) / 2) == zoneId) {
                    segments.add(new double[]{a.x() - ox, a.y() - oy, b.x() - ox, b.y() - oy});
                }
            }
        }
        return segments;
    }

    // ---- auxiliares privados ----

    /** Devuelve el nodo en (x, y); si no existe todavía, lo crea. */
    private Node nodeFor(double x, double y) {
        long key = key(x, y);
        Node existing = nodeAt.get(key);
        if (existing != null) {
            return existing;
        }
        Node node = new Node(nodes.size(), x, y);
        nodes.add(node);
        nodeAt.put(key, node);
        neighbors.put(node.id(), new ArrayList<>());
        return node;
    }

    /** Conecta dos nodos en ambos sentidos (sin duplicados). */
    private void link(Node a, Node b) {
        if (a == null || b == null || a.equals(b)) {
            return;
        }
        List<Node> fromA = neighbors.get(a.id());
        if (!fromA.contains(b)) {
            fromA.add(b);
            neighbors.get(b.id()).add(a);
        }
    }

    /** Las coordenadas son múltiplos enteros de NODE_SPACING, así que (x, y) cabe en un long. */
    private static long key(double x, double y) {
        return (Math.round(x) << 32) ^ (Math.round(y) & 0xFFFFFFFFL);
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(value, max));
    }

    private static void checkZone(int zoneId) {
        if (zoneId < 0 || zoneId >= IConstants.ZONE_COUNT) {
            throw new IllegalArgumentException("Zona inválida: " + zoneId);
        }
    }
}
