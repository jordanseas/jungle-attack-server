package network;

import com.google.gson.JsonParseException;
import java.util.EnumMap;
import java.util.Map;
import protocol.ErrorCode;
import protocol.Message;
import protocol.MessageSerializer;
import protocol.MessageType;

/** Context del Strategy. */
public class MessageDispatcher {
    private Map<MessageType, IMessageHandler> handlers = new EnumMap<>(MessageType.class);

    public void register(MessageType type, IMessageHandler handler) {
        handlers.put(type, handler);
    }

    /**
     * fromJson → busca el handler. Si no hay → UNKNOWN_COMMAND.
     * Si hay error de JSON o falta un campo → MALFORMED_MESSAGE.
     */
    public void dispatch(ClientHandler client, String line) {
        Message message;
        try {
            message = MessageSerializer.fromJson(line);
        } catch (JsonParseException e) {
            client.sendError(ErrorCode.MALFORMED_MESSAGE);
            return;
        }

        // type es null cuando el texto no coincide con ningún MessageType
        IMessageHandler handler = message.type() == null ? null : handlers.get(message.type());
        if (handler == null) {
            client.sendError(ErrorCode.UNKNOWN_COMMAND);
            return;
        }

        try {
            handler.handle(client, message.payload());
        } catch (NullPointerException | ClassCastException | IllegalStateException
                 | UnsupportedOperationException | IllegalArgumentException e) {
            // campo faltante (null), de otro tipo (getAsX) o con un valor que no se puede convertir
            client.sendError(ErrorCode.MALFORMED_MESSAGE);
        }
    }
}
