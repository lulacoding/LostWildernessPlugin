package com.lostwilderness.amplifiedv2;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.calendar.EquatorSettings;
import com.lostwilderness.rpgcore.calendar.EquatorZone;
import com.lostwilderness.rpgcore.calendar.SeasonGuideBook;
import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

final class AmplifiedCalendarCommands {

    private final JavaPlugin plugin;
    private final CalendarServiceV2 calendar;

    AmplifiedCalendarCommands(JavaPlugin plugin) {
        this.plugin = plugin;
        RPGCorePlugin core = RPGCorePlugin.getInstance();
        this.calendar = core != null ? core.getService(CalendarServiceV2.class) : null;
    }

    void registerAll() {
        register("date", new DateCommand());
        register("time", new TimeCommand());
        register("season", new SeasonCommand());
        register("eoc", new EocCommand());
        register("datejoined", new DateJoinedCommand());
    }

    private void register(String name, CommandExecutor exec) {
        var cmd = plugin.getCommand(name);
        if (cmd == null) {
            plugin.getLogger().warning("[calendar] Command /" + name + " is not declared in plugin.yml");
            return;
        }
        cmd.setExecutor(exec);
    }

    private boolean ensureCalendar(CommandSender sender) {
        if (calendar == null) {
            sender.sendMessage(ChatColor.RED + "Calendar service is not available.");
            return false;
        }
        return true;
    }

    private final class DateCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!ensureCalendar(sender)) return true;
            CalendarServiceV2.CalendarSnapshot snap = calendar.getCurrentSnapshot();
            LocalDate d = snap.date();
            sender.sendMessage(ChatColor.GOLD + "Date: " + ChatColor.YELLOW + d.getDayOfMonth() + "/" + d.getMonthValue() + "/" + d.getYear());
            return true;
        }
    }

    private final class TimeCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!ensureCalendar(sender)) return true;
            long mcDay = calendar.getCurrentSnapshot().mcDay();
            long ticks = (mcDay * 24000L) % 24000L;
            int hours = (int) ((ticks / 1000 + 6) % 24);
            int minutes = (int) ((ticks % 1000) * 60 / 1000);
            sender.sendMessage(ChatColor.GOLD + "Time: " + ChatColor.YELLOW +
                String.format("%02d:%02d", hours, minutes) + " (MC day " + mcDay + ")");
            return true;
        }
    }

    private final class SeasonCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!ensureCalendar(sender)) return true;
            if (args.length > 0 && "guide".equalsIgnoreCase(args[0])) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can open the seasonal guide.");
                    return true;
                }
                player.openBook(SeasonGuideBook.build(plugin));
                return true;
            }
            if (sender instanceof Player pl) {
                RPGCorePlugin core = RPGCorePlugin.getInstance();
                EquatorSettings eq = core != null ? core.getService(EquatorSettings.class) : null;
                if (eq == null && core != null) {
                    eq = EquatorSettings.loadOrDisabled(core);
                }
                if (eq != null && EquatorZone.isInBand(pl, eq)) {
                    sender.sendMessage(ChatColor.GOLD + "Zone: " + ChatColor.YELLOW + "Equator belt (buffer near Z=0)");
                    sender.sendMessage(ChatColor.GRAY + "No hemispheric season here. World calendar: "
                            + ChatColor.WHITE + calendar.getCurrentSnapshot().season());
                    return true;
                }
            }
            CalendarServiceV2.Season season = calendar.getCurrentSnapshot().season();
            sender.sendMessage(ChatColor.GOLD + "Season: " + ChatColor.YELLOW + season.name());
            return true;
        }
    }

    private final class EocCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!ensureCalendar(sender)) return true;
            long dayCount = calendar.getCurrentSnapshot().dayCount();
            sender.sendMessage(ChatColor.GOLD + "Days since Epoch: " + ChatColor.YELLOW + dayCount);
            return true;
        }
    }

    private final class DateJoinedCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }
            if (!ensureCalendar(sender)) return true;
            LocalDate joinDate = calendar.getPlayerJoinDate(player.getUniqueId());
            sender.sendMessage(ChatColor.GOLD + "You first joined on: " + ChatColor.YELLOW +
                joinDate.getDayOfMonth() + "/" + joinDate.getMonthValue() + "/" + joinDate.getYear());
            return true;
        }
    }
}

