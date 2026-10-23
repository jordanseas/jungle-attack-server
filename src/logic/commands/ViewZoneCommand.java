package logic.commands;

import model.GameWorld;
import model.PlayerPack;
import protocol.ErrorCode;

public record ViewZoneCommand(PlayerPack pack, int zoneId) implements IGameCommand {

    @Override
    public ErrorCode execute(GameWorld world, long now) {
        // Solo se puede mirar una zona donde hay animales vivos del jugador.
        if (!world.visibleZones(pack).contains(zoneId)) {
            return ErrorCode.ZONE_NOT_VISIBLE;
        }
        pack.setViewedZone(zoneId);
        return null;
    }
}
