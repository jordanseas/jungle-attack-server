package session;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import constants.IConstants;
import logic.GameEngine;
import logic.WorldFactory;
import logic.commands.IGameCommand;
import model.AnimalSpecies;
import model.GameEvent;
import model.GameMap;
import model.GameWorld;
import model.MatchResult;
import model.PlayerPack;
import protocol.ErrorCode;

/** Publisher del Observer. join, selectSpecies y leave son synchronized. */
public class GameSession {

    public enum State { WAITING, SELECTING, PLAYING, FINISHED }

    private String code;
    // volatile: los escribe el hilo del tick o de un cliente y los lee submit() desde otro cliente
    private volatile State state = State.WAITING;
    private List<Player> players = new ArrayList<>();
    private List<IGameListener> listeners = new ArrayList<>();
    private volatile GameEngine engine;
    private ConcurrentLinkedQueue<Runnable> commands = new ConcurrentLinkedQueue<>();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> loop;
    private Random random = new Random();
    private long lastTick;

    public GameSession(String code, ScheduledExecutorService scheduler) {
        this.code = code;
        this.scheduler = scheduler;
    }

    /** MATCH_FULL, NICKNAME_TAKEN; con 2 jugadores → onOpponentJoined + onSelectionChanged. */
    public synchronized ErrorCode join(Player p) {
        if (state != State.WAITING || players.size() >= IConstants.PLAYERS_PER_MATCH) {
            return ErrorCode.MATCH_FULL;
        }
        for (Player other : players) {
            if (other.getNickname().equalsIgnoreCase(p.getNickname())) {
                return ErrorCode.NICKNAME_TAKEN;
            }
        }
        players.add(p);
        addListener(p.getListener());
        if (players.size() == IConstants.PLAYERS_PER_MATCH) {
            state = State.SELECTING;
            for (IGameListener l : listeners) {
                l.onOpponentJoined(this);
            }
            for (IGameListener l : listeners) {
                l.onSelectionChanged(this);
            }
        }
        return null;
    }

    /** WRONG_PHASE, SPECIES_TAKEN; cuando eligen los dos → start(). */
    public synchronized ErrorCode selectSpecies(Player p, AnimalSpecies s) {
        if (state != State.SELECTING) {
            return ErrorCode.WRONG_PHASE;
        }
        if (s == opponentOf(p).getSpecies()) {
            return ErrorCode.SPECIES_TAKEN;
        }
        p.setSpecies(s);
        for (IGameListener l : listeners) {
            l.onSelectionChanged(this);
        }
        if (players.stream().allMatch(pl -> pl.getSpecies() != null)) {
            start();
        }
        return null;
    }

    public List<AnimalSpecies> availableSpecies() {
        List<AnimalSpecies> available = new ArrayList<>(List.of(AnimalSpecies.values()));
        for (Player p : players) {
            available.remove(p.getSpecies());
        }
        return available;
    }

    /** WorldFactory + onGameStarted + programa tick() cada TICK_MS. */
    private void start() {
        long now = System.currentTimeMillis();
        Player p1 = players.get(0);
        Player p2 = players.get(1);
        GameWorld world = new WorldFactory(random).create(new GameMap(),
                p1.getNickname(), p1.getSpecies(), p2.getNickname(), p2.getSpecies(), now);
        engine = new GameEngine(world);
        for (PlayerPack pack : world.playerPacks()) {
            for (Player p : players) {
                if (p.getNickname().equals(pack.getNickname())) {
                    p.setPack(pack);
                }
            }
        }
        state = State.PLAYING;
        lastTick = now;
        for (IGameListener l : listeners) {
            l.onGameStarted(this);
        }
        loop = scheduler.scheduleAtFixedRate(this::tick, IConstants.TICK_MS, IConstants.TICK_MS,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Si state != PLAYING → WRONG_PHASE de inmediato.
     * Si no, encola () -> { e = engine.execute(command, now); if (e != null) onError.accept(e); }
     * y devuelve null.
     */
    public ErrorCode submit(IGameCommand command, Consumer<ErrorCode> onError) {
        if (state != State.PLAYING) {
            return ErrorCode.WRONG_PHASE;
        }
        commands.add(() -> {
            ErrorCode e = engine.execute(command, System.currentTimeMillis());
            if (e != null) {
                onError.accept(e);
            }
        });
        return null;
    }

    /**
     * Ejecuta los comandos encolados, engine.update(...), onTick(...), y termina si hay ganador.
     * Atrapa RuntimeException para que el scheduler no se detenga.
     */
    public void tick() {
        if (state != State.PLAYING) {
            return;
        }
        try {
            long now = System.currentTimeMillis();
            long delta = now - lastTick;
            lastTick = now;

            Runnable command;
            while ((command = commands.poll()) != null) {
                command.run();
            }

            GameWorld world = engine.getWorld();
            if (!world.isFinished()) {
                engine.update(now, delta);
            }
            List<GameEvent> events = world.takeEvents();
            for (IGameListener l : listeners) {
                l.onTick(this, events);
            }
            if (world.isFinished()) {
                finish(world.getResult());
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /** En juego: encola la victoria del rival (OPPONENT_LEFT); antes: cancela y avisa onOpponentLeft. */
    public synchronized void leave(Player p) {
        if (state == State.FINISHED) {
            return;
        }
        if (state == State.PLAYING) {
            Player opponent = opponentOf(p);
            commands.add(() -> engine.getWorld().finish(
                    new MatchResult(opponent.getNickname(), MatchResult.Reason.OPPONENT_LEFT)));
            return;
        }
        state = State.FINISHED;
        players.remove(p);
        removeListener(p.getListener());
        for (IGameListener l : listeners) {
            l.onOpponentLeft(this);
        }
    }

    /** Cancela el loop + onGameOver. */
    private void finish(MatchResult r) {
        state = State.FINISHED;
        if (loop != null) {
            loop.cancel(false);
        }
        for (IGameListener l : listeners) {
            l.onGameOver(this, r);
        }
    }

    public void addListener(IGameListener l) { listeners.add(l); }

    public void removeListener(IGameListener l) { listeners.remove(l); }

    public Player opponentOf(Player p) {
        for (Player other : players) {
            if (other != p) {
                return other;
            }
        }
        return null;
    }

    public GameEngine getEngine() { return engine; }

    public String getCode() { return code; }

    public State getState() { return state; }
}
