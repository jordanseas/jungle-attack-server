package network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

/** Strategy. */
public interface IMessageHandler {
    void handle(ClientHandler client, JsonObject payload);

    /** Lee un arreglo de enteros del payload (p. ej. animalIds). Si falta o no es arreglo, lanza y el dispatcher responde MALFORMED_MESSAGE. */
    static List<Integer> readIds(JsonObject payload, String field) {
        List<Integer> ids = new ArrayList<>();
        for (JsonElement e : payload.getAsJsonArray(field)) {
            ids.add(e.getAsInt());
        }
        return ids;
    }
}
