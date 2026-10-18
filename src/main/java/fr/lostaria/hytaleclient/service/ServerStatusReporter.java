package fr.lostaria.hytaleclient.service;

import com.hypixel.hytale.logger.HytaleLogger;
import fr.lostaria.hytaleclient.HytaleClient;
import fr.lostaria.hytaleclient.http.ServerManagerClient;
import fr.lostaria.hytaleclient.model.ServerStatus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerStatusReporter {

    private static final HytaleLogger LOGGER = HytaleClient.getLOGGER();

    private static final int MAX_ATTEMPTS = 5;
    private static final long FIRST_RETRY_DELAY_MS = 1000;

    private final AuthTokenService authTokenService;
    private final ServerManagerClient serverManagerClient;
    private final String serverId;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "server-status-reporter");
        t.setDaemon(true);
        return t;
    });

    public ServerStatusReporter(AuthTokenService authTokenService, ServerManagerClient serverManagerClient, String serverId) {
        this.authTokenService = authTokenService;
        this.serverManagerClient = serverManagerClient;
        this.serverId = serverId;
    }

    public void report(ServerStatus status) {
        executor.execute(() -> send(status));
    }

    public void close() {
        executor.shutdownNow();
    }

    private void send(ServerStatus status) {
        long delayMs = FIRST_RETRY_DELAY_MS;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String jwt = authTokenService.getJwt();
                ServerManagerClient.Response res = serverManagerClient.updateStatus(serverId, status, jwt);

                if (res.ok()) {
                    LOGGER.atInfo().log("STATUS " + status + " sent for serverId=" + serverId);
                    return;
                }

                if (res.statusCode() == 401) {
                    LOGGER.atWarning().log("STATUS " + status + " unauthorized, refreshing JWT"
                            + " (attempt " + attempt + "/" + MAX_ATTEMPTS + ")");
                    authTokenService.refresh();
                    continue;
                }

                if (res.statusCode() == 400 || res.statusCode() == 404) {
                    LOGGER.atSevere().log("STATUS " + status + " rejected for serverId=" + serverId
                            + " status=" + res.statusCode() + " body=" + res.body());
                    return;
                }

                LOGGER.atWarning().log("STATUS " + status + " failed status=" + res.statusCode()
                        + " (attempt " + attempt + "/" + MAX_ATTEMPTS + ")");

            } catch (Exception e) {
                LOGGER.atWarning().log("STATUS " + status + " failed: " + e
                        + " (attempt " + attempt + "/" + MAX_ATTEMPTS + ")");
            }

            if (attempt < MAX_ATTEMPTS) {
                if (!sleep(delayMs)) return;
                delayMs *= 2;
            }
        }

        LOGGER.atSevere().log("STATUS " + status + " given up for serverId=" + serverId
                + " after " + MAX_ATTEMPTS + " attempts");
    }

    private static boolean sleep(long ms) {
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
