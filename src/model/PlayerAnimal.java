package model;

import constants.IConstants;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class PlayerAnimal extends Animal {
    private Deque<Node> route = new ArrayDeque<>(); // nodos que le faltan por recorrer

    public PlayerAnimal(int id, double x, double y, Pack pack) {
        super(id, x, y, pack);
    }

    /** También cancela el ataque. */
    public void setRoute(List<Node> path) {
        route.clear();
        if (path != null) {
            route.addAll(path);
        }
        clearTarget();
    }

    public Deque<Node> getRoute() { return route; }

    /** Avanza hacia el siguiente nodo; si lo alcanza lo saca y sigue con el resto de la distancia. */
    public void advance(double distance) {
        if (!isAlive()) {
            return;
        }
        double remaining = distance;
        while (remaining > 0 && !route.isEmpty()) {
            Node next = route.peekFirst();
            double dx = next.x() - x;
            double dy = next.y() - y;
            double gap = Math.hypot(dx, dy);
            if (gap <= remaining) {
                // Llega al nodo: se posa exactamente en él, lo saca y sigue con lo que sobra.
                setPosition(next.x(), next.y());
                route.pollFirst();
                remaining -= gap;
            } else {
                // No alcanza: avanza la fracción que le corresponde en línea recta hacia el nodo.
                setPosition(x + dx / gap * remaining, y + dy / gap * remaining);
                remaining = 0;
            }
        }
    }

    @Override
    public int getMaxEnergy() { return IConstants.MAX_PLAYER_ENERGY; }

    @Override
    public boolean isAutoAttacker() { return false; }
}
