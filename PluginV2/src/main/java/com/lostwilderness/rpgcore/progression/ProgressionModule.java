package com.lostwilderness.rpgcore.progression;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.progression.listener.ProgressionDeathListener;
import com.lostwilderness.rpgcore.progression.listener.ProgressionJoinListener;
import com.lostwilderness.rpgcore.progression.repo.ProgressionRepository;
import com.lostwilderness.rpgcore.skills.AuraSkillsBridge;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.Executors;

public final class ProgressionModule implements RpgModule {

    private ModuleContext ctx;
    private ProgressionService progressionService;

    @Override
    public String getName() {
        return "progression";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("player");
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;
        if (ctx.getDatabaseProvider().getDataSource("player") == null) {
            ctx.getPlugin().getLogger().warning("[progression] Skipped: no 'player' datasource in db.yml.");
            return;
        }
        var executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rpgcore-progression-db");
            t.setDaemon(true);
            return t;
        });
        SqlExecutor sql = new SqlExecutor(ctx.getDatabaseProvider(), executor);
        boolean useMysql = isPlayerDatasourceMysql(ctx);
        ProgressionRepository repo = new ProgressionRepository(sql, useMysql);
        try {
            repo.createTableIfNotExists();
        } catch (Exception e) {
            ctx.getPlugin().getLogger().severe("[progression] Failed to create tables: " + e.getMessage());
            throw new RuntimeException("Progression tables failed", e);
        }
        progressionService = new ProgressionService(repo);
        ctx.getServiceRegistry().register(ProgressionService.class, progressionService);
        // Optional BetonQuest bridge (tags/journal on unlock/claim)
        BetonQuestBridge betonBridge = new BetonQuestBridge(ctx.getPlugin());
        ctx.getServiceRegistry().register(BetonQuestBridge.class, betonBridge);
        ctx.getPlugin().getLogger().info("[progression] Loaded. /v2progress and join milestones enabled.");
    }

    @Override
    public void onEnable() {
        if (progressionService == null || ctx == null)
            return;
        Plugin plugin = ctx.getPlugin();
        AuraSkillsBridge auraBridge = ctx.getServiceRegistry().get(AuraSkillsBridge.class);
        BetonQuestBridge betonBridge = ctx.getServiceRegistry().get(BetonQuestBridge.class);
        CalendarServiceV2 calendarService = ctx.getServiceRegistry().get(CalendarServiceV2.class);
        ClanService clanService = ctx.getServiceRegistry().get(ClanService.class);
        com.lostwilderness.rpgcore.personality.TraitService traitService = ctx.getServiceRegistry().get(com.lostwilderness.rpgcore.personality.TraitService.class);
        plugin.getServer().getPluginManager().registerEvents(
                new ProgressionJoinListener(progressionService, ctx.getScheduler(), plugin, auraBridge, betonBridge,
                        calendarService, clanService, traitService),
                plugin);
        plugin.getServer().getPluginManager().registerEvents(
                new ProgressionDeathListener(progressionService, ctx.getScheduler(), plugin),
                plugin);
        // /v2progress is registered by RPGCorePlugin so it always exists
    }

    @Override
    public void onDisable() {
        progressionService = null;
        ctx = null;
    }

    private static boolean isPlayerDatasourceMysql(ModuleContext ctx) {
        String url = ctx.getConfigService().getDbConfig().getString("datasources.player.jdbc-url", "");
        return url != null && url.contains("mysql");
    }
}
