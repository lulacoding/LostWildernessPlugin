package com.lostwilderness.survivalv2;

import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import com.lostwilderness.rpgcore.events.EventService;
import com.lostwilderness.rpgcore.events.SeasonalEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class SurvivalEventCommands implements CommandExecutor, TabCompleter {

    private static final String PERM_START_STOP = "lw.admin.events";

    private final JavaPlugin plugin;
    private final EventService events;

    SurvivalEventCommands(JavaPlugin plugin) {
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
            sendUsage(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender);
            case "start" -> handleStart(sender, args);
            case "stop" -> handleStop(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "Usage: /event <list|info|start|stop> [id] [days]");
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

    private void handleStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERM_START_STOP)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to start events.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /event start <id> [days]");
            return;
        }
        String id = args[1].toLowerCase(Locale.ROOT);
        Integer days = null;
        if (args.length >= 3) {
            try {
                days = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {}
        }
        if (events.forceStart(id, days)) {
            sender.sendMessage(ChatColor.GREEN + "Started event: " + id);
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown event or failed to start: " + id);
        }
    }

    private void handleStop(CommandSender sender) {
        if (!sender.hasPermission(PERM_START_STOP)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to stop events.");
            return;
        }
        if (events.getActive() == null) {
            sender.sendMessage(ChatColor.GRAY + "No event active.");
            return;
        }
        events.forceStop();
        sender.sendMessage(ChatColor.GREEN + "Event stopped.");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (events == null) return out;
        if (args.length == 1) {
            String p = args[0].toLowerCase(Locale.ROOT);
            for (String s : new String[]{"list", "info", "start", "stop"}) {
                if (s.startsWith(p)) out.add(s);
            }
            return out;
        }
        if (args.length == 2 && "start".equalsIgnoreCase(args[0]) && sender.hasPermission(PERM_START_STOP)) {
            String p = args[1].toLowerCase(Locale.ROOT);
            for (String id : events.getRegisteredEventIds()) {
                if (id.startsWith(p)) out.add(id);
            }
        }
        return out;
    }
}
