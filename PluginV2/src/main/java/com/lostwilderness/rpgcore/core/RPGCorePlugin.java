package com.lostwilderness.rpgcore.core;

import com.lostwilderness.rpgcore.boss.BossModule;
import com.lostwilderness.rpgcore.calendar.CalendarModule;
import com.lostwilderness.rpgcore.reputation.ReputationModule;
import com.lostwilderness.rpgcore.classes.ClassesModule;
import com.lostwilderness.rpgcore.personality.PersonalityModule;
import com.lostwilderness.rpgcore.zodiac.ZodiacModule;
import com.lostwilderness.rpgcore.zodiac.ZodiacService;
import com.lostwilderness.rpgcore.zodiac.command.ZodiacCommand;
import com.lostwilderness.rpgcore.party.PartyModule;
import com.lostwilderness.rpgcore.clans.AllianceService;
import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.clans.ClansModule;
import com.lostwilderness.rpgcore.clans.WarService;
import com.lostwilderness.rpgcore.clans.command.AllianceCommand;
import com.lostwilderness.rpgcore.clans.command.ClanCommand;
import com.lostwilderness.rpgcore.clans.command.WarCommand;
import com.lostwilderness.rpgcore.clans.listener.WarHeadDropListener;
import com.lostwilderness.rpgcore.events.EventsModule;
import com.lostwilderness.rpgcore.infra.config.ConfigService;
import com.lostwilderness.rpgcore.infra.db.DatabaseProvider;
import com.lostwilderness.rpgcore.infra.messaging.ClusterMessagingService;
import com.lostwilderness.rpgcore.infra.messaging.StubClusterMessagingService;
import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import com.lostwilderness.rpgcore.player.PlayerModule;
import com.lostwilderness.rpgcore.progression.BetonQuestBridge;
import com.lostwilderness.rpgcore.progression.MilestonesMenu;
import com.lostwilderness.rpgcore.progression.ProgressionModule;
import com.lostwilderness.rpgcore.skills.SkillsModule;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import com.lostwilderness.rpgcore.progression.command.RewardsCommand;
import com.lostwilderness.rpgcore.progression.command.V2ClaimCommand;
import com.lostwilderness.rpgcore.progression.command.V2ProgressCommand;
import com.lostwilderness.rpgcore.skills.AuraSkillsBridge;
import com.lostwilderness.rpgcore.skills.SkillsDebugCommand;
import com.lostwilderness.rpgcore.skills.SkillXpService;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import com.lostwilderness.rpgcore.infra.db.SqlExecutor;

public final class RPGCorePlugin extends JavaPlugin {

    private static RPGCorePlugin instance;

    private ConfigService configService;
    private DatabaseProvider databaseProvider;
    private ClusterMessagingService messaging;
    private ModuleManager moduleManager;
    private ModuleContext moduleContext;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        try {
            configService = new ConfigService(this);
            configService.load();
        } catch (IOException e) {
            getLogger().severe("Failed to load config: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        databaseProvider = new DatabaseProvider(configService.getDbConfig());
        SqlExecutor sqlExecutor = new SqlExecutor(databaseProvider, Executors.newSingleThreadExecutor());
        SchedulerService scheduler = new SchedulerService(this);
        messaging = new StubClusterMessagingService();
        ServiceRegistry serviceRegistry = new ServiceRegistry();

        moduleContext = new ModuleContextImpl(
                this,
                configService,
                databaseProvider,
                sqlExecutor,
                scheduler,
                messaging,
                serviceRegistry);

        // So wrapper plugins (e.g. LW_Lobby_V2) can get the same DB via
        // getService(DatabaseProvider.class)
        serviceRegistry.register(DatabaseProvider.class, databaseProvider);

        moduleManager = new ModuleManager(moduleContext);
        Set<String> enabled = Set.copyOf(configService.getEnabledModules());
        getLogger().info("Enabled modules from config: " + String.join(", ", enabled));
        if (enabled.contains("player")) {
            moduleManager.register(new PlayerModule());
        }
        if (enabled.contains("skills")) {
            moduleManager.register(new SkillsModule());
        }
        if (enabled.contains("calendar")) {
            moduleManager.register(new CalendarModule());
        }
        if (enabled.contains("events")) {
            moduleManager.register(new EventsModule());
        }
        if (enabled.contains("progression")) {
            moduleManager.register(new ProgressionModule());
        }
        if (enabled.contains("clans")) {
            moduleManager.register(new ClansModule());
        }
        if (enabled.contains("boss")) {
            moduleManager.register(new BossModule());
        }
        if (enabled.contains("reputation")) {
            moduleManager.register(new ReputationModule());
        }
        if (enabled.contains("classes")) {
            moduleManager.register(new ClassesModule());
        }
        if (enabled.contains("portals")) {
            moduleManager.register(new com.lostwilderness.rpgcore.portals.PortalsModule());
        }
        if (enabled.contains("personality")) {
            moduleManager.register(new PersonalityModule());
        }
        if (enabled.contains("zodiac")) {
            moduleManager.register(new ZodiacModule());
        }
        if (enabled.contains("party")) {
            moduleManager.register(new PartyModule());
        }

        moduleManager.loadAll();
        moduleManager.enableAll();

        // Register commands (same pattern as v2ping so both definitely get an executor)
        org.bukkit.command.PluginCommand pingCmd = getCommand("v2ping");
        if (pingCmd != null) {
            pingCmd.setExecutor((sender, command, label, args) -> {
                sender.sendMessage(ChatColor.GREEN + "Pong! RPG_Core_V2 commands are working.");
                return true;
            });
            getLogger().info("Registered /v2ping test command.");
        }

        org.bukkit.command.PluginCommand dbCmd = getCommand("v2db");
        if (dbCmd != null) {
            dbCmd.setExecutor((sender, command, label, args) -> {
                if (!sender.isOp())
                    return true;
                String jdbcUrl = configService.getDbConfig().getString("datasources.player.jdbc-url", "");
                boolean isMysql = jdbcUrl.contains("mysql");
                String schemaQuery = isMysql
                        ? "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE()"
                        : "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";

                sqlExecutor.query("player", schemaQuery, rs -> {
                    List<String> tables = new ArrayList<>();
                    while (rs.next())
                        tables.add(rs.getString(1));
                    return tables;
                }).thenAccept(tables -> {
                    getLogger().info("[V2 DB Tables] " + String.join(", ", tables));
                    sender.sendMessage(ChatColor.GOLD + "[V2 DB Tables]");
                    if (tables.isEmpty())
                        sender.sendMessage(ChatColor.RED + "No tables found in "
                                + (isMysql ? "MySQL database" : "H2 PUBLIC schema"));
                    for (String t : tables)
                        sender.sendMessage(ChatColor.YELLOW + " - " + t);
                }).exceptionally(ex -> {
                    sender.sendMessage(ChatColor.RED + "Error: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
                return true;
            });
        }

        ProgressionService progressionService = enabled.contains("progression")
                ? serviceRegistry.get(ProgressionService.class)
                : null;
        org.bukkit.command.PluginCommand progressCmd = getCommand("v2progress");
        if (progressCmd != null) {
            if (progressionService != null) {
                progressCmd.setExecutor(new V2ProgressCommand(progressionService, this));
                getLogger().info("Registered /v2progress (ProgressionService ok).");
            } else {
                progressCmd.setExecutor((sender, command, label, args) -> {
                    sender.sendMessage(ChatColor.RED
                            + "Progression is not available. (Enable 'progression' in config/core.yml and ensure 'player' datasource in db.yml)");
                    return true;
                });
                getLogger().info("Registered /v2progress (fallback - no ProgressionService).");
            }
        } else {
            getLogger().severe("Failed to register /v2progress - getCommand returned null! Is it in plugin.yml?");
        }

        AuraSkillsBridge auraBridge = serviceRegistry.get(AuraSkillsBridge.class);
        BetonQuestBridge betonBridge = serviceRegistry.get(BetonQuestBridge.class);
        org.bukkit.command.PluginCommand claimCmd = getCommand("v2claim");
        if (claimCmd != null && progressionService != null) {
            SkillXpService skillXpService = serviceRegistry.get(SkillXpService.class);
            claimCmd.setExecutor(new V2ClaimCommand(progressionService, skillXpService, betonBridge,
                    moduleContext.getScheduler(), this));
            getLogger().info("Registered /v2claim (claim milestone rewards from menu or command).");
        } else if (claimCmd != null) {
            claimCmd.setExecutor((sender, cmd, label, args) -> {
                sender.sendMessage(ChatColor.RED + "Progression is not available.");
                return true;
            });
        }

        org.bukkit.command.PluginCommand skillsCmd = getCommand("v2skills");
        if (skillsCmd != null) {
            skillsCmd.setExecutor(new SkillsDebugCommand(auraBridge));
            getLogger().info("Registered /v2skills (AuraSkills debug for fighting level).");
        }

        // Built-in milestones GUI (no longer depends on BetonQuest menus)
        MilestonesMenu milestonesMenu = new MilestonesMenu(this);
        getServer().getPluginManager().registerEvents(milestonesMenu, this);
        RewardsCommand rewardsCommand = new RewardsCommand(milestonesMenu);
        org.bukkit.command.PluginCommand rewardsCmd = getCommand("rewards");
        if (rewardsCmd != null) {
            rewardsCmd.setExecutor(rewardsCommand);
            getLogger().info("Registered /rewards (opens BetonQuest milestones menu).");
        }
        org.bukkit.command.PluginCommand milestonesCmd = getCommand("milestones");
        if (milestonesCmd != null) {
            milestonesCmd.setExecutor(rewardsCommand);
        }

        ClanService clanService = enabled.contains("clans") ? serviceRegistry.get(ClanService.class) : null;
        WarService warService = enabled.contains("clans") ? serviceRegistry.get(WarService.class) : null;
        AllianceService allianceService = enabled.contains("clans") ? serviceRegistry.get(AllianceService.class) : null;
        org.bukkit.command.PluginCommand clanCmd = getCommand("clan");
        if (clanCmd != null) {
            if (clanService != null) {
                ClanCommand clanCommand = new ClanCommand(clanService);
                clanCmd.setExecutor(clanCommand);
                clanCmd.setTabCompleter(clanCommand);
                getLogger().info("Registered /clan (ClanService ok).");
            } else {
                clanCmd.setExecutor((sender, cmd, label, args) -> {
                    sender.sendMessage(ChatColor.RED
                            + "Clans are not available. Enable 'clans' in config/core.yml and ensure 'player' datasource in db.yml.");
                    return true;
                });
                getLogger().info("Registered /clan (fallback – no ClanService).");
            }
        }
        if (getCommand("war") != null && clanService != null && warService != null) {
            WarCommand warCommand = new WarCommand(clanService, warService);
            getCommand("war").setExecutor(warCommand);
            getCommand("war").setTabCompleter(warCommand);
            if (enabled.contains("clans")) {
                getServer().getPluginManager().registerEvents(
                        new com.lostwilderness.rpgcore.clans.listener.WarHeadDropListener(clanService, warService),
                        this);
            }
            getLogger().info("Registered /war and war head drops.");
        }
        if (getCommand("alliance") != null && clanService != null && allianceService != null) {
            AllianceCommand allianceCommand = new AllianceCommand(clanService, allianceService);
            getCommand("alliance").setExecutor(allianceCommand);
            getCommand("alliance").setTabCompleter(allianceCommand);
            getLogger().info("Registered /alliance.");
        }

        // Zodiac command
        ZodiacService zodiacService = enabled.contains("zodiac")
            ? serviceRegistry.get(ZodiacService.class)
            : null;
        org.bukkit.command.PluginCommand zodiacCmd = getCommand("zodiac");

        // Debug logging to identify which dependency is null
        getLogger().info("[ZODIAC DEBUG] zodiacCmd: " + (zodiacCmd != null ? "OK" : "NULL"));
        getLogger().info("[ZODIAC DEBUG] zodiacService: " + (zodiacService != null ? "OK" : "NULL"));
        getLogger().info("[ZODIAC DEBUG] clanService: " + (clanService != null ? "OK" : "NULL"));

        if (zodiacCmd != null && zodiacService != null && clanService != null) {
            zodiacCmd.setExecutor(new ZodiacCommand(zodiacService, clanService));
            getLogger().info("Registered /zodiac command.");
        } else {
            getLogger().warning("Failed to register /zodiac command - one or more dependencies is null (see debug above)");
        }

        getLogger().info("RPG_Core_V2 enabled.");
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.disableAll();
            moduleManager = null;
        }
        moduleContext = null;
        if (databaseProvider != null) {
            databaseProvider.close();
            databaseProvider = null;
        }
        if (messaging != null) {
            messaging.close();
            messaging = null;
        }
        getLogger().info("RPG_Core_V2 disabled.");
        instance = null;
    }

    public static RPGCorePlugin getInstance() {
        return instance;
    }

    /**
     * Allow wrapper plugins (Survival/Amplified) to fetch core services.
     */
    public <T> T getService(Class<T> type) {
        if (moduleContext == null) {
            return null;
        }
        return moduleContext.getServiceRegistry().get(type);
    }
}
