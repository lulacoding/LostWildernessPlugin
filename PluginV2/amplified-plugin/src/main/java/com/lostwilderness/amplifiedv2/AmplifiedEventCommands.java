package com.lostwilderness.amplifiedv2;

import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import com.lostwilderness.rpgcore.events.EventService;
import com.lostwilderness.rpgcore.events.SeasonalEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Read-only event commands for Amplified: list and info only (no start/stop).
 */
final class AmplifiedEventCommands implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final EventService events;

    AmplifiedEventCommands(JavaPlugin plugin) {
        this.plugin = plugin;
        RPGCorePlugin core = RPGCorePlugin.getInstance();
        this.events = core != null ? core.getService(EventService.class) : null;
    }

    void register() {
        var cmd = plugin.getCommand("event");
        if (cmd == null) {
            plugin.getLogger().warning("[events] Command /event not declared in plugin.yml");
            return;
        }
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (events == null) {
            sender.sendMessage(ChatColor.RED + "Event service is not available.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /event <list|info>");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender);
            default -> sender.sendMessage(ChatColor.GRAY + "Usage: /event <list|info>");
        }
        return true;
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Registered events:");
        for (String id : events.getRegisteredEventIds()) {
            SeasonalEvent ev = events.getEventById(id);
            String name = ev != null ? ev.getDisplayName() : id;
            sender.sendMessage(ChatColor.YELLOW + "  - " + id + ChatColor.GRAY + " (" + name + ")");
        }
    }

    private void handleInfo(CommandSender sender) {
        SeasonalEvent active = events.getActive();
        if (active == null) {
            sender.sendMessage(ChatColor.GRAY + "No event active.");
            return;
        }
        int days = events.getDaysRemaining();
        sender.sendMessage(ChatColor.GOLD + "Active: " + ChatColor.YELLOW + active.getId()
            + ChatColor.GRAY + " (" + active.getDisplayName() + "), "
            + ChatColor.WHITE + days + " day(s) remaining");
        SeasonalEvent queued = events.getQueuedTomorrow();
        if (queued != null) {
            sender.sendMessage(ChatColor.GRAY + "Queued for tomorrow: " + queued.getId() + " (" + queued.getDisplayName() + ")");
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (events == null) return out;
        if (args.length == 1) {
            String p = args[0].toLowerCase(Locale.ROOT);
            for (String s : new String[]{"list", "info"}) {
                if (s.startsWith(p)) out.add(s);
            }
        }
        return out;
    }
}
