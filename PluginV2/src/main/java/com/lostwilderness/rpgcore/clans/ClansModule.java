package com.lostwilderness.rpgcore.clans;

import com.lostwilderness.rpgcore.clans.listener.ClanDisplayListener;
import com.lostwilderness.rpgcore.clans.repo.ClanRepository;
import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Clans module: persistence (player datasource), ClanService, display listener, /clan command (registered by core).
 */
public final class ClansModule implements RpgModule {

    private static final String DATASOURCE = "player";

    private ModuleContext ctx;
    private ClanService clanService;
    private ClanDisplayListener displayListener;

    @Override
    public String getName() {
        return "clans";
    }

    @Override
    public List<String> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;
        if (ctx.getDatabaseProvider().getDataSource(DATASOURCE) == null) {
            ctx.getPlugin().getLogger().warning("[clans] Skipped: no 'player' datasource in db.yml.");
            return;
        }
        var executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpgcore-clans-db");
            t.setDaemon(true);
            return t;
        });
        SqlExecutor sql = new SqlExecutor(ctx.getDatabaseProvider(), executor);
        boolean mysql = isMysql(ctx);
        ClanRepository repo = new ClanRepository(sql, mysql);
        try {
            repo.createTablesIfNotExists();
        } catch (Exception e) {
            ctx.getPlugin().getLogger().severe("[clans] Failed to create tables: " + e.getMessage());
            throw new RuntimeException("Clans tables failed", e);
        }
        clanService = new ClanServiceImpl(repo, ctx.getPlugin());
        ctx.getServiceRegistry().register(ClanService.class, clanService);
        ctx.getServiceRegistry().register(WarService.class, new WarServiceImpl(repo));
        ctx.getServiceRegistry().register(AllianceService.class, new AllianceServiceImpl(repo));
        displayListener = new ClanDisplayListener(clanService);
        ctx.getPlugin().getLogger().info("[clans] Loaded. /clan, /war, /alliance and scoreboard display enabled.");
    }

    @Override
    public void onEnable() {
        if (clanService == null || ctx == null) return;
        Plugin plugin = ctx.getPlugin();
        plugin.getServer().getPluginManager().registerEvents(displayListener, plugin);
    }

    @Override
    public void onDisable() {
        displayListener = null;
        clanService = null;
        ctx = null;
    }

    private static boolean isMysql(ModuleContext ctx) {
        String url = ctx.getConfigService().getDbConfig().getString("datasources." + DATASOURCE + ".jdbc-url", "");
        return url != null && url.contains("mysql");
    }
}
