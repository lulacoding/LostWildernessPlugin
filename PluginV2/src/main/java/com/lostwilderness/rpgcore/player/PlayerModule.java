package com.lostwilderness.rpgcore.player;

import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import com.lostwilderness.rpgcore.player.listener.PlayerProfilePreloadListener;
import com.lostwilderness.rpgcore.player.listener.PlayerSessionListener;
import com.lostwilderness.rpgcore.player.repo.PlayerProfileRepository;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executors;

public final class PlayerModule implements RpgModule {

    private ModuleContext ctx;
    private PlayerProfileService profileService;
    private PlayerProfileRepository repo;

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;
        var db = ctx.getDatabaseProvider();
        if (db.getDataSource("player") == null) {
            return; // no player datasource configured
        }
        var executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpgcore-player-db");
            t.setDaemon(true);
            return t;
        });
        SqlExecutor sql = new SqlExecutor(ctx.getDatabaseProvider(), executor);
        boolean useMysql = isPlayerDatasourceMysql(ctx);
        repo = new PlayerProfileRepository(sql, executor, useMysql);
        repo.createTableIfNotExists();
        profileService = new PlayerProfileService(repo, ctx.getScheduler());
        ctx.getServiceRegistry().register(PlayerProfileService.class, profileService);
    }

    @Override
    public void onEnable() {
        if (profileService == null || ctx == null) return;
        Plugin plugin = ctx.getPlugin();
        plugin.getServer().getPluginManager().registerEvents(
            new PlayerProfilePreloadListener(profileService), plugin);
        plugin.getServer().getPluginManager().registerEvents(
            new PlayerSessionListener(profileService), plugin);
    }

    @Override
    public void onDisable() {
        profileService = null;
        repo = null;
        ctx = null;
    }

    private static boolean isPlayerDatasourceMysql(ModuleContext ctx) {
        String url = ctx.getConfigService().getDbConfig().getString("datasources.player.jdbc-url", "");
        return url != null && url.contains("mysql");
    }
}
