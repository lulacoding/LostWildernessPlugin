package com.lostwilderness.rpgcore.pets;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.pets.command.PetCommand;
import com.lostwilderness.rpgcore.pets.listener.PetDeathListener;
import com.lostwilderness.rpgcore.pets.listener.PetReconciliationListener;
import com.lostwilderness.rpgcore.pets.listener.PetTameListener;
import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.pets.listener.PetCollarListener;
import com.lostwilderness.rpgcore.pets.listener.PetPersonalityListener;
import com.lostwilderness.rpgcore.pets.listener.GoldenTamingListener;
import com.lostwilderness.rpgcore.pets.task.PetPersonalityEffectTask;
import com.lostwilderness.rpgcore.pets.task.PetZodiacEffectTask;
import com.lostwilderness.rpgcore.zodiac.ZodiacService;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Pet tracking module - Phase 1: Core infrastructure only.
 * Tracks tamed animals with zodiac signs and personalities.
 */
public final class PetModule implements RpgModule {

    private ModuleContext ctx;
    private PetServiceImpl service;
    private ZodiacService zodiacService;
    private ClanService clanService;
    private PetTameListener tameListener;
    private PetDeathListener deathListener;
    private PetReconciliationListener reconciliationListener;
    private PetZodiacEffectTask zodiacEffectTask;
    private PetPersonalityEffectTask personalityEffectTask;
    private PetPersonalityListener personalityListener;
    private PetCollarListener collarListener;
    private GoldenTamingListener goldenTamingListener;

    @Override
    public String getName() {
        return "pets";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("player", "calendar", "zodiac");
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;

        // Check datasource
        if (ctx.getDatabaseProvider().getDataSource("player") == null) {
            ctx.getPlugin().getLogger().warning("[pets] Skipped: no 'player' datasource");
            return;
        }

        // Check calendar dependency
        CalendarServiceV2 calendar = ctx.getServiceRegistry().get(CalendarServiceV2.class);
        if (calendar == null) {
            ctx.getPlugin().getLogger().warning("[pets] Skipped: CalendarServiceV2 not available");
            return;
        }

        // Create DB executor
        var executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpgcore-pets-db");
            t.setDaemon(true);
            return t;
        });

        // Create repository
        SqlExecutor sql = new SqlExecutor(ctx.getDatabaseProvider(), executor);
        boolean useMysql = isPlayerDatasourceMysql(ctx);
        PetRepository repo = new PetRepository(sql, useMysql);
        repo.createTableIfNotExists();

        // Get zodiac service (loaded before pets due to dependency order)
        zodiacService = ctx.getServiceRegistry().get(ZodiacService.class);
        // ClanService is optional for collar colors
        clanService = ctx.getServiceRegistry().get(ClanService.class);
        if (zodiacService == null) {
            ctx.getPlugin().getLogger().warning("[pets] ZodiacService not available - zodiac effects disabled");
        }

        // Initialize golden taming items (registers PDC key)
        GoldenTamingItems.initialize(ctx.getPlugin());

        // Create service
        service = new PetServiceImpl(repo, calendar, ctx.getPlugin());

        // Register in ServiceRegistry
        ctx.getServiceRegistry().register(PetService.class, service);

        ctx.getPlugin().getLogger().info("[pets] Loaded - Phase 1 (Core Infrastructure)");
    }

    @Override
    public void onEnable() {
        if (ctx == null || service == null) {
            return;
        }

        // Register listeners
        tameListener = new PetTameListener(service, ctx.getPlugin());
        ctx.getPlugin().getServer().getPluginManager().registerEvents(tameListener, ctx.getPlugin());

        deathListener = new PetDeathListener(service, ctx.getPlugin());
        ctx.getPlugin().getServer().getPluginManager().registerEvents(deathListener, ctx.getPlugin());

        reconciliationListener = new PetReconciliationListener(service, ctx.getPlugin());
        ctx.getPlugin().getServer().getPluginManager().registerEvents(reconciliationListener, ctx.getPlugin());

        // Register command
        if (ctx.getPlugin() instanceof JavaPlugin javaPlugin) {
            PluginCommand petsCmd = javaPlugin.getCommand("pets");
            if (petsCmd != null) {
                PetCommand petCommand = new PetCommand(service);
                petsCmd.setExecutor(petCommand);
                petsCmd.setTabCompleter(petCommand);
            }
        }

        // Start zodiac effect task (Phase 3)
        if (zodiacService != null) {
            zodiacEffectTask = new PetZodiacEffectTask(service, zodiacService, ctx.getPlugin());
            zodiacEffectTask.start();
        }

        // Register collar listener (Phase 5)
        if (clanService != null) {
            collarListener = new PetCollarListener(service, clanService, ctx.getPlugin());
            ctx.getPlugin().getServer().getPluginManager().registerEvents(collarListener, ctx.getPlugin());
        }

        // Register personality event listener (Phase 4)
        personalityListener = new PetPersonalityListener(service, ctx.getPlugin());
        ctx.getPlugin().getServer().getPluginManager().registerEvents(personalityListener, ctx.getPlugin());

        // Start personality passive/scheduled task (Phase 4)
        personalityEffectTask = new PetPersonalityEffectTask(service, ctx.getPlugin());
        personalityEffectTask.start();

        // Register golden taming listener + recipes (Phase 6)
        goldenTamingListener = new GoldenTamingListener(service, ctx.getPlugin());
        ctx.getPlugin().getServer().getPluginManager().registerEvents(goldenTamingListener, ctx.getPlugin());
        GoldenTamingItems.registerRecipes(ctx.getPlugin());

        ctx.getPlugin().getLogger().info("[pets] Enabled - Phase 6 (Golden Taming Items)");
    }

    @Override
    public void onDisable() {
        if (tameListener != null) {
            HandlerList.unregisterAll(tameListener);
            tameListener = null;
        }
        if (deathListener != null) {
            HandlerList.unregisterAll(deathListener);
            deathListener = null;
        }
        if (reconciliationListener != null) {
            HandlerList.unregisterAll(reconciliationListener);
            reconciliationListener = null;
        }
        if (zodiacEffectTask != null) {
            zodiacEffectTask.stop();
            zodiacEffectTask = null;
        }
        if (personalityEffectTask != null) {
            personalityEffectTask.stop();
            personalityEffectTask = null;
        }
        if (personalityListener != null) {
            HandlerList.unregisterAll(personalityListener);
            personalityListener = null;
        }
        if (collarListener != null) {
            HandlerList.unregisterAll(collarListener);
            collarListener = null;
        }
        if (goldenTamingListener != null) {
            HandlerList.unregisterAll(goldenTamingListener);
            goldenTamingListener = null;
        }
        ctx = null;
        service = null;
        zodiacService = null;
        clanService = null;
    }

    private boolean isPlayerDatasourceMysql(ModuleContext ctx) {
        String jdbcUrl = ctx.getConfigService().getDbConfig().getString("datasources.player.jdbc-url", "");
        return jdbcUrl != null && jdbcUrl.contains("mysql");
    }
}
