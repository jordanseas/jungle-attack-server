package protocol;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public final class MessageSerializer {
    private static final Gson GSON = new Gson();

    private MessageSerializer() {
    }

    /** Una sola línea. */
    public static String toJson(Message m) {
        JsonObject root = new JsonObject();
        root.addProperty("type", m.type().name());
        root.add("payload", m.payload() != null ? m.payload() : new JsonObject());
        return GSON.toJson(root); // Gson sin pretty printing no mete saltos de línea
    }

    /**
     * Lanza JsonParseException si el JSON es inválido.
     * Si el "type" no existe en MessageType, devuelve el mensaje con type null
     * para que el dispatcher responda UNKNOWN_COMMAND.
     */
    public static Message fromJson(String line) throws JsonParseException {
        if (line == null || line.isBlank()) {
            throw new JsonParseException("Mensaje vacío");
        }
        JsonElement element = JsonParser.parseString(line);
        if (!element.isJsonObject()) {
            throw new JsonParseException("El mensaje debe ser un objeto JSON");
        }
        JsonObject root = element.getAsJsonObject();

        JsonElement typeElement = root.get("type");
        if (typeElement == null || !typeElement.isJsonPrimitive() || !typeElement.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("Falta el campo \"type\"");
        }

        JsonElement payloadElement = root.get("payload");
        JsonObject payload;
        if (payloadElement == null || payloadElement.isJsonNull()) {
            payload = new JsonObject();
        } else if (payloadElement.isJsonObject()) {
            payload = payloadElement.getAsJsonObject();
        } else {
            throw new JsonParseException("El campo \"payload\" debe ser un objeto");
        }

        return new Message(parseType(typeElement.getAsString()), payload);
    }

    private static MessageType parseType(String name) {
        try {
            return MessageType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
