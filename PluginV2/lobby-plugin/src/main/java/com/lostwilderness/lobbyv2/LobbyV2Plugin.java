package com.lostwilderness.lobbyv2;

import com.lostwilderness.lobbyv2.personality.QuizCompletionHandler;
import com.lostwilderness.lobbyv2.personality.QuizSessionManager;
import com.lostwilderness.lobbyv2.personality.QuizTriggerListener;
import com.lostwilderness.rpgcore.classes.ClassService;
import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import com.lostwilderness.rpgcore.infra.db.DatabaseProvider;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.personality.TraitRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LobbyV2Plugin extends JavaPlugin {

    private static final double HARD_SPAWN_X = 8.5;
    private static final double HARD_SPAWN_Y = -48.0;
    private static final double HARD_SPAWN_Z = 8.5;

    private VerificationRepository verificationRepository;
    private TraitRepository traitRepository;

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("RPG_Core_V2")) {
            getLogger().severe("RPG_Core_V2 not found! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("LW_Lobby_V2 initialized. Binding to RPG_Core_V2...");
        RPGCorePlugin core = (RPGCorePlugin) getServer().getPluginManager().getPlugin("RPG_Core_V2");
        if (core == null)
            return;

        saveDefaultConfig();

        // Fetch DatabaseProvider from core
        DatabaseProvider dbProvider = core.getService(DatabaseProvider.class);
        if (dbProvider == null || dbProvider.getDataSource("player") == null) {
            getLogger().severe("DatabaseProvider 'player' not found in RPG_Core_V2. Lobby DB features will fail.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        verificationRepository = new VerificationRepository(dbProvider.getDataSource("player"));

        // Create TraitRepository with dedicated executor (following PlayerModule pattern)
        Executor dbExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "lobby-trait-db");
            t.setDaemon(true);
            return t;
        });
        SqlExecutor sqlExecutor = new SqlExecutor(dbProvider, dbExecutor);

        boolean useMysql = getConfig().getBoolean("database.mysql", true);
        traitRepository = new TraitRepository(sqlExecutor, dbExecutor, useMysql);
        traitRepository.createTablesIfNotExists();

        // Create quiz system
        QuizCompletionHandler quizCompletionHandler = new QuizCompletionHandler(this, traitRepository);
        QuizSessionManager quizSessionManager = new QuizSessionManager(this, quizCompletionHandler);

        // Fetch ClassService from core
        ClassService classService = core.getService(ClassService.class);
        if (classService == null) {
            getLogger().warning("ClassService not found! Is the classes module enabled in RPG_Core_V2?");
        }

        getCommand("verify").setExecutor(new VerifyCommand(verificationRepository));

        // Listeners
        String targetServer = getConfig().getString("target-server", "survival");
        Material teleporterBlock = Material.matchMaterial(getConfig().getString("teleporter-block", "END_GATEWAY"));
        if (teleporterBlock == null)
            teleporterBlock = Material.END_GATEWAY;

        getServer().getPluginManager().registerEvents(
                new LobbyTeleportListener(this, verificationRepository, traitRepository, classService, targetServer, teleporterBlock),
                this);

        // Register quiz trigger listener
        getServer().getPluginManager().registerEvents(
                new QuizTriggerListener(this, traitRepository, quizSessionManager),
                this);

        World world = Bukkit.getWorlds().get(0);
        Location spawnLoc = new Location(world,
                HARD_SPAWN_X,
                HARD_SPAWN_Y,
                HARD_SPAWN_Z,
                (float) getConfig().getDouble("spawn.yaw", 0),
                (float) getConfig().getDouble("spawn.pitch", 0));

        Location greeterFocus = null;
        if (getConfig().isConfigurationSection("greeter-intro.focus")) {
            greeterFocus = new Location(world,
                    getConfig().getDouble("greeter-intro.focus.x", spawnLoc.getX()),
                    getConfig().getDouble("greeter-intro.focus.y", spawnLoc.getY()),
                    getConfig().getDouble("greeter-intro.focus.z", spawnLoc.getZ()));
        }

        getServer().getPluginManager().registerEvents(new LobbyProtectionListener(
                this,
                verificationRepository,
                spawnLoc,
                getConfig().getBoolean("greeter-intro.enabled", true),
                getConfig().getString("greeter-intro.event", "lobby_basics.greeter_intro_start"),
                getConfig().getInt("greeter-intro.lock-ticks", 60),
                getConfig().getDouble("greeter-intro.trigger-radius", 5.0),
                greeterFocus), this);
    }
}
