package com.lostwilderness.rpgcore.personality;

import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Personality trait & elemental quest system module.
 * Handles trait assignment, progression, Ultimate items, and Holy Enchants.
 */
public final class PersonalityModule implements RpgModule {

    private ModuleContext ctx;
    private TraitService traitService;
    private TraitRepository repository;

    @Override
    public String getName() {
        return "personality";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("player", "progression");
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;

        // Check database availability
        if (ctx.getDatabaseProvider().getDataSource("player") == null) {
            ctx.getPlugin().getLogger().warning("[personality] No 'player' datasource configured. Module disabled.");
            return;
        }

        // Fetch dependencies
        ProgressionService progressionService = ctx.getServiceRegistry().get(ProgressionService.class);
        if (progressionService == null) {
            ctx.getPlugin().getLogger().severe("[personality] ProgressionService not found! Is progression module enabled?");
            return;
        }

        Plugin plugin = ctx.getPlugin();

        // Create dedicated executor for database operations
        Executor dbExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpgcore-personality-db");
            t.setDaemon(true);
            return t;
        });

        // Create SqlExecutor
        SqlExecutor sqlExecutor = new SqlExecutor(ctx.getDatabaseProvider(), dbExecutor);

        // Determine if using MySQL or H2
        boolean useMysql = isPlayerDatasourceMysql(ctx);

        // Create repository and initialize tables
        repository = new TraitRepository(sqlExecutor, dbExecutor, useMysql);
        repository.createTablesIfNotExists();

        // Create services
        CompletionService completionService = new CompletionService(progressionService);
        HolyEnchantService holyEnchantService = new HolyEnchantService(plugin);
        traitService = new TraitServiceImpl(
            repository,
            progressionService,
            completionService,
            holyEnchantService,
            plugin
        );

        // Register services
        ctx.getServiceRegistry().register(TraitService.class, traitService);
        ctx.getServiceRegistry().register(CompletionService.class, completionService);
        ctx.getServiceRegistry().register(HolyEnchantService.class, holyEnchantService);

        plugin.getLogger().info("[personality] Loaded and services registered.");
    }

    @Override
    public void onEnable() {
        if (traitService == null || ctx == null) return;

        // Listeners will be registered in Phase 4 (survival plugin)
        // This module provides services only; listeners live in survival plugin

        ctx.getPlugin().getLogger().info("[personality] Module enabled.");
    }

    @Override
    public void onDisable() {
        traitService = null;
        repository = null;
        ctx = null;
    }

    /**
     * Detect if player datasource is MySQL/MariaDB (vs H2).
     */
    private static boolean isPlayerDatasourceMysql(ModuleContext ctx) {
        String url = ctx.getConfigService().getDbConfig().getString("datasources.player.jdbc-url", "");
        return url != null && url.contains("mysql");
    }
}
