package com.lostwilderness.rpgcore.events;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows currently active events (daily + seasonal) in a boss bar to overworld players.
 */
final class EventBossBarManager {

    private final Plugin plugin;
    private final BossBar bossBar;
    private String lastTitle = "";
    private boolean visible = false;

    EventBossBarManager(Plugin plugin) {
        this.plugin = plugin;
        this.bossBar = Bukkit.createBossBar("", BarColor.PURPLE, BarStyle.SOLID);
        this.bossBar.setVisible(false);
        this.bossBar.setProgress(1.0);
    }

    void updateFromEvents(List<DailyWorldEvent> dailyEvents, SeasonalEvent activeSeasonal) {
        boolean enabled = plugin.getConfig().getBoolean("events.boss-bar.enabled", true);
        if (!enabled) {
            hide();
            return;
        }

        List<String> names = new ArrayList<>();
        for (DailyWorldEvent ev : dailyEvents) {
            if (ev.isActive()) names.add(ev.getDisplayName());
        }
        if (activeSeasonal != null) {
            names.add(activeSeasonal.getDisplayName());
        }

        if (names.isEmpty()) {
            hide();
            return;
        }

        String title = "§dEvent: §f" + String.join("§7, §f", names);
        if (!title.equals(lastTitle)) {
            bossBar.setTitle(title);
            lastTitle = title;
        }
        showToOverworldPlayers();
    }

    void tick() {
        if (!visible) return;
        showToOverworldPlayers();
    }

    private void showToOverworldPlayers() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            for (Player p : w.getPlayers()) {
                bossBar.addPlayer(p);
            }
        }
        bossBar.setVisible(true);
        visible = true;
    }

    private void hide() {
        bossBar.removeAll();
        bossBar.setVisible(false);
        visible = false;
        lastTitle = "";
    }
}
