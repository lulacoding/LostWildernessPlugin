package com.lostwilderness.rpgcore.infra.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Loads and provides access to YAML config files (core.yml, db.yml, etc.).
 */
public final class ConfigService {

    private final Plugin plugin;
    private final File configFolder;
    private YamlConfiguration coreConfig;
    private YamlConfiguration dbConfig;

    public ConfigService(Plugin plugin) {
        this.plugin = plugin;
        this.configFolder = new File(plugin.getDataFolder(), "config");
    }

    public void load() throws IOException {
        configFolder.mkdirs();
        coreConfig = loadOrSaveDefault("core.yml");
        dbConfig = loadOrSaveDefault("db.yml");
    }

    private YamlConfiguration loadOrSaveDefault(String name) throws IOException {
        File file = new File(configFolder, name);
        if (!file.exists()) {
            try (InputStream in = plugin.getResource("config/" + name)) {
                if (in != null) java.nio.file.Files.copy(in, file.toPath());
            }
            if (!file.exists()) file.createNewFile();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    // --- Core config ---

    public String getEnvironment() {
        return coreConfig.getString("environment", "dev");
    }

    public String getServerRole() {
        return coreConfig.getString("server-role", "survival");
    }

    @SuppressWarnings("unchecked")
    public List<String> getEnabledModules() {
        List<?> list = coreConfig.getList("enabled-modules", List.of("player"));
        return list != null ? (List<String>) list : List.of("player");
    }

    // --- DB config (for DatabaseProvider) ---

    public YamlConfiguration getCoreConfig() {
        return coreConfig;
    }

    public YamlConfiguration getDbConfig() {
        return dbConfig;
    }
}
