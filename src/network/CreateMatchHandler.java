package network;

import com.google.gson.JsonObject;
import java.util.Map;
import protocol.ErrorCode;
import protocol.Message;
import protocol.MessageType;
import session.GameSession;
import session.MatchManager;
import session.Player;

public class CreateMatchHandler implements IMessageHandler {

    /** Valida el apodo (INVALID_NICKNAME) → MatchManager.createMatch → MATCH_CREATED. */
    @Override
    public void handle(ClientHandler client, JsonObject payload) {
        if (client.getSession() != null) {
            client.sendError(ErrorCode.WRONG_PHASE);
            return;
        }
        String nickname = payload.get("nickname").getAsString();
        if (!MatchManager.isValidNickname(nickname)) {
            client.sendError(ErrorCode.INVALID_NICKNAME);
            return;
        }
        Player player = new Player(nickname, client);
        GameSession session = MatchManager.getInstance().createMatch(player);
        client.join(player, session);
        client.send(Message.of(MessageType.MATCH_CREATED, Map.of("code", session.getCode())));
    }
}
