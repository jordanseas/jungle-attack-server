package logic;

import constants.IConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.Animal;
import model.AnimalSpecies;
import model.BotAnimal;
import model.CoffeeCup;
import model.Flag;
import model.GameMap;
import model.GameWorld;
import model.Node;
import model.Pack;
import model.PlayerAnimal;
import model.PlayerPack;

public class WorldFactory {
    private Random random; // se inyecta (semilla fija en las pruebas)

    public WorldFactory(Random random) {
        this.random = random;
    }

    /**
     * Arma una partida: 2 jugadores + (SPECIES_COUNT - 2) packs de bots, cada uno con una especie distinta,
     * en SPECIES_COUNT zonas distintas. Todas las entidades (animales, banderas y tazas) usan ids únicos.
     */
    public GameWorld create(GameMap map, String nick1, AnimalSpecies sp1,
                            String nick2, AnimalSpecies sp2, long now) {
        int[] nextEntityId = {1}; // contador compartido por animales, banderas y tazas
        int nextPackId = 1;

        // 1. Baraja las 12 zonas y toma 7 distintas: 2 para jugadores y el resto para bots.
        List<Integer> zones = new ArrayList<>();
        for (int zone = 0; zone < IConstants.ZONE_COUNT; zone++) {
            zones.add(zone);
        }
        Collections.shuffle(zones, random);
        int botPackCount = IConstants.SPECIES_COUNT - IConstants.PLAYERS_PER_MATCH;
        List<Integer> playerZones = new ArrayList<>(zones.subList(0, IConstants.PLAYERS_PER_MATCH));
        List<Integer> botZones = new ArrayList<>(
                zones.subList(IConstants.PLAYERS_PER_MATCH, IConstants.PLAYERS_PER_MATCH + botPackCount));

        // Las especies que no eligieron los jugadores se reparten al azar entre los bots.
        List<AnimalSpecies> botSpecies = new ArrayList<>(List.of(AnimalSpecies.values()));
        botSpecies.remove(sp1);
        botSpecies.remove(sp2);
        Collections.shuffle(botSpecies, random);

        List<Pack> packs = new ArrayList<>();

        // 2. Jugadores: 10 animales en closestNodes(...) del centro de su zona (y la bandera en el centro).
        PlayerPack player1 = new PlayerPack(nextPackId++, sp1, nick1);
        PlayerPack player2 = new PlayerPack(nextPackId++, sp2, nick2);
        placePlayer(map, player1, playerZones.get(0), nextEntityId);
        placePlayer(map, player2, playerZones.get(1), nextEntityId);
        packs.add(player1);
        packs.add(player2);

        // 3. Bots: 4 animales en el centro de su zona, separados BOT_SPREAD.
        for (int i = 0; i < botPackCount; i++) {
            Pack bots = new Pack(nextPackId++, botSpecies.get(i));
            placeBots(map, bots, botZones.get(i), nextEntityId);
            packs.add(bots);
        }

        // Las banderas se crean después de los animales para que los animales lleven los ids más bajos.
        for (PlayerPack player : List.of(player1, player2)) {
            Node center = map.centerOf(player.getViewedZone());
            player.setFlag(new Flag(nextEntityId[0]++, center.x(), center.y(), player));
        }

        // 4. Tazas: 10 nodos al azar que no estén en zonas de jugadores ni repetidos.
        List<Node> candidates = new ArrayList<>();
        for (int zone = 0; zone < IConstants.ZONE_COUNT; zone++) {
            if (!playerZones.contains(zone)) {
                candidates.addAll(map.nodesIn(zone));
            }
        }
        Collections.shuffle(candidates, random);
        List<CoffeeCup> cups = new ArrayList<>();
        for (Node node : candidates.subList(0, IConstants.COFFEE_CUP_COUNT)) {
            cups.add(new CoffeeCup(nextEntityId[0]++, node.x(), node.y()));
        }

        return new GameWorld(map, packs, cups, now);
    }

    /** Coloca los animales del jugador en los nodos más cercanos al centro de su zona. */
    private void placePlayer(GameMap map, PlayerPack player, int zone, int[] nextEntityId) {
        Node center = map.centerOf(zone);
        for (Node node : PathFinder.closestNodes(map, center, IConstants.PLAYER_PACK_SIZE)) {
            player.getMembers().add(new PlayerAnimal(nextEntityId[0]++, node.x(), node.y(), player));
        }
        player.setViewedZone(zone); // empieza viendo su propia zona
    }

    /** Coloca los bots alrededor del centro de la zona: arriba, abajo, izquierda y derecha. */
    private void placeBots(GameMap map, Pack bots, int zone, int[] nextEntityId) {
        Node center = map.centerOf(zone);
        double spread = IConstants.BOT_SPREAD;
        double[][] offsets = {{spread, 0}, {-spread, 0}, {0, spread}, {0, -spread}};
        for (int i = 0; i < IConstants.BOT_PACK_SIZE; i++) {
            double[] offset = offsets[i % offsets.length];
            Animal bot = new BotAnimal(nextEntityId[0]++, center.x() + offset[0], center.y() + offset[1], bots);
            bots.getMembers().add(bot);
        }
    }
}
