package network;

import com.google.gson.JsonObject;
import java.util.List;
import logic.commands.MoveCommand;
import protocol.ErrorCode;
import session.GameSession;

public class MoveHandler implements IMessageHandler {

    /** Arma MoveCommand → session.submit(cmd, client::sendError); si devuelve error → sendError. */
    @Override
    public void handle(ClientHandler client, JsonObject payload) {
        GameSession session = client.getSession();
        if (session == null) {
            client.sendError(ErrorCode.WRONG_PHASE);
            return;
        }
        List<Integer> animalIds = IMessageHandler.readIds(payload, "animalIds");
        int zoneId = payload.get("zoneId").getAsInt();
        double x = payload.get("x").getAsDouble();
        double y = payload.get("y").getAsDouble();

        MoveCommand cmd = new MoveCommand(client.getPlayer().getPack(), animalIds, zoneId, x, y);
        ErrorCode error = session.submit(cmd, client::sendError);
        if (error != null) {
            client.sendError(error);
        }
    }
}
