package com.lostwilderness.rpgcore.party;

import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.party.listener.PartySessionListener;
import com.lostwilderness.rpgcore.party.listener.PartyDisplayListener;
import com.lostwilderness.rpgcore.party.listener.PartyFriendlyFireListener;
import com.lostwilderness.rpgcore.party.listener.PartyBuffListener;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Party module: cross-server session-based parties with visual display and combat mechanics.
 * Requires "player" datasource for persistence.
 */
public final class PartyModule implements RpgModule {

    private static final String DATASOURCE = "player";

    private ModuleContext ctx;
    private YamlConfiguration config;
    private PartyRepository repo;
    private PartyServiceImpl partyService;
    private PartyCommand partyCommand;
    private PartySessionListener sessionListener;
    private PartyDisplayListener displayListener;
    private PartyFriendlyFireListener friendlyFireListener;
    private PartyBuffListener buffListener;

    @Override
    public String getName() {
        return "party";
    }

    @Override
    public List<String> getDependencies() {
        return Collections.singletonList("player");
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;
        Plugin plugin = ctx.getPlugin();

        // Validate datasource
        if (ctx.getDatabaseProvider().getDataSource(DATASOURCE) == null) {
            plugin.getLogger().warning("[party] Skipped: no 'player' datasource in db.yml.");
            return;
        }

        // Load config
        this.config = loadConfig(plugin);
        int maxSize = config.getInt("party.max-size", 6);
        String defaultColor = config.getString("party.color", "#00FF00");

        // Create repository
        var executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpgcore-party-db");
            t.setDaemon(true);
            return t;
        });
        SqlExecutor sql = new SqlExecutor(ctx.getDatabaseProvider(), executor);
        boolean mysql = isMysql(ctx);
        this.repo = new PartyRepository(sql, mysql);

        try {
            repo.createTablesIfNotExists();
        } catch (Exception e) {
            plugin.getLogger().severe("[party] Failed to create tables: " + e.getMessage());
            throw new RuntimeException("Party tables failed", e);
        }

        // Create service (with scheduler for async operations)
        partyService = new PartyServiceImpl(repo, ctx.getMessaging(), ctx.getScheduler(),
                                           plugin, maxSize, defaultColor);
        ctx.getServiceRegistry().register(PartyService.class, partyService);

        // Create command
        partyCommand = new PartyCommand(partyService);

        // Create listeners
        sessionListener = new PartySessionListener(partyService, repo, ctx.getScheduler());
        displayListener = new PartyDisplayListener(partyService, config);
        friendlyFireListener = new PartyFriendlyFireListener(partyService, config);
        buffListener = new PartyBuffListener(partyService, config);

        // Set display listener on service (for updatePlayerDisplay calls)
        partyService.setDisplayListener(displayListener);

        plugin.getLogger().info("[party] Loaded. /party command enabled. Max size: " + maxSize);
    }

    @Override
    public void onEnable() {
        if (partyService == null || ctx == null) return;

        // Register command
        Plugin plugin = ctx.getPlugin();
        if (plugin instanceof org.bukkit.plugin.java.JavaPlugin javaPlugin) {
            org.bukkit.command.PluginCommand cmd = javaPlugin.getCommand("party");
            if (cmd != null) {
                cmd.setExecutor(partyCommand);
                cmd.setTabCompleter(partyCommand);
                plugin.getLogger().info("[party] Registered /party command.");
            } else {
                plugin.getLogger().warning("[party] Failed to register /party - not in plugin.yml?");
            }
        }

        // Phase 2: Subscribe to cluster messages
        ctx.getMessaging().subscribe("party/sync", partyService::handlePartySync);
        ctx.getMessaging().subscribe("party/disband", partyService::handlePartyDisband);
        plugin.getLogger().info("[party] Subscribed to cluster messaging channels.");

        // Phase 2: Register session listener
        plugin.getServer().getPluginManager().registerEvents(sessionListener, plugin);
        plugin.getLogger().info("[party] Registered session listener (quit/join).");

        // Phase 2: Start database polling fallback (only if ClusterMessagingService is stubbed)
        partyService.startDatabasePolling();

        // Phase 3: Register display and combat listeners
        plugin.getServer().getPluginManager().registerEvents(displayListener, plugin);
        plugin.getLogger().info("[party] Registered display listener (visual effects).");

        plugin.getServer().getPluginManager().registerEvents(friendlyFireListener, plugin);
        plugin.getLogger().info("[party] Registered friendly fire listener (damage protection).");

        plugin.getServer().getPluginManager().registerEvents(buffListener, plugin);
        plugin.getLogger().info("[party] Registered buff listener (party damage boost).");
    }

    @Override
    public void onDisable() {
        buffListener = null;
        friendlyFireListener = null;
        displayListener = null;
        sessionListener = null;
        partyCommand = null;
        partyService = null;
        repo = null;
        config = null;
        ctx = null;
    }

    private static boolean isMysql(ModuleContext ctx) {
        String url = ctx.getConfigService().getDbConfig().getString("datasources." + DATASOURCE + ".jdbc-url", "");
        return url != null && url.contains("mysql");
    }

    private static YamlConfiguration loadConfig(Plugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "config/party.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config/party.yml", false);
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }
}
