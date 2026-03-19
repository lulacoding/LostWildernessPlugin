package com.lostwilderness.rpgcore.core;

import com.lostwilderness.rpgcore.infra.config.ConfigService;
import com.lostwilderness.rpgcore.infra.db.DatabaseProvider;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.infra.messaging.ClusterMessagingService;
import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import org.bukkit.plugin.Plugin;

/**
 * Context passed to modules in onLoad. Exposes only infra and service registry.
 */
public interface ModuleContext {

    Plugin getPlugin();

    ConfigService getConfigService();

    DatabaseProvider getDatabaseProvider();

    SqlExecutor getSqlExecutor();

    SchedulerService getScheduler();

    ClusterMessagingService getMessaging();

    ServiceRegistry getServiceRegistry();
}
