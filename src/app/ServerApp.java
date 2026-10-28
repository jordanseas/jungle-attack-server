package app;

import constants.IConstants;
import constants.ServerConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import network.AttackHandler;
import network.ClientHandler;
import network.CreateMatchHandler;
import network.JoinMatchHandler;
import network.MessageDispatcher;
import network.MoveHandler;
import network.SelectAnimalHandler;
import network.ViewZoneHandler;
import protocol.MessageType;

public class ServerApp {

    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    /**
     * Registra los 6 handlers, abre el ServerSocket (try-with-resources) y por cada accept()
     * envía un ClientHandler al pool de hilos.
     */
    public static void main(String[] args) {
        MessageDispatcher dispatcher = new MessageDispatcher();
        dispatcher.register(MessageType.CREATE_MATCH, new CreateMatchHandler());
        dispatcher.register(MessageType.JOIN_MATCH, new JoinMatchHandler());
        dispatcher.register(MessageType.SELECT_ANIMAL, new SelectAnimalHandler());
        dispatcher.register(MessageType.MOVE_ANIMALS, new MoveHandler());
        dispatcher.register(MessageType.ATTACK, new AttackHandler());
        dispatcher.register(MessageType.VIEW_ZONE, new ViewZoneHandler());

        int port = resolvePort(args);
        ExecutorService pool = Executors.newCachedThreadPool(); // un hilo por cliente conectado
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Jungle Attack escuchando en el puerto " + port);
            while (true) {
                Socket socket = server.accept();
                System.out.println("[conexión] cliente conectado desde " + socket.getRemoteSocketAddress());
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    PrintWriter out = new PrintWriter(
                            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                    pool.execute(new ClientHandler(in, out, dispatcher));
                } catch (IOException e) {
                    // el cliente se desconectó antes de poder atenderlo
                    socket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo abrir el puerto " + port + ": " + e.getMessage());
        } finally {
            pool.shutdownNow();
        }
    }

    /** argumento → config.properties → SERVER_PORT */
    public static int resolvePort(String[] args) {
        if (args != null && args.length > 0 && args[0] != null) {
            try {
                int port = Integer.parseInt(args[0].trim());
                if (port >= MIN_PORT && port <= MAX_PORT) {
                    return port;
                }
            } catch (NumberFormatException e) {
                // argumento inválido: se sigue con config.properties
            }
        }
        return ServerConfig.loadPort(Path.of(IConstants.CONFIG_FILE));
    }
}
