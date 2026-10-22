package network;

import com.google.gson.JsonObject;
import model.AnimalSpecies;
import protocol.ErrorCode;
import session.GameSession;

public class SelectAnimalHandler implements IMessageHandler {

    /** session.selectSpecies(...) */
    @Override
    public void handle(ClientHandler client, JsonObject payload) {
        GameSession session = client.getSession();
        if (session == null) {
            client.sendError(ErrorCode.WRONG_PHASE);
            return;
        }
        AnimalSpecies species = AnimalSpecies.fromName(payload.get("species").getAsString());
        if (species == null) {
            client.sendError(ErrorCode.MALFORMED_MESSAGE);
            return;
        }
        ErrorCode error = session.selectSpecies(client.getPlayer(), species);
        if (error != null) {
            client.sendError(error);
        }
    }
}
