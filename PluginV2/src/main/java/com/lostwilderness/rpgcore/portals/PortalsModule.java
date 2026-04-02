package com.lostwilderness.rpgcore.portals;

import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.portals.command.DeletePortalsCommand;
import com.lostwilderness.rpgcore.portals.command.PortalsCommand;
import com.lostwilderness.rpgcore.portals.listener.PortalBreakListener;
import com.lostwilderness.rpgcore.portals.listener.PortalEnterListener;
import com.lostwilderness.rpgcore.portals.listener.PortalInteractListener;
import com.lostwilderness.rpgcore.portals.listener.PortalJoinListener;
import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public final class PortalsModule implements RpgModule {

    private static final String DATASOURCE = "player";

    private ModuleContext ctx;
    private PortalRepository repo;
    private PortalService portalService;
    private PortalsCommand portalsCommand;
    private DeletePortalsCommand deletePortalsCommand;

    @Override
    public String getName() {
        return "portals";
    }

    @Override
    public List<String> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;
        if (ctx.getDatabaseProvider().getDataSource(DATASOURCE) == null) {
            ctx.getPlugin().getLogger().warning("[portals] Skipped: no 'player' datasource in db.yml.");
            return;
        }
        var executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpgcore-portals-db");
            t.setDaemon(true);
            return t;
        });
        SqlExecutor sql = new SqlExecutor(ctx.getDatabaseProvider(), executor);
        boolean mysql = ctx.getConfigService().getDbConfig().getString("datasources." + DATASOURCE + ".jdbc-url", "")
                .contains("mysql");
        repo = new PortalRepository(sql, mysql);
        repo.createTablesIfNotExists();

        boolean isSurvival = "survival".equalsIgnoreCase(ctx.getConfigService().getServerRole());
        PortalsConfig config = new PortalsConfig(ctx.getPlugin(), isSurvival);
        portalService = new PortalService(repo, config, isSurvival, ctx.getScheduler(), ctx.getPlugin());
        ctx.getServiceRegistry().register(PortalService.class, portalService);

        portalsCommand = new PortalsCommand(repo, portalService);
        deletePortalsCommand = new DeletePortalsCommand(ctx.getPlugin(), repo, portalService);

        ctx.getPlugin().getLogger()
                .info("[portals] Loaded. /portals, /deleteportals and cross-server transfer enabled.");
    }

    @Override
    public void onEnable() {
        if (portalService == null || ctx == null)
            return;
        Plugin plugin = ctx.getPlugin();

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");

        ClanService clanService = ctx.getServiceRegistry().get(ClanService.class);
        plugin.getServer().getPluginManager()
                .registerEvents(new PortalInteractListener(portalService, clanService), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PortalEnterListener(plugin, portalService, repo),
                plugin);
        plugin.getServer().getPluginManager().registerEvents(new PortalJoinListener(plugin, portalService, repo),
                plugin);
        plugin.getServer().getPluginManager().registerEvents(new PortalBreakListener(plugin, portalService, repo),
                plugin);

        if (plugin instanceof JavaPlugin javaPlugin) {
            var portalsCmd = javaPlugin.getCommand("portals");
            if (portalsCmd != null)
                portalsCmd.setExecutor(portalsCommand);
            var deletePortalsCmd = javaPlugin.getCommand("deleteportals");
            if (deletePortalsCmd != null)
                deletePortalsCmd.setExecutor(deletePortalsCommand);
        }
    }

    @Override
    public void onDisable() {
        portalService = null;
        repo = null;
        portalsCommand = null;
        deletePortalsCommand = null;
        ctx = null;
    }
}
