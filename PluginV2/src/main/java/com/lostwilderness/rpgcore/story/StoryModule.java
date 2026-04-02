package com.lostwilderness.rpgcore.story;

import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.clans.repo.ClanRepository;
import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.progression.BetonQuestBridge;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import com.lostwilderness.rpgcore.reputation.ReputationService;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * Story module — Phase 1 story gate listeners.
 *
 * Wires vanilla events (Ender Dragon death, Easter day) into progression milestones
 * and provides the first-join intro sequence.
 *
 * Dependencies: progression, reputation, calendar (all registered before story in core.yml)
 */
public final class StoryModule implements RpgModule {

    private ModuleContext ctx;
    private EnderDragonKillListener dragonListener;
    private StoryIntroListener introListener;
    private RedeemerEasterListener redeemerListener;
    private NetherEntryListener netherListener;
    private WitherKillListener witherKillListener;
    private FatherOfEnderKillListener fatherOfEnderListener;
    private StoryCompassSettings compassSettings;
    private WaypointZonesSettings waypointZonesSettings;
    private WaypointArrivalListener waypointArrivalListener;
    private AmosCompassTagListener amosCompassTagListener;

    @Override
    public String getName() {
        return "story";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("progression", "reputation", "calendar", "clans");
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;
        this.compassSettings = StoryCompassSettings.load(ctx.getPlugin());
        this.waypointZonesSettings = WaypointZonesSettings.load(ctx.getPlugin());
        ctx.getPlugin().getLogger().info("[story] Loaded — Phase 1 story gates active.");
    }

    @Override
    public void onEnable() {
        if (ctx == null) return;

        ProgressionService progression = ctx.getServiceRegistry().get(ProgressionService.class);
        ReputationService reputation = ctx.getServiceRegistry().get(ReputationService.class);

        if (progression == null) {
            ctx.getPlugin().getLogger().warning("[story] Skipped: ProgressionService not available.");
            return;
        }
        if (reputation == null) {
            ctx.getPlugin().getLogger().warning("[story] Skipped: ReputationService not available.");
            return;
        }

        dragonListener = new EnderDragonKillListener(progression, reputation, ctx.getPlugin());
        ctx.getPlugin().getServer().getPluginManager().registerEvents(dragonListener, ctx.getPlugin());

        introListener = new StoryIntroListener(progression, ctx.getPlugin());
        ctx.getPlugin().getServer().getPluginManager().registerEvents(introListener, ctx.getPlugin());

        redeemerListener = new RedeemerEasterListener(progression, reputation, ctx.getPlugin());
        ctx.getPlugin().getServer().getPluginManager().registerEvents(redeemerListener, ctx.getPlugin());

        netherListener = new NetherEntryListener(progression, ctx.getPlugin());
        ctx.getPlugin().getServer().getPluginManager().registerEvents(netherListener, ctx.getPlugin());

        BetonQuestBridge betonBridge = ctx.getServiceRegistry().get(BetonQuestBridge.class);
        if (compassSettings != null) {
            org.bukkit.command.PluginCommand compassCmd = Bukkit.getPluginCommand("v2thornwellcompass");
            if (compassCmd != null) {
                compassCmd.setExecutor(new ThornwellCompassCommand(ctx.getPlugin(), compassSettings));
            }
            amosCompassTagListener = new AmosCompassTagListener(ctx.getPlugin(), betonBridge);
            ctx.getPlugin().getServer().getPluginManager().registerEvents(amosCompassTagListener, ctx.getPlugin());
        }
        if (waypointZonesSettings != null && !waypointZonesSettings.getZones().isEmpty()) {
            waypointArrivalListener = new WaypointArrivalListener(ctx.getPlugin(), waypointZonesSettings.getZones(), betonBridge);
            waypointArrivalListener.start();
        }

        ClanService clanService = ctx.getServiceRegistry().get(ClanService.class);
        ClanRepository clanRepo = ctx.getServiceRegistry().get(ClanRepository.class);
        if (clanService != null && clanRepo != null) {
            witherKillListener = new WitherKillListener(progression, clanService, clanRepo, ctx.getPlugin());
            ctx.getPlugin().getServer().getPluginManager().registerEvents(witherKillListener, ctx.getPlugin());

            fatherOfEnderListener = new FatherOfEnderKillListener(progression, reputation, ctx.getPlugin());
            ctx.getPlugin().getServer().getPluginManager().registerEvents(fatherOfEnderListener, ctx.getPlugin());
        } else {
            ctx.getPlugin().getLogger().warning("[story] Wither/FatherOfEnder listeners skipped: ClanService or ClanRepository unavailable.");
        }

        ctx.getPlugin().getLogger().info("[story] Enabled — Dragon kill, intro, Redeemer Easter, Nether entry, Wither kill, Father of Ender listeners active.");
    }

    @Override
    public void onDisable() {
        if (dragonListener != null)       { HandlerList.unregisterAll(dragonListener);       dragonListener = null;       }
        if (introListener != null)        { HandlerList.unregisterAll(introListener);        introListener = null;        }
        if (redeemerListener != null)     { HandlerList.unregisterAll(redeemerListener);     redeemerListener = null;     }
        if (netherListener != null)       { HandlerList.unregisterAll(netherListener);       netherListener = null;       }
        if (witherKillListener != null)   { HandlerList.unregisterAll(witherKillListener);   witherKillListener = null;   }
        if (fatherOfEnderListener != null){ HandlerList.unregisterAll(fatherOfEnderListener);fatherOfEnderListener = null;}
        if (waypointArrivalListener != null) {
            waypointArrivalListener.stop();
            waypointArrivalListener = null;
        }
        if (amosCompassTagListener != null) {
            HandlerList.unregisterAll(amosCompassTagListener);
            amosCompassTagListener = null;
        }
        ctx = null;
    }
}
