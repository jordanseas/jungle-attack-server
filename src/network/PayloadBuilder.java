package network;

import constants.IConstants;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import model.Animal;
import model.CoffeeCup;
import model.Flag;
import model.GameEvent;
import model.GameMap;
import model.GameWorld;
import model.MatchResult;
import model.Pack;
import model.PlayerPack;
import protocol.Payloads;

public final class PayloadBuilder {

    private PayloadBuilder() {
    }

    /** Mapa + estado inicial. */
    public static Payloads.GameStarted gameStarted(GameWorld w, PlayerPack viewer) {
        GameMap map = w.getMap();
        List<Payloads.ZoneInfo> zones = new ArrayList<>();
        for (int zoneId = 0; zoneId < IConstants.ZONE_COUNT; zoneId++) {
            zones.add(new Payloads.ZoneInfo(zoneId, map.neighborZones(zoneId), map.pathsOf(zoneId)));
        }
        PlayerPack opponent = w.opponentOf(viewer);
        return new Payloads.GameStarted(IConstants.ZONE_WIDTH, IConstants.ZONE_HEIGHT, zones,
                opponent.getNickname(), viewer.getSpecies().name(), opponent.getSpecies().name(),
                stateUpdate(w, viewer, List.of()));
    }

    /** Solo la zona que mira, en coordenadas locales y con packType relativo. */
    public static Payloads.StateUpdate stateUpdate(GameWorld w, PlayerPack viewer, List<GameEvent> events) {
        int zoneId = viewer.getViewedZone();
        double ox = GameMap.originX(zoneId);
        double oy = GameMap.originY(zoneId);

        List<Payloads.AnimalState> animals = new ArrayList<>();
        for (Pack pack : w.getPacks()) {
            String packType = packType(pack, viewer);
            for (Animal a : pack.getMembers()) {
                if (a.getZoneId() == zoneId) {
                    animals.add(new Payloads.AnimalState(a.getId(), pack.getId(), pack.getSpecies().name(),
                            packType, a.getX() - ox, a.getY() - oy, a.getEnergy(), a.isAlive()));
                }
            }
        }

        List<Payloads.CupState> cups = new ArrayList<>();
        for (CoffeeCup c : w.getCups()) {
            if (!c.isConsumed() && c.getZoneId() == zoneId) {
                cups.add(new Payloads.CupState(c.getId(), c.getX() - ox, c.getY() - oy));
            }
        }

        List<Payloads.FlagState> flags = new ArrayList<>();
        for (PlayerPack p : w.playerPacks()) {
            Flag f = p.getFlag();
            if (f != null && f.getZoneId() == zoneId) {
                flags.add(new Payloads.FlagState(p == viewer ? "PLAYER" : "OPPONENT", f.getX() - ox, f.getY() - oy));
            }
        }

        List<Payloads.EventState> eventStates = new ArrayList<>();
        for (GameEvent e : events) {
            if (e.zoneId() == zoneId) {
                eventStates.add(new Payloads.EventState(e.type().name(), e.sourceId(), e.targetId()));
            }
        }

        List<Integer> visibleZones = new ArrayList<>(new TreeSet<>(w.visibleZones(viewer)));
        return new Payloads.StateUpdate(System.currentTimeMillis(), zoneId, visibleZones, animals, cups, flags,
                viewer.getDamage(), viewer.getCups(), viewer.getBotsKilled(), eventStates);
    }

    public static Payloads.Summary summary(GameWorld w, MatchResult r, PlayerPack viewer) {
        Map<Integer, Integer> deathsByPack = new LinkedHashMap<>();
        for (Pack pack : w.getPacks()) {
            deathsByPack.put(pack.getId(), pack.countDead());
        }
        long durationMs = System.currentTimeMillis() - w.getStartTime();
        return new Payloads.Summary(r.winner(), r.reason().name(), durationMs, viewer.getBotsKilled(),
                deathsByPack, viewer.getCups(), w.opponentOf(viewer).getCups());
    }

    /** "PLAYER", "OPPONENT" o "BOT", relativo a quien recibe. */
    private static String packType(Pack pack, PlayerPack viewer) {
        if (pack == viewer) {
            return "PLAYER";
        }
        return pack.isBot() ? "BOT" : "OPPONENT";
    }
}
