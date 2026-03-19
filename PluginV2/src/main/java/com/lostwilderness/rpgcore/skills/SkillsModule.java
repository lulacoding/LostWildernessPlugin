package com.lostwilderness.rpgcore.skills;

import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * Optional module that registers the AuraSkills bridge when the plugin is
 * present.
 * Other modules can get AuraSkillsBridge from the service registry to grant XP
 * or check levels.
 */
public final class SkillsModule implements RpgModule {

    private ModuleContext ctx;
    private AuraSkillsBridge bridge;
    private SkillXpService skillXpService;

    @Override
    public String getName() {
        return "skills";
    }

    @Override
    public List<String> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;
        Plugin plugin = ctx.getPlugin();
        bridge = new AuraSkillsBridge(plugin);
        skillXpService = new SkillXpService(bridge);
        ctx.getServiceRegistry().register(AuraSkillsBridge.class, bridge);
        ctx.getServiceRegistry().register(SkillXpService.class, skillXpService);
    }

    @Override
    public void onEnable() {
        // Nothing to enable; bridge is used by other modules via registry
    }

    @Override
    public void onDisable() {
        skillXpService = null;
        bridge = null;
        ctx = null;
    }
}
