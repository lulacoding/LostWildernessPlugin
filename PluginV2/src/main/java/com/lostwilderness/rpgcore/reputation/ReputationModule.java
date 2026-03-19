package com.lostwilderness.rpgcore.reputation;

import com.lostwilderness.rpgcore.reputation.command.ReputationCommand;
import com.lostwilderness.rpgcore.reputation.listener.ReputationListener;
import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import com.lostwilderness.rpgcore.classes.ClassService;

public class ReputationModule implements RpgModule {

    private ModuleContext context;
    private ReputationRepository repository;
    private ReputationService service;

    @Override
    public void onLoad(ModuleContext ctx) {
        this.context = ctx;

        String jdbcUrl = ctx.getConfigService().getDbConfig().getString("datasources.player.jdbc-url", "");
        boolean mysql = jdbcUrl.contains("mysql") || jdbcUrl.contains("mariadb");

        this.repository = new ReputationRepository(
            ctx.getDatabaseProvider(),
            ctx.getSqlExecutor(),
            mysql
        );
        ClassService classService = ctx.getServiceRegistry().get(ClassService.class);
        this.service = new ReputationService(repository, classService);

        // Register service during onLoad so other modules can depend on it
        ctx.getServiceRegistry().register(ReputationService.class, service);
        ctx.getPlugin().getLogger().info("[reputation] Loaded and service registered.");
    }

    @Override
    public void onEnable() {
        RPGCorePlugin plugin = (RPGCorePlugin) context.getPlugin();

        // Register listeners
        var pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new ReputationListener(service, context), plugin);

        // Register commands
        plugin.getCommand("reputation").setExecutor(new ReputationCommand(service));
    }

    @Override
    public void onDisable() {
        // Cleanup if needed
    }

    @Override
    public String getName() {
        return "reputation";
    }

    @Override
    public java.util.List<String> getDependencies() {
        return java.util.List.of("player", "classes");
    }
}
