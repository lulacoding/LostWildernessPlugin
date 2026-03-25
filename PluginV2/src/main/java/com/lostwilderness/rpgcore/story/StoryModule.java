package com.lostwilderness.rpgcore.story;

import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import com.lostwilderness.rpgcore.reputation.ReputationService;
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

    @Override
    public String getName() {
        return "story";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("progression", "reputation", "calendar");
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;
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

        ctx.getPlugin().getLogger().info("[story] Enabled — Dragon kill, intro, Redeemer Easter, Nether entry listeners active.");
    }

    @Override
    public void onDisable() {
        if (dragonListener != null) { HandlerList.unregisterAll(dragonListener); dragonListener = null; }
        if (introListener != null)  { HandlerList.unregisterAll(introListener);  introListener = null;  }
        if (redeemerListener != null) { HandlerList.unregisterAll(redeemerListener); redeemerListener = null; }
        if (netherListener != null)   { HandlerList.unregisterAll(netherListener);   netherListener = null;   }
        ctx = null;
    }
}
