package network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import model.AnimalSpecies;
import model.GameEvent;
import model.MatchResult;
import protocol.ErrorCode;
import protocol.Message;
import protocol.MessageSerializer;
import protocol.MessageType;
import protocol.Payloads;
import session.GameSession;
import session.IGameListener;
import session.Player;

/** Concrete Subscriber. send() es synchronized. */
public class ClientHandler implements Runnable, IGameListener {
    private BufferedReader in; // UTF-8; se reciben en el constructor
    private PrintWriter out;
    private MessageDispatcher dispatcher;
    // volatile: los lee el hilo del cliente y los escribe también el hilo del tick
    private volatile Player player;
    private volatile GameSession session; // null mientras está en el lobby

    public ClientHandler(BufferedReader in, PrintWriter out, MessageDispatcher dispatcher) {
        this.in = in;
        this.out = out;
        this.dispatcher = dispatcher;
    }

    /** Lee línea por línea → dispatcher.dispatch(this, ...); en finally: session.leave(player) y cierra todo. */
    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                dispatcher.dispatch(this, line);
            }
        } catch (IOException e) {
            // el cliente cerró la conexión
        } finally {
            GameSession s = session;
            if (s != null) {
                s.leave(player);
            }
            close();
        }
    }

    /** Sincronizado: escriben este hilo y el hilo del tick. */
    public synchronized void send(Message m) {
        out.println(MessageSerializer.toJson(m));
        out.flush();
    }

    public void sendError(ErrorCode code) {
        send(Message.error(code));
    }

    /** Guarda la partida actual. */
    public void join(Player p, GameSession s) {
        this.player = p;
        this.session = s;
    }

    public Player getPlayer() { return player; }

    public GameSession getSession() { return session; }

    // --- IGameListener: arma el mensaje con PayloadBuilder y lo envía ---

    /** MATCH_JOINED { code, opponent }. Se envía a los dos jugadores. */
    @Override
    public void onOpponentJoined(GameSession s) {
        session = s; // quien se une aún no tenía la partida guardada
        Player opponent = s.opponentOf(player);
        send(Message.of(MessageType.MATCH_JOINED,
                Map.of("code", s.getCode(), "opponent", opponent.getNickname())));
    }

    /** ANIMAL_SELECTION { available, opponentChoice }. */
    @Override
    public void onSelectionChanged(GameSession s) {
        JsonArray available = new JsonArray();
        for (AnimalSpecies sp : s.availableSpecies()) {
            available.add(sp.name());
        }
        AnimalSpecies opponentChoice = s.opponentOf(player).getSpecies();

        JsonObject payload = new JsonObject();
        payload.add("available", available);
        payload.addProperty("opponentChoice", opponentChoice == null ? null : opponentChoice.name());
        send(Message.of(MessageType.ANIMAL_SELECTION, payload));
    }

    @Override
    public void onGameStarted(GameSession s) {
        send(Message.of(MessageType.GAME_STARTED,
                PayloadBuilder.gameStarted(s.getEngine().getWorld(), player.getPack())));
    }

    @Override
    public void onTick(GameSession s, List<GameEvent> events) {
        send(Message.of(MessageType.STATE_UPDATE,
                PayloadBuilder.stateUpdate(s.getEngine().getWorld(), player.getPack(), events)));
    }

    /** Además deja session = null (el cliente vuelve al lobby). */
    @Override
    public void onGameOver(GameSession s, MatchResult result) {
        session = null;
        send(Message.of(MessageType.GAME_OVER,
                PayloadBuilder.summary(s.getEngine().getWorld(), result, player.getPack())));
    }

    /**
     * El rival se fue antes de empezar: no hay mundo, así que se envía un GAME_OVER
     * vacío con reason OPPONENT_LEFT. Además deja session = null.
     */
    @Override
    public void onOpponentLeft(GameSession s) {
        session = null;
        Payloads.Summary summary = new Payloads.Summary(player.getNickname(),
                MatchResult.Reason.OPPONENT_LEFT.name(), 0, 0, Map.of(), 0, 0);
        send(Message.of(MessageType.GAME_OVER, summary));
    }

    private void close() {
        try {
            in.close();
        } catch (IOException e) {
            // ya estaba cerrado
        }
        out.close();
    }
}
