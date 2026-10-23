package logic.systems;

import java.util.List;
import model.Animal;
import model.GameWorld;
import model.MatchResult;
import model.PlayerPack;

public class VictorySystem implements ITickSystem {

    @Override
    public void update(GameWorld world, long now, long deltaMs) {
        if (world.isFinished()) {
            return;
        }
        List<PlayerPack> players = world.playerPacks();
        if (players.size() < 2) {
            return;
        }
        PlayerPack first = players.get(0);
        PlayerPack second = players.get(1);

        // 1) Bandera tocada: gana quien toca la bandera del rival (si se tocan a la vez, empate).
        boolean firstFlagTouched = isFlagTouched(world, first);
        boolean secondFlagTouched = isFlagTouched(world, second);
        if (firstFlagTouched && secondFlagTouched) {
            world.finish(new MatchResult(null, MatchResult.Reason.FLAG_TOUCHED));
            return;
        }
        if (firstFlagTouched) {
            world.finish(new MatchResult(second.getNickname(), MatchResult.Reason.FLAG_TOUCHED));
            return;
        }
        if (secondFlagTouched) {
            world.finish(new MatchResult(first.getNickname(), MatchResult.Reason.FLAG_TOUCHED));
            return;
        }

        // 2) Todos muertos: gana el rival; si los dos se quedan sin animales, empate.
        boolean firstWiped = first.isWiped();
        boolean secondWiped = second.isWiped();
        if (firstWiped && secondWiped) {
            world.finish(new MatchResult(null, MatchResult.Reason.ALL_DEAD));
        } else if (firstWiped) {
            world.finish(new MatchResult(second.getNickname(), MatchResult.Reason.ALL_DEAD));
        } else if (secondWiped) {
            world.finish(new MatchResult(first.getNickname(), MatchResult.Reason.ALL_DEAD));
        }
    }

    /** ¿Algún animal vivo del rival toca la bandera de este jugador? */
    private boolean isFlagTouched(GameWorld world, PlayerPack owner) {
        if (owner.getFlag() == null) {
            return false;
        }
        PlayerPack rival = world.opponentOf(owner);
        if (rival == null) {
            return false;
        }
        for (Animal animal : rival.living()) {
            if (owner.getFlag().isTouchedBy(animal)) {
                return true;
            }
        }
        return false;
    }
}
