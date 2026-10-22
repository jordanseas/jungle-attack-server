package session;

import constants.IConstants;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import model.MatchResult;
import protocol.ErrorCode;

/** Singleton con holder idiom. */
public class MatchManager implements IGameListener {

    /** Guarda INSTANCE; la JVM lo crea una sola vez y de forma segura entre hilos, sin locks. */
    private static class Holder {
        private static final MatchManager INSTANCE = new MatchManager();
    }

    private ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2); // compartido por todas las partidas
    private Random random = new Random();

    private MatchManager() {
    }

    public static MatchManager getInstance() {
        return Holder.INSTANCE;
    }

    /** 4 caracteres de MATCH_CODE_CHARS. */
    public static String generateCode(Random random) {
        StringBuilder code = new StringBuilder(IConstants.MATCH_CODE_LENGTH);
        for (int i = 0; i < IConstants.MATCH_CODE_LENGTH; i++) {
            code.append(IConstants.MATCH_CODE_CHARS.charAt(random.nextInt(IConstants.MATCH_CODE_CHARS.length())));
        }
        return code.toString();
    }

    public static boolean isValidNickname(String n) {
        if (n == null || n.isBlank()) {
            return false;
        }
        int length = n.trim().length();
        return length >= IConstants.NICKNAME_MIN_LENGTH && length <= IConstants.NICKNAME_MAX_LENGTH;
    }

    public static boolean isValidCode(String c) {
        return c != null && c.matches(IConstants.MATCH_CODE_REGEX);
    }

    /** Repite generateCode hasta que putIfAbsent lo acepte (atómico, no hay códigos repetidos). */
    public GameSession createMatch(Player creator) {
        GameSession session;
        do {
            session = new GameSession(generateCode(random), scheduler);
        } while (sessions.putIfAbsent(session.getCode(), session) != null);
        session.addListener(this);
        session.join(creator);
        return session;
    }

    /** MATCH_NOT_FOUND o el error de join. */
    public ErrorCode joinMatch(String code, Player p) {
        GameSession session = sessions.get(code);
        if (session == null) {
            return ErrorCode.MATCH_NOT_FOUND;
        }
        return session.join(p);
    }

    /** Elimina la partida del mapa. */
    @Override
    public void onGameOver(GameSession s, MatchResult r) {
        sessions.remove(s.getCode(), s);
    }

    /** La partida se canceló antes de empezar: también se elimina. */
    @Override
    public void onOpponentLeft(GameSession s) {
        sessions.remove(s.getCode(), s);
    }

    public int activeMatches() { return sessions.size(); }
}
