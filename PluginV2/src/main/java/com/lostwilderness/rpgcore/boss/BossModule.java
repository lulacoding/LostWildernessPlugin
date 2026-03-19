package com.lostwilderness.rpgcore.boss;

import com.lostwilderness.rpgcore.boss.command.ArenaTestCommand;
import com.lostwilderness.rpgcore.boss.command.DiabloLairCommand;
import com.lostwilderness.rpgcore.boss.listener.*;
import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BossModule implements RpgModule {

    private ModuleContext context;
    private ArenaManager arenaManager;
    private BossKillRepository bossKillRepository;

    @Override
    public void onLoad(ModuleContext ctx) {
        this.context = ctx;
        this.arenaManager = new ArenaManager();

        String jdbcUrl = ctx.getConfigService().getDbConfig().getString("datasources.player.jdbc-url", "");
        boolean mysql = jdbcUrl.contains("mysql") || jdbcUrl.contains("mariadb");

        this.bossKillRepository = new BossKillRepository(
                ctx.getDatabaseProvider(),
                ctx.getSqlExecutor(),
                mysql);
    }

    @Override
    public void onEnable() {
        RPGCorePlugin plugin = (RPGCorePlugin) context.getPlugin();

        // Register ArenaManager in context services
        context.getServiceRegistry().register(ArenaManager.class, arenaManager);

        BossService bossService = new BossService(plugin, arenaManager);
        context.getServiceRegistry().register(BossService.class, bossService);

        boolean amplified = plugin.getConfig().getString("server-name", "").equalsIgnoreCase("amplified");

        // Register listeners
        var pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new RoofWitherListener(plugin, amplified, bossKillRepository, bossService, arenaManager),
                plugin);
        pm.registerEvents(new BossAbilityListener(), plugin);
        pm.registerEvents(new ArenaBoundaryListener(arenaManager), plugin);
        pm.registerEvents(new BossKillTracker(plugin, bossKillRepository, context), plugin);
        pm.registerEvents(new BossDropListener(), plugin);

        // Register commands
        plugin.getCommand("arena-test").setExecutor(new ArenaTestCommand(arenaManager));
        plugin.getCommand("diablo-lair").setExecutor(new DiabloLairCommand());
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) {
            arenaManager.clear();
        }
    }

    @Override
    public String getName() {
        return "Boss";
    }

}

    
    
        
    

