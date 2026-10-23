package logic.systems;

import java.util.List;
import java.util.Set;
import model.Animal;
import model.GameWorld;
import model.PlayerPack;

public class ViewSystem implements ITickSystem {

    @Override
    public void update(GameWorld world, long now, long deltaMs) {
        for (PlayerPack pack : world.playerPacks()) {
            Set<Integer> visible = world.visibleZones(pack);
            if (visible.contains(pack.getViewedZone())) {
                continue; // la pantalla sigue donde hay animales vivos
            }
            List<Animal> alive = pack.living();
            if (!alive.isEmpty()) {
                pack.setViewedZone(alive.get(0).getZoneId()); // se mueve a la zona de uno de sus animales vivos
            }
        }
    }
}
