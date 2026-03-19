package com.lostwilderness.rpgcore.infra.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central provider of named HikariCP data sources from config.
 * Ensures relocated H2 driver is loaded so DriverManager can find it (shadow JAR relocates org.h2).
 */
public final class DatabaseProvider {

    private static final String H2_DRIVER_RELOCATED = "com.lostwilderness.rpgcore.lib.org.h2.Driver";

    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    public DatabaseProvider(YamlConfiguration dbConfig) {
        ConfigurationSection datasources = dbConfig.getConfigurationSection("datasources");
        if (datasources == null) return;
        for (String name : datasources.getKeys(false)) {
            ConfigurationSection ds = datasources.getConfigurationSection(name);
            if (ds == null) continue;
            String jdbcUrl = ds.getString("jdbc-url");
            if (jdbcUrl == null || jdbcUrl.isBlank()) continue;
            ensureDriverLoaded(jdbcUrl);
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(ds.getString("username", ""));
            config.setPassword(ds.getString("password", ""));
            config.setMaximumPoolSize(ds.getInt("maximum-pool-size", 10));
            dataSources.put(name, new HikariDataSource(config));
        }
    }

    private static void ensureDriverLoaded(String jdbcUrl) {
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:h2:")) {
            try {
                Class.forName(H2_DRIVER_RELOCATED);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("H2 driver not found (relocated). Is the -all jar used?", e);
            }
        }
    }

    public DataSource getDataSource(String name) {
        return dataSources.get(name);
    }

    public void close() {
        for (DataSource ds : dataSources.values()) {
            if (ds instanceof HikariDataSource hikari) {
                hikari.close();
            }
        }
        dataSources.clear();
    }
}
