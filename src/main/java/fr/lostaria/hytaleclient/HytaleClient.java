package fr.lostaria.hytaleclient;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import fr.lostaria.hytaleclient.config.Config;
import fr.lostaria.hytaleclient.config.ConfigManager;
import lombok.Getter;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HytaleClient extends JavaPlugin {

    @Getter
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Getter
    private final JavaPluginInit init;

    @Getter
    private ConfigManager configManager;

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
        if (cfg != null) {
            LOGGER.atInfo().log("local=" + cfg.isLocal() + ", serverId=" + cfg.getServerId());
        }
    }
}
