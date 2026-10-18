package fr.lostaria.hytaleclient;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import fr.lostaria.hytaleclient.config.Config;
import fr.lostaria.hytaleclient.config.ConfigManager;
import fr.lostaria.hytaleclient.http.AuthClient;
import fr.lostaria.hytaleclient.http.ServerManagerClient;
import fr.lostaria.hytaleclient.listeners.BootListener;
import fr.lostaria.hytaleclient.model.ServerStatus;
import fr.lostaria.hytaleclient.service.AuthTokenService;
import fr.lostaria.hytaleclient.service.ServerStatusReporter;
import lombok.Getter;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HytaleClient extends JavaPlugin {

    @Getter
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Getter
    private final JavaPluginInit init;

    @Getter
    private ConfigManager configManager;

    private AuthTokenService authTokenService;

    private ServerStatusReporter statusReporter;

    public HytaleClient(@NonNullDecl JavaPluginInit init) {
        super(init);
        this.init = init;
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());

        this.configManager = new ConfigManager(this);
        this.configManager.init();

        Config cfg = this.configManager.getConfig();
        if (cfg == null) {
            return;
        }

        String serverId = resolveServerId(cfg);
        LOGGER.atInfo().log("local=" + cfg.isLocal() + ", serverId=" + serverId);

        if (cfg.isLocal()) {
            LOGGER.atInfo().log("Local mode: server status is not sent to the ServerManager");
            return;
        }

        this.authTokenService = new AuthTokenService(new AuthClient(cfg.getAuthApiUrl(), cfg.getPathDeviceToken()));

        this.statusReporter = new ServerStatusReporter(
                authTokenService,
                new ServerManagerClient(cfg.getServerManagerApiUrl()),
                serverId
        );

        statusReporter.report(ServerStatus.STARTING);

        getEventRegistry().register(BootEvent.class, new BootListener(statusReporter)::onBoot);
    }

    @Override
    protected void shutdown() {
        if (statusReporter != null) {
            statusReporter.close();
        }
        if (authTokenService != null) {
            authTokenService.close();
        }
    }

    private static String resolveServerId(Config cfg) {
        String fromEnv = System.getenv("SERVER_ID");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return cfg.getServerId();
    }
}
