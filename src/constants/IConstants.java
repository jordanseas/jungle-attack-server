package constants;

public interface IConstants {
    int SERVER_PORT = 5050;
    String CONFIG_FILE = "config.properties";
    int MATCH_CODE_LENGTH = 4;
    String MATCH_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    String MATCH_CODE_REGEX = "[A-Za-z0-9]{4}";
    int NICKNAME_MIN_LENGTH = 3;
    int NICKNAME_MAX_LENGTH = 16;
    int MAP_COLUMNS = 4;
    int MAP_ROWS = 3;
    int ZONE_COUNT = MAP_COLUMNS * MAP_ROWS;
    int ZONE_WIDTH = 800;
    int ZONE_HEIGHT = 600;
    int NODE_SPACING = 50; // la mitad de cada zona debe ser múltiplo de NODE_SPACING
    int SPECIES_COUNT = 7;
    int PLAYERS_PER_MATCH = 2;
    int PLAYER_PACK_SIZE = 10;
    int MAX_PLAYER_ENERGY = 20;
    int PLAYER_DAMAGE = 2;
    int BOT_PACK_SIZE = 4;
    int BOT_ENERGY = 10;
    int BOT_DAMAGE = 1;
    int COFFEE_CUP_COUNT = 10;
    int BOTS_PER_BONUS = 2;
    long ATTACK_COOLDOWN_MS = 1000;
    int NO_TARGET = -1;
    double ENCOUNTER_RADIUS = 40;
    double CUP_RADIUS = 20;
    double FLAG_RADIUS = 25;
    // qué tan lejos de la recta del camino puede caer un clic: el camino de las imágenes del cliente mide 56
    // de ancho y además se curva (hasta ~17) y la glorieta del centro está corrida, así que un clic válido
    // puede quedar hasta ~55 de la recta
    double PATH_SNAP_TOLERANCE = 56;
    double ZONE_EDGE_MARGIN = 100; // un clic a menos de esto del borde manda al animal a la zona vecina
    double ZONE_CROSS_DEPTH = 100; // cuánto se mete en la zona vecina al cruzar (múltiplo de NODE_SPACING)
    double BOT_SPREAD = 10; // separación visual de los bots alrededor de su nodo
    double ANIMAL_SPEED = 80; // unidades por segundo
    long TICK_MS = 100;
}
