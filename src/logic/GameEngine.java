package logic;

import java.util.List;
import logic.commands.IGameCommand;
import logic.systems.CombatSystem;
import logic.systems.CupSystem;
import logic.systems.ITickSystem;
import logic.systems.MovementSystem;
import logic.systems.ViewSystem;
import logic.systems.VictorySystem;
import model.GameWorld;
import protocol.ErrorCode;

/** Facade. Orden del tick: Movement, Cup, Victory, Combat, View. */
public class GameEngine {
    private GameWorld world;
    private List<ITickSystem> systems; // el orden de la lista es el orden del tick

    public GameEngine(GameWorld world) {
        this.world = world;
        this.systems = List.of(new MovementSystem(), new CupSystem(), new VictorySystem(),
                new CombatSystem(), new ViewSystem());
    }

    /** command.execute(world, now); null si todo salió bien. */
    public ErrorCode execute(IGameCommand command, long now) {
        return command.execute(world, now);
    }

    /** Recorre systems; se detiene si world.isFinished(). */
    public void update(long now, long deltaMs) {
        for (ITickSystem system : systems) {
            if (world.isFinished()) {
                break; // alguien ya ganó: las fases que faltan no se ejecutan
            }
            system.update(world, now, deltaMs);
        }
    }

    public GameWorld getWorld() { return world; }
}
