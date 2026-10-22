package protocol;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record Message(MessageType type, JsonObject payload) {

    private static final Gson GSON = new Gson();

    public Message {
        if (payload == null) {
            payload = new JsonObject();
        }
    }

    /** Convierte cualquier objeto a JsonObject con Gson. */
    public static Message of(MessageType type, Object payload) {
        if (payload == null) {
            return new Message(type, new JsonObject());
        }
        if (payload instanceof JsonObject json) {
            return new Message(type, json);
        }
        JsonElement tree = GSON.toJsonTree(payload);
        if (!tree.isJsonObject()) {
            throw new IllegalArgumentException("El payload debe serializarse como objeto JSON: " + payload.getClass());
        }
        return new Message(type, tree.getAsJsonObject());
    }

    /** payload { code, message } */
    public static Message error(ErrorCode code) {
        JsonObject payload = new JsonObject();
        payload.addProperty("code", code.name());
        payload.addProperty("message", code.getMessage());
        return new Message(MessageType.ERROR, payload);
    }
}
