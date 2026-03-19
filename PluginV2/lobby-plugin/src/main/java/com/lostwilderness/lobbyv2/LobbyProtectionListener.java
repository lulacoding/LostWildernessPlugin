package com.lostwilderness.lobbyv2;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyProtectionListener implements Listener {

    private final Plugin plugin;
    private final VerificationRepository verificationRepository;
    private final Location spawnLocation;
    private final boolean greeterIntroEnabled;
    private final String greeterEvent;
    private final int greeterLockTicks;
    private final double greeterTriggerRadius;
    private final Location greeterFocus;
    private final Set<UUID> pendingGreeterIntro = ConcurrentHashMap.newKeySet();

    public LobbyProtectionListener(Plugin plugin,
            VerificationRepository verificationRepository,
            Location spawnLocation,
            boolean greeterIntroEnabled,
            String greeterEvent,
            int greeterLockTicks,
            double greeterTriggerRadius,
            Location greeterFocus) {
        this.plugin = plugin;
        this.verificationRepository = verificationRepository;
        this.spawnLocation = spawnLocation;
        this.greeterIntroEnabled = greeterIntroEnabled;
        this.greeterEvent = greeterEvent;
        this.greeterLockTicks = Math.max(20, greeterLockTicks);
        this.greeterTriggerRadius = Math.max(1.0, greeterTriggerRadius);
        this.greeterFocus = greeterFocus;
    }

    @EventHandler
    public void onInitialSpawn(PlayerSpawnLocationEvent e) {
        if (spawnLocation == null)
            return;
        e.setSpawnLocation(spawnLocation.clone());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (spawnLocation != null) {
            // Keep this as a second safety net in case other plugins mutate spawn after
            // initial spawn event.
            p.teleport(spawnLocation.clone());
        }
        p.setGameMode(GameMode.ADVENTURE);
        p.setHealth(20);
        p.setFoodLevel(20);

        p.sendMessage("§eWelcome to Lost Wilderness!");
        p.sendMessage("§b1. §eRun §f/verify §eto start verification.");
        p.sendMessage("§b2. §eComplete §f/verify <your_name> §ein Discord.");
        p.sendMessage("§b3. §eChoose your class with §f/class §eor the Class Guide NPC.");
        p.sendMessage("§b4. §eStep into the Gateway to enter Survival.");

        if (greeterIntroEnabled) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean seen = verificationRepository.hasSeenGreeterIntro(p.getUniqueId().toString());
                if (!seen) {
                    pendingGreeterIntro.add(p.getUniqueId());
                }
            });
        }
    }

    @EventHandler
    public void onFirstMove(PlayerMoveEvent e) {
        if (e.getTo() == null)
            return;
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!pendingGreeterIntro.contains(uuid))
            return;
        if (e.getFrom().getX() == e.getTo().getX()
                && e.getFrom().getY() == e.getTo().getY()
                && e.getFrom().getZ() == e.getTo().getZ())
            return;
        if (spawnLocation == null || !player.getWorld().equals(spawnLocation.getWorld()))
            return;
        if (e.getTo().distanceSquared(spawnLocation) > (greeterTriggerRadius * greeterTriggerRadius))
            return;

        pendingGreeterIntro.remove(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> verificationRepository.markGreeterIntroSeen(uuid.toString()));
        lockAndStartGreeterIntro(player);
    }

    private void lockAndStartGreeterIntro(Player player) {
        if (!player.isOnline())
            return;

        if (greeterFocus != null && greeterFocus.getWorld() != null
                && player.getWorld().equals(greeterFocus.getWorld())) {
            Location look = player.getLocation().clone();
            look.setDirection(greeterFocus.toVector().subtract(look.toVector()));
            player.teleport(look);
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, greeterLockTicks, 10, false, false, false));
        player.addPotionEffect(
                new PotionEffect(PotionEffectType.JUMP_BOOST, greeterLockTicks, 128, false, false, false));
        player.sendMessage("§eThe greeter catches your attention...");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline())
                return;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "q event " + player.getName() + " " + greeterEvent);
        }, 5L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline())
                return;
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        }, greeterLockTicks);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.getPlayer().hasPermission("lw.admin")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!e.getPlayer().hasPermission("lw.admin")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        e.setCancelled(true);
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().hasPermission("lw.admin")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent e) {
        if (e.toWeatherState()) {
            e.setCancelled(true); // Prevent rain
        }
    }
}
