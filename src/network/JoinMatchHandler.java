package network;

import com.google.gson.JsonObject;
import java.util.Locale;
import protocol.ErrorCode;
import session.MatchManager;
import session.Player;

public class JoinMatchHandler implements IMessageHandler {

    /** Valida el apodo y el código (INVALID_MATCH_CODE) → MatchManager.joinMatch. MATCH_JOINED lo envía onOpponentJoined. */
    @Override
    public void handle(ClientHandler client, JsonObject payload) {
        if (client.getSession() != null) {
            client.sendError(ErrorCode.WRONG_PHASE);
            return;
        }
        String nickname = payload.get("nickname").getAsString();
        String code = payload.get("code").getAsString();
        if (!MatchManager.isValidNickname(nickname)) {
            client.sendError(ErrorCode.INVALID_NICKNAME);
            return;
        }
        if (!MatchManager.isValidCode(code)) {
            client.sendError(ErrorCode.INVALID_MATCH_CODE);
            return;
        }
        Player player = new Player(nickname, client);
        // el player se guarda antes porque join() dispara onOpponentJoined en este mismo hilo
        client.join(player, null);
        ErrorCode error = MatchManager.getInstance().joinMatch(code.toUpperCase(Locale.ROOT), player);
        if (error != null) {
            client.join(null, null);
            client.sendError(error);
        }
    }
}
