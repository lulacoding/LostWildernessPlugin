package com.lostwilderness.rpgcore.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads and enables modules in dependency order; disables in reverse order.
 */
public final class ModuleManager {

    private final ModuleContext ctx;
    private final List<RpgModule> loadOrder = new ArrayList<>();
    private final Map<String, RpgModule> byName = new HashMap<>();

    public ModuleManager(ModuleContext ctx) {
        this.ctx = ctx;
    }

    public void register(RpgModule module) {
        if (byName.containsKey(module.getName())) return;
        byName.put(module.getName(), module);
        loadOrder.add(module);
    }

    public void loadAll() {
        sortByDependencies();
        for (RpgModule m : loadOrder) {
            m.onLoad(ctx);
        }
    }

    public void enableAll() {
        for (RpgModule m : loadOrder) {
            m.onEnable();
        }
    }

    public void disableAll() {
        for (int i = loadOrder.size() - 1; i >= 0; i--) {
            try {
                loadOrder.get(i).onDisable();
            } catch (Exception e) {
                // log if you have logger in context
            }
        }
    }

    private void sortByDependencies() {
        List<RpgModule> sorted = new ArrayList<>();
        Set<String> added = new HashSet<>();
        while (sorted.size() < loadOrder.size()) {
            boolean progress = false;
            for (RpgModule m : loadOrder) {
                if (added.contains(m.getName())) continue;
                boolean depsOk = m.getDependencies().stream().allMatch(added::contains);
                if (depsOk) {
                    sorted.add(m);
                    added.add(m.getName());
                    progress = true;
                }
            }
            if (!progress) break; // cycle or missing dep
        }
        loadOrder.clear();
        loadOrder.addAll(sorted);
    }
}
