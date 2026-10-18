package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class GameWorld {
    private GameMap map;
    private List<Pack> packs;
    private Map<Integer, Animal> animals = new LinkedHashMap<>(); // conserva el orden de inserción: recorridos predecibles
    private List<CoffeeCup> cups;
    private List<GameEvent> events = new ArrayList<>();
    private MatchResult result;
    private long startTime;

    public GameWorld(GameMap map, List<Pack> packs, List<CoffeeCup> cups, long startTime) {
        this.map = map;
        this.packs = packs;
        this.cups = cups;
        this.startTime = startTime;
        for (Pack pack : packs) {
            for (Animal animal : pack.getMembers()) {
                if (animals.put(animal.getId(), animal) != null) {
                    throw new IllegalArgumentException("Id de animal repetido: " + animal.getId());
                }
            }
        }
    }

    public GameMap getMap() { return map; }

    public List<Pack> getPacks() { return packs; }

    public List<CoffeeCup> getCups() { return cups; }

    public long getStartTime() { return startTime; }

    public Animal getAnimal(int id) { return animals.get(id); }

    public List<Animal> livingAnimals() {
        List<Animal> alive = new ArrayList<>();
        for (Animal animal : animals.values()) {
            if (animal.isAlive()) {
                alive.add(animal);
            }
        }
        return alive;
    }

    public List<PlayerPack> playerPacks() {
        List<PlayerPack> players = new ArrayList<>();
        for (Pack pack : packs) {
            if (pack instanceof PlayerPack playerPack) {
                players.add(playerPack);
            }
        }
        return players;
    }

    public PlayerPack opponentOf(PlayerPack pack) {
        for (PlayerPack other : playerPacks()) {
            if (other != pack) {
                return other;
            }
        }
        return null; // no hay rival
    }

    /** Zonas con animales vivos del jugador. */
    public Set<Integer> visibleZones(PlayerPack pack) {
        Set<Integer> zones = new TreeSet<>();
        if (pack != null) {
            for (Animal animal : pack.living()) {
                zones.add(animal.getZoneId());
            }
        }
        return zones;
    }

    public void addEvent(GameEvent e) { events.add(e); }

    /** Devuelve y limpia. */
    public List<GameEvent> takeEvents() {
        List<GameEvent> out = new ArrayList<>(events);
        events.clear();
        return out;
    }

    public void finish(MatchResult r) { this.result = r; }

    public boolean isFinished() { return result != null; }

    public MatchResult getResult() { return result; }
}
