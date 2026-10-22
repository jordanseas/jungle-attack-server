package constants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ServerConfig {

    private static final String PORT_KEY = "server.port";
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    /** Lee server.port; si el archivo no existe o el valor no es válido, devuelve SERVER_PORT. */
    public static int loadPort(Path file) {
        if (file == null || !Files.isRegularFile(file)) {
            return IConstants.SERVER_PORT;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            properties.load(in);
        } catch (IOException e) {
            return IConstants.SERVER_PORT;
        }

        String value = properties.getProperty(PORT_KEY);
        if (value == null) {
            return IConstants.SERVER_PORT;
        }

        try {
            int port = Integer.parseInt(value.trim());
            if (port < MIN_PORT || port > MAX_PORT) {
                return IConstants.SERVER_PORT;
            }
            return port;
        } catch (NumberFormatException e) {
            return IConstants.SERVER_PORT;
        }
    }
}
