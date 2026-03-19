package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Magic Storm: storm + thunder; some mobs become "storm" mobs (glowing, buffed); extra loot on kill; PDC restore.
 */
public final class MagicStormEvent implements DailyWorldEvent, Listener {

    private static final String TAG_MAGIC_STORM = "lw_magic_storm";

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private final LWConfigs lwConfigs;
    private final NamespacedKey keyMaxHealth;
    private final NamespacedKey keyAttackDamage;
    private final NamespacedKey keyCustomName;
    private volatile boolean active = false;
    private BukkitRunnable endTask;

    public MagicStormEvent(Plugin plugin, CalendarServiceV2 calendar, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.calendar = calendar;
        this.lwConfigs = lwConfigs;
        this.keyMaxHealth = new NamespacedKey(plugin, "magic_storm_health");
        this.keyAttackDamage = new NamespacedKey(plugin, "magic_storm_damage");
        this.keyCustomName = new NamespacedKey(plugin, "magic_storm_name");
    }

    private boolean isEnabled() {
        return lwConfigs != null && lwConfigs.getEventsExtra() != null
            && lwConfigs.getEventsExtra().getBoolean("magic_storm.enabled", true);
    }

    private double getRareMobChance() {
        return lwConfigs != null && lwConfigs.getEventsExtra() != null
            ? lwConfigs.getEventsExtra().getDouble("magic_storm.rare_mob_chance", 0.35)
            : 0.35;
    }

    private double getExtraLootChance() {
        return lwConfigs != null && lwConfigs.getEventsExtra() != null
            ? lwConfigs.getEventsExtra().getDouble("magic_storm.extra_loot_chance", 0.40)
            : 0.40;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (world == null) return;
        if (!isEnabled()) return;
        double chance = lwConfigs.getEventsExtra().getDouble("magic_storm.base_chance_per_day", 0.08);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;
        CalendarServiceV2.Season s = calendar.getCurrentSnapshot().season();
        if (!lwConfigs.getEventsExtra().getStringList("magic_storm.allowed_seasons").contains(s.name())) return;

        active = true;
        final World worldRef = world;
        worldRef.setStorm(true);
        worldRef.setThundering(true);
        for (Player p : worldRef.getPlayers()) {
            p.sendMessage("§dA magical storm sweeps across the land…");
        }
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                worldRef.setStorm(false);
                worldRef.setThundering(false);
                restoreAllStormMobs();
                active = false;
                endTask = null;
            }
        };
        endTask.runTaskLater(plugin, 24000L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!active) return;
        if (e.getEntity().getWorld().getEnvironment() != World.Environment.NORMAL) return;
        if (!(e.getEntity() instanceof Monster)) return;
        if (ThreadLocalRandom.current().nextDouble() >= getRareMobChance()) return;
        LivingEntity living = (LivingEntity) e.getEntity();
        PersistentDataContainer pdc = living.getPersistentDataContainer();
        double origHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null
            ? living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : 20.0;
        double origDamage = 0;
        if (living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            origDamage = living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        }
        String origName = living.getCustomName() != null ? living.getCustomName() : "";
        pdc.set(keyMaxHealth, PersistentDataType.DOUBLE, origHealth);
        pdc.set(keyAttackDamage, PersistentDataType.DOUBLE, origDamage);
        pdc.set(keyCustomName, PersistentDataType.STRING, origName);
        living.setGlowing(true);
        living.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0));
        living.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 999999, 0));
        if (living.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            living.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(origHealth * 1.25);
            living.setHealth(Math.min(living.getHealth() * 1.25, origHealth * 1.25));
        }
        if (living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(origDamage * 1.25);
        }
        String display = origName.isEmpty() ? living.getType().name().replace("_", " ") : origName;
        living.setCustomName("§dStorm " + display);
        living.setCustomNameVisible(true);
        living.addScoreboardTag(TAG_MAGIC_STORM);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        if (!e.getEntity().getScoreboardTags().contains(TAG_MAGIC_STORM)) return;
        Player killer = e.getEntity().getKiller();
        if (killer == null) return;
        if (ThreadLocalRandom.current().nextDouble() >= getExtraLootChance()) return;
        org.bukkit.Location loc = e.getEntity().getLocation();
        ItemStack extra = createEnchantedLoot();
        if (extra != null && !extra.getType().isAir()) {
            e.getEntity().getWorld().dropItemNaturally(loc, extra);
        }
    }

    private ItemStack createEnchantedLoot() {
        if (ThreadLocalRandom.current().nextBoolean()) {
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            if (meta != null) {
                Enchantment[] enchants = new Enchantment[]{ Enchantment.UNBREAKING, Enchantment.MENDING, Enchantment.FORTUNE, Enchantment.SHARPNESS, Enchantment.PROTECTION };
                Enchantment pick = enchants[ThreadLocalRandom.current().nextInt(enchants.length)];
                meta.addStoredEnchant(pick, 1 + ThreadLocalRandom.current().nextInt(2), true);
                book.setItemMeta(meta);
            }
            return book;
        }
        return new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
    }

    private void restoreStormMob(LivingEntity living) {
        if (!living.getScoreboardTags().contains(TAG_MAGIC_STORM)) return;
        PersistentDataContainer pdc = living.getPersistentDataContainer();
        Double origHealth = pdc.get(keyMaxHealth, PersistentDataType.DOUBLE);
        Double origDamage = pdc.get(keyAttackDamage, PersistentDataType.DOUBLE);
        String origName = pdc.get(keyCustomName, PersistentDataType.STRING);
        living.setGlowing(false);
        living.removePotionEffect(PotionEffectType.SPEED);
        living.removePotionEffect(PotionEffectType.STRENGTH);
        if (origHealth != null && living.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            living.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(origHealth);
            living.setHealth(Math.min(living.getHealth(), origHealth));
        }
        if (origDamage != null && living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(origDamage);
        }
        living.setCustomName(origName != null ? origName : null);
        living.setCustomNameVisible(false);
        living.removeScoreboardTag(TAG_MAGIC_STORM);
        pdc.remove(keyMaxHealth);
        pdc.remove(keyAttackDamage);
        pdc.remove(keyCustomName);
    }

    private void restoreAllStormMobs() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            for (org.bukkit.entity.Entity entity : w.getLivingEntities()) {
                if (entity instanceof LivingEntity && entity.getScoreboardTags().contains(TAG_MAGIC_STORM)) {
                    restoreStormMob((LivingEntity) entity);
                }
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (active) return;
        if (e.getWorld().getEnvironment() != World.Environment.NORMAL) return;
        for (org.bukkit.entity.Entity entity : e.getChunk().getEntities()) {
            if (entity instanceof LivingEntity && entity.getScoreboardTags().contains(TAG_MAGIC_STORM)) {
                restoreStormMob((LivingEntity) entity);
            }
        }
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        restoreAllStormMobs();
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) { w.setStorm(false); w.setThundering(false); }
        }
        if (endTask != null) { endTask.cancel(); endTask = null; }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Magic Storm"; }
}
