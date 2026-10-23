package logic.systems;

import constants.IConstants;
import java.util.LinkedHashMap;
import java.util.Map;
import model.Animal;
import model.EventType;
import model.GameEvent;
import model.GameWorld;
import model.PlayerPack;

public class DamageResolver {

    public void resolve(GameWorld world, long now) {
        // 1) Se juntan los golpes de este tic: cada animal con objetivo y sin cooldown pega una vez.
        //    Todavía no se resta energía, así que dos animales que se pegan a la vez se dan su golpe aunque uno muera.
        Map<Integer, Integer> damageByTarget = new LinkedHashMap<>();
        Map<Integer, Animal> firstAttackerByTarget = new LinkedHashMap<>();
        for (Animal attacker : world.livingAnimals()) {
            if (attacker.getTargetId() == IConstants.NO_TARGET || !attacker.canAttack(now)) {
                continue;
            }
            Animal target = world.getAnimal(attacker.getTargetId());
            if (target == null || !target.isAlive()) {
                continue;
            }
            attacker.registerAttack();
            world.addEvent(new GameEvent(EventType.ATTACK, attacker.getId(), target.getId(), target.getZoneId()));
            damageByTarget.merge(target.getId(), attacker.getDamage(), Integer::sum);
            firstAttackerByTarget.putIfAbsent(target.getId(), attacker);
        }

        // 2) Se aplica todo el daño junto.
        // 3) Por cada muerte: evento ANIMAL_DIED y, si era un bot, se le anota la baja al pack atacante.
        for (Map.Entry<Integer, Integer> hit : damageByTarget.entrySet()) {
            Animal victim = world.getAnimal(hit.getKey());
            if (!victim.takeDamage(hit.getValue())) {
                continue; // sigue vivo
            }
            Animal killer = firstAttackerByTarget.get(victim.getId());
            world.addEvent(new GameEvent(EventType.ANIMAL_DIED, killer.getId(), victim.getId(), victim.getZoneId()));
            if (victim.getPack().isBot() && killer.getPack() instanceof PlayerPack killerPack) {
                killerPack.addBotKill();
                world.addEvent(new GameEvent(EventType.BOT_KILLED, killer.getId(), victim.getId(), victim.getZoneId()));
            }
        }
    }
}
