package network;

import com.google.gson.JsonObject;
import java.util.List;
import logic.commands.AttackCommand;
import protocol.ErrorCode;
import session.GameSession;

public class AttackHandler implements IMessageHandler {

    /** Arma AttackCommand → session.submit(cmd, client::sendError). */
    @Override
    public void handle(ClientHandler client, JsonObject payload) {
        GameSession session = client.getSession();
        if (session == null) {
            client.sendError(ErrorCode.WRONG_PHASE);
            return;
        }
        List<Integer> animalIds = IMessageHandler.readIds(payload, "animalIds");
        int targetId = payload.get("targetId").getAsInt();

        AttackCommand cmd = new AttackCommand(client.getPlayer().getPack(), animalIds, targetId);
        ErrorCode error = session.submit(cmd, client::sendError);
        if (error != null) {
            client.sendError(error);
        }
    }
}
