package com.lostwilderness.survivalv2;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.calendar.EquatorSettings;
import com.lostwilderness.rpgcore.calendar.EquatorZone;
import com.lostwilderness.rpgcore.calendar.SeasonGuideBook;
import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import com.lostwilderness.rpgcore.personality.CompletionService;
import com.lostwilderness.rpgcore.personality.PlayerTraitProfile;
import com.lostwilderness.rpgcore.personality.TraitService;
import com.lostwilderness.rpgcore.world.BiomeBackupStore;
import com.lostwilderness.rpgcore.zodiac.ZodiacProfile;
import com.lostwilderness.rpgcore.zodiac.ZodiacService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

final class SurvivalCalendarCommands {

    private final JavaPlugin plugin;
    private final CalendarServiceV2 calendar;
    private final ZodiacService zodiac;
    private final TraitService traitService;
    private final CompletionService completionService;

    SurvivalCalendarCommands(JavaPlugin plugin) {
        this.plugin = plugin;
        RPGCorePlugin core = RPGCorePlugin.getInstance();
        this.calendar = core != null ? core.getService(CalendarServiceV2.class) : null;
        this.zodiac = core != null ? core.getService(ZodiacService.class) : null;
        this.traitService = core != null ? core.getService(TraitService.class) : null;
        this.completionService = core != null ? core.getService(CompletionService.class) : null;
    }

    void registerAll() {
        register("date", new DateCommand());
        register("time", new TimeCommand());
        register("season", new SeasonCommand());
        register("eoc", new EocCommand());
        register("datejoined", new DateJoinedCommand());
        register("nextday", new NextDayCommand());
        register("resetcalendar", new ResetCalendarCommand());
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
            int hours = (int) ((ticks / 1000 + 6) % 24); // simple MC-time to clock approximation
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
            if (args.length >= 1 && "biomes".equalsIgnoreCase(args[0])) {
                if (!sender.hasPermission("lw.admin.biomes")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to manage biome painting.");
                    return true;
                }
                RPGCorePlugin core = RPGCorePlugin.getInstance();
                BiomeBackupStore store = core != null ? core.getService(BiomeBackupStore.class) : null;
                if (store == null) {
                    sender.sendMessage(ChatColor.RED + "Biome backup store is not available.");
                    return true;
                }
                if (args.length >= 2 && "backup".equalsIgnoreCase(args[1])) {
                    boolean on = args.length >= 3 && "on".equalsIgnoreCase(args[2]);
                    store.setDiskBackupEnabled(on);
                    sender.sendMessage(ChatColor.GREEN + "Disk backup for biome restore: " + (on ? "on" : "off"));
                    return true;
                }
                if (args.length >= 2) {
                    switch (args[1].toLowerCase()) {
                        case "on" -> {
                            store.setPaintingEnabled(true);
                            sender.sendMessage(ChatColor.GREEN + "Biome painting enabled. Keep a full world backup.");
                            return true;
                        }
                        case "off" -> {
                            store.setPaintingEnabled(false);
                            sender.sendMessage(ChatColor.YELLOW + "Biome painting disabled.");
                            return true;
                        }
                        case "restore" -> {
                            store.startRestoreAll(sender, 6);
                            return true;
                        }
                    }
                }
                sender.sendMessage(ChatColor.GOLD + "Biome painting: " + (store.isPaintingEnabled() ? "on" : "off")
                    + ", disk backup: " + (store.isDiskBackupEnabled() ? "on" : "off")
                    + ", restore: " + (store.isRestoreMode() ? "active" : "idle"));
                sender.sendMessage(ChatColor.GRAY + "Usage: /season biomes on|off|restore|backup on|off");
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

            // Show zodiac info if available
            if (zodiac != null) {
                java.util.Optional<ZodiacProfile> profileOpt = zodiac.getZodiacProfile(player.getUniqueId());
                if (profileOpt.isPresent()) {
                    ZodiacProfile profile = profileOpt.get();
                    sender.sendMessage(ChatColor.GOLD + "Your Zodiac Sign: " + ChatColor.YELLOW +
                        profile.monthSign().displayName() + " " + profile.monthSign().symbol());
                    sender.sendMessage(ChatColor.GOLD + "Your Year Sign: " + ChatColor.YELLOW +
                        profile.yearSign().displayName() + " " + profile.yearSign().symbol());

                    if (profile.isEpochian()) {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "You are one of the Chosen (Epochian).");
                        if (profile.secondSignRevealed()) {
                            sender.sendMessage(ChatColor.GOLD + "Your 2nd Sign: " + ChatColor.YELLOW +
                                profile.secondSign().displayName() + " " + profile.secondSign().symbol());
                        } else {
                            sender.sendMessage(ChatColor.GRAY + "Your 2nd Sign: ??? (Seek the Lord to reveal your destiny)");
                        }
                    }

                    if (profile.spiritAnimalRevealed()) {
                        sender.sendMessage(ChatColor.GOLD + "Your Spirit Animal: " + ChatColor.YELLOW +
                            profile.spiritAnimal().displayName());
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "Your Spirit Animal: Hidden");
                    }
                }
            }

            // Show trait info if available
            if (traitService != null && completionService != null) {
                traitService.getProfile(player.getUniqueId()).thenAccept(profileOpt -> {
                    if (profileOpt.isPresent()) {
                        PlayerTraitProfile traitProfile = profileOpt.get();
                        int completion = completionService.getCompletionPercent(player.getUniqueId());

                        // Format trait tier
                        String tierColor = traitProfile.primaryTier().getColor().toString();
                        String traitDisplay = tierColor + traitProfile.primaryTier().getDisplayName() + " " +
                            traitProfile.primaryTrait().getDisplayName() + " " + traitProfile.primaryTrait().getSymbol();

                        // Format element
                        String elementDisplay;
                        if (!traitProfile.elementRevealed()) {
                            elementDisplay = ChatColor.GRAY + "??? (Not yet revealed)";
                        } else {
                            String elementColor = traitProfile.element().getColor().toString();
                            String status = traitProfile.elementActivated() ? ChatColor.GREEN + "Activated" : ChatColor.GRAY + "Inactive";
                            elementDisplay = elementColor + traitProfile.element().getDisplayName() + " " +
                                traitProfile.element().getSymbol() + " " + ChatColor.GRAY + "(" + status + ChatColor.GRAY + ")";
                        }

                        // Format completion
                        String completionDisplay;
                        if (completion >= 300) {
                            completionDisplay = ChatColor.GOLD + "300% " + ChatColor.GRAY + "(Post-Game)";
                        } else if (completion >= 200) {
                            completionDisplay = ChatColor.GREEN.toString() + completion + "% " + ChatColor.GRAY + "(All Temples)";
                        } else if (completion >= 100) {
                            completionDisplay = ChatColor.YELLOW.toString() + completion + "% " + ChatColor.GRAY + "(Ultimate Tier)";
                        } else if (completion >= 50) {
                            completionDisplay = ChatColor.AQUA.toString() + completion + "% " + ChatColor.GRAY + "(Master Tier)";
                        } else if (completion >= 25) {
                            completionDisplay = ChatColor.WHITE.toString() + completion + "% " + ChatColor.GRAY + "(Trait Tier)";
                        } else {
                            completionDisplay = ChatColor.GRAY.toString() + completion + "% " + ChatColor.GRAY + "(Apprentice)";
                        }

                        sender.sendMessage(ChatColor.GOLD + "Active Trait: " + traitDisplay);
                        sender.sendMessage(ChatColor.GOLD + "Element: " + elementDisplay);
                        sender.sendMessage(ChatColor.GOLD + "Story Completion: " + completionDisplay);

                        if (traitProfile.isPostGame()) {
                            sender.sendMessage(ChatColor.GOLD + "⭐ Post-Game God " + ChatColor.GRAY + "(All 5 elements active)");
                        }
                    }
                }).exceptionally(ex -> {
                    plugin.getLogger().warning("[datejoined] Failed to load trait profile: " + ex.getMessage());
                    return null;
                });
            }

            return true;
        }
    }

    private final class NextDayCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!sender.hasPermission("lw.admin.calendar")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to change the calendar.");
                return true;
            }
            if (!ensureCalendar(sender)) return true;
            calendar.advanceToNextDay();
            CalendarServiceV2.CalendarSnapshot snap = calendar.getCurrentSnapshot();
            LocalDate d = snap.date();
            sender.sendMessage(ChatColor.GREEN + "Advanced to next day: " +
                d.getDayOfMonth() + "/" + d.getMonthValue() + "/" + d.getYear());
            return true;
        }
    }

    private final class ResetCalendarCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!sender.hasPermission("lw.admin.calendar")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to reset the calendar.");
                return true;
            }
            if (!ensureCalendar(sender)) return true;
            calendar.resetCalendar();
            sender.sendMessage(ChatColor.GREEN + "Calendar has been reset.");
            return true;
        }
    }
}

