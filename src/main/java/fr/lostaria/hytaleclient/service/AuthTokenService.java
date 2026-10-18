package fr.lostaria.hytaleclient.service;

import com.hypixel.hytale.logger.HytaleLogger;
import fr.lostaria.hytaleclient.HytaleClient;
import fr.lostaria.hytaleclient.http.AuthClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuthTokenService {

    private static final HytaleLogger LOGGER = HytaleClient.getLOGGER();

    private final AuthClient authClient;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "jwt-refresher");
        t.setDaemon(true);
        return t;
    });

    private volatile String jwt;

    public AuthTokenService(AuthClient authClient) {
        this.authClient = authClient;

        scheduler.scheduleAtFixedRate(() -> {
            try {
                refresh();
            } catch (Exception e) {
                LOGGER.atWarning().log("JWT refresh failed: " + e);
            }
        }, 55, 55, TimeUnit.MINUTES);
    }

    public String getJwt() throws Exception {
        String current = jwt;
        if (current != null) {
            return current;
        }

        synchronized (this) {
            if (jwt == null) {
                refresh();
            }
            return jwt;
        }
    }

    public synchronized void refresh() throws Exception {
        this.jwt = authClient.fetchJwt();
        LOGGER.atInfo().log("JWT refreshed.");
    }

    public void close() {
        scheduler.shutdownNow();
    }
}
