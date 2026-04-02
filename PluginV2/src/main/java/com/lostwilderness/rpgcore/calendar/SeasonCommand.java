package com.lostwilderness.rpgcore.calendar;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.time.format.DateTimeFormatter;

/**
 * /season [guide|info]
 *
 *   /season        → opens the Season Guide GUI
 *   /season info   → opens the Season Guide GUI
 *   /season guide  → gives the written Season Guide Book item
 */
public final class SeasonCommand implements CommandExecutor {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d");

    private final CalendarServiceV2 calendarService;
    private final SeasonGuideMenu menu;
    private final Plugin plugin;
    private final EquatorSettings equatorSettings;

    public SeasonCommand(CalendarServiceV2 calendarService, SeasonGuideMenu menu, Plugin plugin,
                          EquatorSettings equatorSettings) {
        this.calendarService = calendarService;
        this.menu = menu;
        this.plugin = plugin;
        this.equatorSettings = equatorSettings != null ? equatorSettings : EquatorSettings.disabled();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        String sub = args.length > 0 ? args[0].toLowerCase() : "info";

        switch (sub) {
            case "guide" -> {
                ItemStack book = SeasonGuideBook.build(plugin);
                player.getInventory().addItem(book);
                player.sendMessage(ChatColor.GREEN + "You received the Seasonal Guide!");
            }
            case "info", "default" -> {
                if (EquatorZone.isInBand(player, equatorSettings)) {
                    player.sendMessage(ChatColor.GREEN + "Equator belt: " + ChatColor.GRAY + "mild buffer near Z=0 - no hemispheric season. "
                            + ChatColor.DARK_GRAY + "World is " + ChatColor.WHITE + calendarService.getCurrentSnapshot().season()
                            + ChatColor.DARK_GRAY + ".");
                }
                menu.open(player);
            }
            default -> {
                player.sendMessage(ChatColor.RED + "Usage: /season [guide|info]");
            }
        }
        return true;
    }
}
