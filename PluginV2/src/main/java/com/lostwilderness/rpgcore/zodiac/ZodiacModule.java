package com.lostwilderness.rpgcore.zodiac;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.reputation.ReputationService;
import com.lostwilderness.rpgcore.zodiac.listener.ZodiacEffectListener;
import com.lostwilderness.rpgcore.zodiac.listener.ZodiacJoinListener;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Zodiac module - manages player zodiac signs and applies perks.
 */
public final class ZodiacModule implements RpgModule {

    private ModuleContext ctx;
    private ZodiacServiceImpl service;
    private ZodiacJoinListener joinListener;
    private ZodiacEffectListener effectListener;

    @Override
    public String getName() {
        return "zodiac";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("player", "calendar", "reputation", "clans");
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;

        // Check dependencies
        if (ctx.getDatabaseProvider().getDataSource("player") == null) {
            ctx.getPlugin().getLogger().warning("[zodiac] Skipped: no 'player' datasource");
            return;
        }

        CalendarServiceV2 calendar = ctx.getServiceRegistry().get(CalendarServiceV2.class);
        if (calendar == null) {
            ctx.getPlugin().getLogger().warning("[zodiac] Skipped: CalendarServiceV2 not available");
            return;
        }

        ReputationService reputation = ctx.getServiceRegistry().get(ReputationService.class);
        if (reputation == null) {
            ctx.getPlugin().getLogger().warning("[zodiac] Skipped: ReputationService not available");
            return;
        }

        ClanService clans = ctx.getServiceRegistry().get(ClanService.class);
        if (clans == null) {
            ctx.getPlugin().getLogger().warning("[zodiac] Skipped: ClanService not available");
            return;
        }

        // Create executor for async DB operations
        var executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpgcore-zodiac-db");
            t.setDaemon(true);
            return t;
        });

        // Set up repository
        SqlExecutor sql = new SqlExecutor(ctx.getDatabaseProvider(), executor);
        boolean useMysql = isPlayerDatasourceMysql(ctx);
        ZodiacRepository repo = new ZodiacRepository(sql, useMysql);
        repo.createTableIfNotExists();

        // Initialize service
        service = new ZodiacServiceImpl(repo, calendar, clans, ctx.getPlugin());

        // Register in ServiceRegistry
        ctx.getServiceRegistry().register(ZodiacService.class, service);
        ctx.getPlugin().getLogger().info("[zodiac] Loaded and service registered.");
    }

    @Override
    public void onEnable() {
        if (ctx == null || service == null) {
            return;
        }

        CalendarServiceV2 calendar = ctx.getServiceRegistry().get(CalendarServiceV2.class);
        ReputationService reputation = ctx.getServiceRegistry().get(ReputationService.class);

        // Register join listener
        joinListener = new ZodiacJoinListener(service, calendar);
        ctx.getPlugin().getServer().getPluginManager().registerEvents(joinListener, ctx.getPlugin());

        // Register effect listener
        effectListener = new ZodiacEffectListener(service, reputation, calendar, ctx.getScheduler());
        ctx.getPlugin().getServer().getPluginManager().registerEvents(effectListener, ctx.getPlugin());

        ctx.getPlugin().getLogger().info("[zodiac] Enabled with listeners and effect scheduler.");
    }

    @Override
    public void onDisable() {
        if (joinListener != null) {
            HandlerList.unregisterAll(joinListener);
            joinListener = null;
        }
        if (effectListener != null) {
            effectListener.shutdown();
            HandlerList.unregisterAll(effectListener);
            effectListener = null;
        }
        ctx = null;
        service = null;
    }

    private boolean isPlayerDatasourceMysql(ModuleContext ctx) {
        String jdbcUrl = ctx.getConfigService().getDbConfig().getString("datasources.player.jdbc-url", "");
        return jdbcUrl.contains("mysql");
    }
}
