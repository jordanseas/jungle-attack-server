package logic.systems;

import model.Animal;
import model.CoffeeCup;
import model.EventType;
import model.GameEvent;
import model.GameWorld;
import model.PlayerAnimal;
import model.PlayerPack;

public class CupSystem implements ITickSystem {

    @Override
    public void update(GameWorld world, long now, long deltaMs) {
        for (CoffeeCup cup : world.getCups()) {
            if (cup.isConsumed()) {
                continue;
            }
            for (Animal animal : world.livingAnimals()) {
                if (!(animal instanceof PlayerAnimal player)) {
                    continue; // los bots no recogen tazas
                }
                if (cup.tryCollect(player)) {
                    ((PlayerPack) player.getPack()).addCup();
                    world.addEvent(new GameEvent(EventType.CUP_COLLECTED, player.getId(), cup.getId(), cup.getZoneId()));
                    break; // la taza ya se consumió: el primero que llegó se la queda
                }
            }
        }
    }
}
