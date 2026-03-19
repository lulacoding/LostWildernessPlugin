package com.lostwilderness.rpgcore.core;

import com.lostwilderness.rpgcore.infra.config.ConfigService;
import com.lostwilderness.rpgcore.infra.db.DatabaseProvider;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.infra.messaging.ClusterMessagingService;
import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import org.bukkit.plugin.Plugin;

public final class ModuleContextImpl implements ModuleContext {

    private final Plugin plugin;
    private final ConfigService configService;
    private final DatabaseProvider databaseProvider;
    private final SqlExecutor sqlExecutor;
    private final SchedulerService scheduler;
    private final ClusterMessagingService messaging;
    private final ServiceRegistry serviceRegistry;

    public ModuleContextImpl(Plugin plugin,
                             ConfigService configService,
                             DatabaseProvider databaseProvider,
                             SqlExecutor sqlExecutor,
                             SchedulerService scheduler,
                             ClusterMessagingService messaging,
                             ServiceRegistry serviceRegistry) {
        this.plugin = plugin;
        this.configService = configService;
        this.databaseProvider = databaseProvider;
        this.sqlExecutor = sqlExecutor;
        this.scheduler = scheduler;
        this.messaging = messaging;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public ConfigService getConfigService() {
        return configService;
    }

    @Override
    public DatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }

    @Override
    public SqlExecutor getSqlExecutor() {
        return sqlExecutor;
    }

    @Override
    public SchedulerService getScheduler() {
        return scheduler;
    }

    @Override
    public ClusterMessagingService getMessaging() {
        return messaging;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
}
