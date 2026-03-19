package com.lostwilderness.rpgcore.classes;

import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import com.lostwilderness.rpgcore.skills.AuraSkillsBridge;
import com.lostwilderness.rpgcore.skills.SkillXpService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public final class ClassesModule implements RpgModule {

    private ModuleContext ctx;
    private ClassService classService;
    private ClassSkillTreeService skillTreeService;

    @Override
    public String getName() {
        return "classes";
    }

    @Override
    public List<String> getDependencies() {
        // Depends on player + skills being present for best experience,
        // but will still mostly function if AuraSkills is missing.
        return Collections.emptyList();
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;

        // Ensure player datasource exists
        if (ctx.getDatabaseProvider().getDataSource("player") == null) {
            ctx.getPlugin().getLogger()
                    .warning("[classes] No 'player' datasource configured; ClassesModule will not be active.");
            return;
        }

        var executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpgcore-classes-db");
            t.setDaemon(true);
            return t;
        });
        SqlExecutor sql = new SqlExecutor(ctx.getDatabaseProvider(), executor);

        boolean useMysql = isPlayerDatasourceMysql(ctx);
        PlayerClassRepository repo = new PlayerClassRepository(sql, useMysql);
        repo.createTableIfNotExists();

        SkillXpService skillXpService = ctx.getServiceRegistry().get(SkillXpService.class);
        ConfigurationSection classConfig = ctx.getConfigService().getCoreConfig().getConfigurationSection("classes");
        if (classConfig == null) {
            classConfig = loadBundledClassConfig(ctx.getPlugin());
            if (classConfig != null) {
                ctx.getPlugin().getLogger().warning(
                        "[classes] Missing classes section in config/core.yml; using bundled defaults for starter-xp.");
            } else {
                ctx.getPlugin().getLogger().warning(
                        "[classes] Missing classes section and bundled default could not be loaded; starter-xp grants disabled.");
            }
        }

        classService = new ClassService(ctx.getPlugin(), repo, skillXpService, classConfig);
        ctx.getServiceRegistry().register(ClassService.class, classService);

        // Phase 4: Create ClassSkillTreeService
        AuraSkillsBridge auraBridge = ctx.getServiceRegistry().get(AuraSkillsBridge.class);
        skillTreeService = new ClassSkillTreeServiceImpl(ctx.getPlugin(), auraBridge, repo);
        ctx.getServiceRegistry().register(ClassSkillTreeService.class, skillTreeService);
    }

    @Override
    public void onEnable() {
        if (ctx == null || classService == null)
            return;
        RPGCorePlugin plugin = (RPGCorePlugin) ctx.getPlugin();

        ClassSelectionMenu menu = new ClassSelectionMenu(plugin, classService);
        ClassCommand classCommand = new ClassCommand(menu);
        if (plugin.getCommand("class") != null) {
            plugin.getCommand("class").setExecutor(classCommand);
            plugin.getLogger().info("[classes] Registered /class command executor.");
        } else {
            plugin.getLogger()
                    .severe("[classes] Could not register /class - command missing from plugin.yml at runtime.");
        }

        // Register GUI and listeners
        plugin.getServer().getPluginManager().registerEvents(menu, plugin);
        plugin.getServer().getPluginManager().registerEvents(new ClassStarterXpJoinListener(plugin, classService),
                plugin);

        // Phase 4: Create XP listener and pass to ability/passive listeners
        AuraSkillsBridge auraBridge = ctx.getServiceRegistry().get(AuraSkillsBridge.class);
        SkillXpService skillXpService = ctx.getServiceRegistry().get(SkillXpService.class);
        ClassMasteryXpListener xpListener = new ClassMasteryXpListener(
                classService, skillTreeService, skillXpService, plugin);
        plugin.getServer().getPluginManager().registerEvents(xpListener, plugin);

        plugin.getServer().getPluginManager().registerEvents(
                new ClassPassiveListener(classService, skillTreeService, xpListener), plugin);
        if (auraBridge != null) {
            plugin.getServer().getPluginManager().registerEvents(
                    new ClassAbilityListener(classService, auraBridge, skillTreeService, xpListener), plugin);
        }

        // Phase 8: Register custom mastery skills and mana abilities with AuraSkills
        if (auraBridge != null && auraBridge.isAvailable()) {
            try {
                AuraSkillsIntegration integration = new AuraSkillsIntegration(plugin);
                integration.registerCustomSkills();
                integration.registerManaAbilities();
                plugin.getLogger().info("[classes] AuraSkills integration complete - 5 skills + 5 mana abilities registered");
            } catch (Exception e) {
                plugin.getLogger().warning("[classes] Failed to register custom content with AuraSkills: " + e.getMessage());
                plugin.getLogger().warning("[classes] Class skill trees will still work, but won't appear in /skills GUI");
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().warning("[classes] AuraSkills not available - custom mastery skills not registered");
        }
    }

    @Override
    public void onDisable() {
        ctx = null;
        classService = null;
    }

    private static boolean isPlayerDatasourceMysql(ModuleContext ctx) {
        String url = ctx.getConfigService().getDbConfig().getString("datasources.player.jdbc-url", "");
        return url != null && url.contains("mysql");
    }

    private static ConfigurationSection loadBundledClassConfig(Plugin plugin) {
        try (InputStream in = plugin.getResource("config/core.yml")) {
            if (in == null) {
                return null;
            }
            YamlConfiguration bundled = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            return bundled.getConfigurationSection("classes");
        } catch (Exception ex) {
            plugin.getLogger().warning("[classes] Failed loading bundled class defaults: " + ex.getMessage());
            return null;
        }
    }
}
