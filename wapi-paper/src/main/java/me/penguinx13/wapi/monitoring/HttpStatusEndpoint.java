package me.penguinx13.wapi.monitoring;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class HttpStatusEndpoint {

    private static final String STATUS_PATH = "/status";

    private final ServerStatusService serverStatusService;
    private final JavaPlugin plugin;
    private final int port;
    private final String authorizationSecret;

    private HttpServer server;

    public HttpStatusEndpoint(final ServerStatusService serverStatusService,
                              final JavaPlugin plugin,
                              final int port,
                              final String authorizationSecret) {
        this.serverStatusService = serverStatusService;
        this.plugin = plugin;
        this.port = port;
        this.authorizationSecret = authorizationSecret;
    }

    public void start() {
        if (server != null) {
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(STATUS_PATH, new StatusHandler(serverStatusService, authorizationSecret));
            server.start();
            plugin.getLogger().info("Monitoring endpoint started on port " + port + " at " + STATUS_PATH);
        } catch (final IOException exception) {
            plugin.getLogger().severe("Failed to start monitoring endpoint: " + exception.getMessage());
            server = null;
        }
    }

    public void stop() {
        if (server == null) {
            return;
        }

        server.stop(0);
        server = null;
        plugin.getLogger().info("Monitoring endpoint stopped");
    }

    private static final class StatusHandler implements HttpHandler {

        private static final String GET_METHOD = "GET";
        private static final String AUTHORIZATION_HEADER = "Authorization";

        private final ServerStatusService serverStatusService;
        private final String expectedSecret;

        private StatusHandler(final ServerStatusService serverStatusService, final String expectedSecret) {
            this.serverStatusService = serverStatusService;
            this.expectedSecret = expectedSecret;
        }

        @Override
        public void handle(final HttpExchange exchange) throws IOException {
            try {
                if (!GET_METHOD.equalsIgnoreCase(exchange.getRequestMethod())) {
                    writeResponse(exchange, 405, "Method Not Allowed");
                    return;
                }

                final String authorizationHeader = exchange.getRequestHeaders().getFirst(AUTHORIZATION_HEADER);
                if (authorizationHeader == null || !authorizationHeader.equals(expectedSecret)) {
                    writeResponse(exchange, 403, "Forbidden");
                    return;
                }

                final ServerStatus status = serverStatusService.getStatus();
                final String response = toJson(status);

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                writeResponse(exchange, 200, response);
            } finally {
                exchange.close();
            }
        }

        private static String toJson(final ServerStatus status) {
            return String.format(Locale.US,
                "{\"serverId\":\"%s\",\"tps\":%.2f,\"players\":%d,\"maxPlayers\":%d,\"ramUsed\":%d,\"ramMax\":%d,\"timestamp\":%d}",
                escapeJson(status.getServerId()),
                status.getTps(),
                status.getPlayers(),
                status.getMaxPlayers(),
                status.getRamUsed(),
                status.getRamMax(),
                status.getTimestamp());
        }

        private static String escapeJson(final String value) {
            if (value == null) {
                return "";
            }
            return value.replace("\\", "\\\\").replace("\"", "\\\"");
        }

        private static void writeResponse(final HttpExchange exchange,
                                          final int statusCode,
                                          final String responseBody) throws IOException {
            final byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(bytes);
            }
        }
    }
}
