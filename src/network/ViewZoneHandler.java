package network;

import com.google.gson.JsonObject;
import logic.commands.ViewZoneCommand;
import protocol.ErrorCode;
import session.GameSession;

public class ViewZoneHandler implements IMessageHandler {

    /** Arma ViewZoneCommand → session.submit(cmd, client::sendError). */
    @Override
    public void handle(ClientHandler client, JsonObject payload) {
        GameSession session = client.getSession();
        if (session == null) {
            client.sendError(ErrorCode.WRONG_PHASE);
            return;
        }
        int zoneId = payload.get("zoneId").getAsInt();

        ViewZoneCommand cmd = new ViewZoneCommand(client.getPlayer().getPack(), zoneId);
        ErrorCode error = session.submit(cmd, client::sendError);
        if (error != null) {
            client.sendError(error);
        }
    }
}
