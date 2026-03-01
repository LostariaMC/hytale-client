package fr.lostaria.hytaleclient.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypixel.hytale.logger.HytaleLogger;
import fr.lostaria.hytaleclient.HytaleClient;
import fr.lostaria.hytaleclient.utils.ResourceUtils;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final HytaleLogger LOGGER = HytaleClient.getLOGGER();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HytaleClient main;
    private final Path dataDir;

    @Getter
    private final Path configPath;

    @Getter
    private Config config;

    public ConfigManager(HytaleClient main) {
        this.main = main;
        this.dataDir = main.getInit().getDataDirectory();
        this.configPath = dataDir.resolve("config.json");
    }

    public void init() {
        try {
            Files.createDirectories(dataDir);

            if (!Files.exists(configPath)) {
                ResourceUtils.copyResourceIfMissing(main.getClass(), "config.json", configPath);
                LOGGER.atInfo().log("Created default config.json at: " + configPath.toAbsolutePath());
            }

            this.config = MAPPER.readValue(configPath.toFile(), Config.class);
            LOGGER.atInfo().log("Config loaded.");

        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Failed to init config.json");
        }
    }
}
