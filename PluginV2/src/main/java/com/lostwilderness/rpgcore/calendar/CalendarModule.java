package com.lostwilderness.rpgcore.calendar;

import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public final class CalendarModule implements RpgModule {

    private ModuleContext ctx;
    private CalendarServiceV2Impl service;
    private CalendarDayChangeTitleListener dayChangeTitleListener;

    @Override
    public String getName() {
        return "calendar";
    }

    @Override
    public List<String> getDependencies() {
        // Needs player datasource; rely on same DB setup as progression.
        return Collections.singletonList("player");
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;
        if (ctx.getDatabaseProvider().getDataSource("player") == null) {
            ctx.getPlugin().getLogger().warning("[calendar] Skipped: no 'player' datasource in db.yml.");
            return;
        }
        var executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpgcore-calendar-db");
            t.setDaemon(true);
            return t;
        });
        SqlExecutor sql = new SqlExecutor(ctx.getDatabaseProvider(), executor);
        boolean useMysql = isPlayerDatasourceMysql(ctx);
        CalendarRepository repo = new CalendarRepository(sql, useMysql);
        repo.createTablesIfNotExists();
        boolean isLeader = "survival".equalsIgnoreCase(ctx.getConfigService().getServerRole());
        service = new CalendarServiceV2Impl(ctx.getPlugin(), repo, ctx.getScheduler(), isLeader);
        service.load().join();
        ctx.getServiceRegistry().register(CalendarServiceV2.class, service);
        ctx.getPlugin().getLogger().info("[calendar] Loaded and initial state ready.");
    }

    @Override
    public void onEnable() {
        if (service != null) {
            service.startDayChangeTask();
            dayChangeTitleListener = new CalendarDayChangeTitleListener(ctx.getPlugin());
            ctx.getPlugin().getServer().getPluginManager().registerEvents(dayChangeTitleListener, ctx.getPlugin());
        }
    }

    @Override
    public void onDisable() {
        if (dayChangeTitleListener != null) {
            HandlerList.unregisterAll(dayChangeTitleListener);
            dayChangeTitleListener = null;
        }
        ctx = null;
        service = null;
    }

    private static boolean isPlayerDatasourceMysql(ModuleContext ctx) {
        String url = ctx.getConfigService().getDbConfig().getString("datasources.player.jdbc-url", "");
        return url != null && url.contains("mysql");
    }
}

