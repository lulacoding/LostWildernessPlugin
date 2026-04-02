package com.lostwilderness.rpgcore.events;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.calendar.EquatorSettings;
import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.core.RpgModule;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import com.lostwilderness.rpgcore.events.impl.*;
import com.lostwilderness.rpgcore.world.BiomeBackupStore;
import com.lostwilderness.rpgcore.world.BiomePainter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EventsModule implements RpgModule {

    private ModuleContext ctx;
    private EventServiceImpl service;
    private ParanoiaEvent paranoiaEvent;
    private SeasonalWeatherListener seasonalWeatherListener;
    private EquatorWeatherListener equatorWeatherListener;
    private WildlifeMigrationListener wildlifeMigrationListener;
    private SeasonalCropsListener seasonalCropsListener;
    private LWConfigs lwConfigs;
    private BloodMoonEvent bloodMoonEvent;
    private MagicStormEvent magicStormEvent;
    private FishingFestivalEvent fishingFestivalEvent;
    private MiningBlessingEvent miningBlessingEvent;
    private RestfulSleepEvent restfulSleepEvent;
    private FestivalEvent festivalEvent;
    private BiomeBackupStore biomeBackupStore;
    private BiomePainter biomePainter;

    @Override
    public String getName() {
        return "events";
    }

    @Override
    public List<String> getDependencies() {
        return Collections.singletonList("calendar");
    }

    @Override
    public void onLoad(ModuleContext ctx) {
        this.ctx = ctx;
        CalendarServiceV2 calendar = ctx.getServiceRegistry().get(CalendarServiceV2.class);
        if (calendar == null) {
            ctx.getPlugin().getLogger().warning("[events] Skipped: calendar module not loaded.");
            return;
        }
        EventContext context = new EventContext(ctx.getPlugin(), calendar);
        FileConfiguration cfg = ctx.getPlugin().getConfig();
        if (cfg.contains("events.disabled_worlds")) {
            context.setDisabledWorlds(cfg.getStringList("events.disabled_worlds"));
        }
        if (cfg.contains("events.world-day-offsets") && cfg.isConfigurationSection("events.world-day-offsets")) {
            java.util.Map<String, Integer> offsets = new java.util.HashMap<>();
            for (String worldName : cfg.getConfigurationSection("events.world-day-offsets").getKeys(false)) {
                offsets.put(worldName.toLowerCase(java.util.Locale.ROOT), cfg.getInt("events.world-day-offsets." + worldName, 0));
            }
            context.setWorldDayOffsets(offsets);
        }

        List<SeasonalEvent> seasonalEvents = new ArrayList<>();
        seasonalEvents.add(new TestEvent(ctx.getPlugin()));

        lwConfigs = new LWConfigs(ctx.getPlugin());
        lwConfigs.loadAll();

        EquatorSettings equatorSettings = lwConfigs.getEquatorSettings();
        ctx.getServiceRegistry().register(EquatorSettings.class, equatorSettings);

        BlockRestoreManager blockRestore = new BlockRestoreManager(ctx.getPlugin());
        EclipseEvent eclipseEvent = new EclipseEvent(ctx.getPlugin());
        ThunderEvent thunderEvent = new ThunderEvent(ctx.getPlugin());
        FogEvent fogEvent = new FogEvent(ctx.getPlugin());
        BlizzardEvent blizzardEvent = new BlizzardEvent(ctx.getPlugin(), blockRestore, equatorSettings);
        FrostEvent frostEvent = new FrostEvent(ctx.getPlugin(), calendar, blockRestore, equatorSettings);
        SummerHeatwaveEvent heatwaveEvent = new SummerHeatwaveEvent(ctx.getPlugin(), calendar, lwConfigs);
        JungleMonsoonEvent monsoonEvent = new JungleMonsoonEvent(ctx.getPlugin(), calendar);
        SeasonalStormEvent stormEvent = new SeasonalStormEvent(ctx.getPlugin(), calendar);
        SpringBloomEvent springEvent = new SpringBloomEvent(ctx.getPlugin(), calendar);
        AutumnLeafFallEvent autumnEvent = new AutumnLeafFallEvent(ctx.getPlugin(), calendar);
        paranoiaEvent = new ParanoiaEvent(ctx.getPlugin(), eclipseEvent);

        BloodMoonEvent bloodMoonEvent = new BloodMoonEvent(ctx.getPlugin(), lwConfigs);
        TornadoEvent tornadoEvent = new TornadoEvent(ctx.getPlugin(), lwConfigs);
        MagicStormEvent magicStormEvent = new MagicStormEvent(ctx.getPlugin(), calendar, lwConfigs);
        this.magicStormEvent = magicStormEvent;
        festivalEvent = new FestivalEvent(ctx.getPlugin(), lwConfigs);
        FishingFestivalEvent fishingFestivalEvent = new FishingFestivalEvent(ctx.getPlugin(), lwConfigs);
        MiningBlessingEvent miningBlessingEvent = new MiningBlessingEvent(ctx.getPlugin(), lwConfigs);
        RestfulSleepEvent restfulSleepEvent = new RestfulSleepEvent(ctx.getPlugin(), lwConfigs);

        List<DailyWorldEvent> dailyEvents = new ArrayList<>();
        dailyEvents.add(thunderEvent);
        dailyEvents.add(eclipseEvent);
        dailyEvents.add(blizzardEvent);
        dailyEvents.add(frostEvent);
        dailyEvents.add(heatwaveEvent);
        dailyEvents.add(monsoonEvent);
        dailyEvents.add(stormEvent);
        dailyEvents.add(springEvent);
        dailyEvents.add(autumnEvent);
        dailyEvents.add(fogEvent);
        dailyEvents.add(paranoiaEvent);
        dailyEvents.add(bloodMoonEvent);
        dailyEvents.add(tornadoEvent);
        dailyEvents.add(magicStormEvent);
        dailyEvents.add(festivalEvent);
        dailyEvents.add(fishingFestivalEvent);
        dailyEvents.add(miningBlessingEvent);
        dailyEvents.add(restfulSleepEvent);
        NewYearEvent newYearEvent = new NewYearEvent(ctx.getPlugin(), calendar);
        dailyEvents.add(newYearEvent);
        HolidayEvent holidayEvent = new HolidayEvent(ctx.getPlugin(), calendar);
        dailyEvents.add(holidayEvent);

        EventCooldownTracker cooldownTracker = new EventCooldownTracker();
        EventCascadeRegistry cascadeRegistry = new EventCascadeRegistry();
        int eclipseChancePct = Math.max(0, Math.min(100, cfg.getInt("events.thunder.eclipse-chance-pct", 40)));
        cascadeRegistry.register(eclipseEvent, (day, world) -> thunderEvent.triggerEclipseStorm(day, world),
                eclipseChancePct / 100.0);
        double paranoiaChance = cfg.getDouble("events.paranoia.chance", 1.0);
        cascadeRegistry.register(eclipseEvent, paranoiaEvent, Math.max(0.0, Math.min(1.0, paranoiaChance)));

        EventBossBarManager bossBarManager = new EventBossBarManager(ctx.getPlugin());
        double tpsThreshold = cfg.getDouble("events.pause-if-tps-below", 15.0);

        service = new EventServiceImpl(ctx.getPlugin(), calendar, context, ctx.getScheduler(),
                seasonalEvents, dailyEvents, cooldownTracker, cascadeRegistry, bossBarManager, tpsThreshold);
        ctx.getServiceRegistry().register(EventService.class, service);
        this.bloodMoonEvent = bloodMoonEvent;
        this.fishingFestivalEvent = fishingFestivalEvent;
        this.miningBlessingEvent = miningBlessingEvent;
        this.restfulSleepEvent = restfulSleepEvent;
        if (cfg.getBoolean("events.seasonal-weather.enabled", true)) {
            seasonalWeatherListener = new SeasonalWeatherListener(ctx.getPlugin(), calendar, lwConfigs);
        }
        if (cfg.getBoolean("events.wildlife-migration.enabled", false)
                || lwConfigs.getFauna().getBoolean("migration.enabled", false)) {
            wildlifeMigrationListener = new WildlifeMigrationListener(ctx.getPlugin(), calendar, lwConfigs);
        }
        if (lwConfigs.getCrops().getBoolean("seasonal_crops.enabled", false)) {
            seasonalCropsListener = new SeasonalCropsListener(ctx.getPlugin(), calendar, lwConfigs);
        }
        biomeBackupStore = new BiomeBackupStore(ctx.getPlugin());
        biomeBackupStore.setDiskBackupEnabled(cfg.getBoolean("events.biome-painting.disk-backup-enabled", true));
        biomeBackupStore.setPaintingEnabled(cfg.getBoolean("events.biome-painting.painting-enabled", false));
        ctx.getServiceRegistry().register(BiomeBackupStore.class, biomeBackupStore);
        biomePainter = new BiomePainter(ctx.getPlugin(), calendar, biomeBackupStore);
        ctx.getPlugin().getLogger().info("[events] Loaded with " + seasonalEvents.size() + " seasonal, "
                + dailyEvents.size() + " daily event(s).");
    }

    @Override
    public void onEnable() {
        if (service != null) {
            service.register();
            if (paranoiaEvent != null) {
                ctx.getPlugin().getServer().getPluginManager().registerEvents(paranoiaEvent, ctx.getPlugin());
            }
            if (seasonalWeatherListener != null) {
                ctx.getPlugin().getServer().getPluginManager().registerEvents(seasonalWeatherListener, ctx.getPlugin());
            }
            EquatorSettings eq = lwConfigs.getEquatorSettings();
            if (eq.enabled()) {
                equatorWeatherListener = new EquatorWeatherListener(ctx.getPlugin(), eq);
                ctx.getPlugin().getServer().getPluginManager().registerEvents(equatorWeatherListener, ctx.getPlugin());
            }
            if (wildlifeMigrationListener != null) {
                ctx.getPlugin().getServer().getPluginManager().registerEvents(wildlifeMigrationListener,
                        ctx.getPlugin());
            }
            if (seasonalCropsListener != null) {
                ctx.getPlugin().getServer().getPluginManager().registerEvents(seasonalCropsListener, ctx.getPlugin());
            }
            if (bloodMoonEvent != null) ctx.getPlugin().getServer().getPluginManager().registerEvents(bloodMoonEvent, ctx.getPlugin());
            if (magicStormEvent != null) ctx.getPlugin().getServer().getPluginManager().registerEvents(magicStormEvent, ctx.getPlugin());
            if (fishingFestivalEvent != null) ctx.getPlugin().getServer().getPluginManager().registerEvents(fishingFestivalEvent, ctx.getPlugin());
            if (miningBlessingEvent != null) ctx.getPlugin().getServer().getPluginManager().registerEvents(miningBlessingEvent, ctx.getPlugin());
            if (restfulSleepEvent != null) ctx.getPlugin().getServer().getPluginManager().registerEvents(restfulSleepEvent, ctx.getPlugin());
            if (festivalEvent != null) ctx.getPlugin().getServer().getPluginManager().registerEvents(festivalEvent, ctx.getPlugin());
            if (biomeBackupStore != null) ctx.getPlugin().getServer().getPluginManager().registerEvents(biomeBackupStore, ctx.getPlugin());
            if (biomePainter != null) biomePainter.register();
        }
    }

    @Override
    public void onDisable() {
        if (service != null) {
            service.unregister();
            service = null;
        }
        if (paranoiaEvent != null) {
            org.bukkit.event.HandlerList.unregisterAll(paranoiaEvent);
            paranoiaEvent = null;
        }
        if (seasonalWeatherListener != null) {
            org.bukkit.event.HandlerList.unregisterAll(seasonalWeatherListener);
            seasonalWeatherListener = null;
        }
        if (equatorWeatherListener != null) {
            equatorWeatherListener.cancel();
            org.bukkit.event.HandlerList.unregisterAll(equatorWeatherListener);
            equatorWeatherListener = null;
        }
        if (wildlifeMigrationListener != null) {
            org.bukkit.event.HandlerList.unregisterAll(wildlifeMigrationListener);
            wildlifeMigrationListener = null;
        }
        if (seasonalCropsListener != null) {
            org.bukkit.event.HandlerList.unregisterAll(seasonalCropsListener);
            seasonalCropsListener = null;
        }
        if (bloodMoonEvent != null) { org.bukkit.event.HandlerList.unregisterAll(bloodMoonEvent); bloodMoonEvent = null; }
        if (magicStormEvent != null) { org.bukkit.event.HandlerList.unregisterAll(magicStormEvent); magicStormEvent = null; }
        if (fishingFestivalEvent != null) { org.bukkit.event.HandlerList.unregisterAll(fishingFestivalEvent); fishingFestivalEvent = null; }
        if (miningBlessingEvent != null) { org.bukkit.event.HandlerList.unregisterAll(miningBlessingEvent); miningBlessingEvent = null; }
        if (restfulSleepEvent != null) { org.bukkit.event.HandlerList.unregisterAll(restfulSleepEvent); restfulSleepEvent = null; }
        if (festivalEvent != null) { org.bukkit.event.HandlerList.unregisterAll(festivalEvent); festivalEvent = null; }
        if (biomePainter != null) {
            biomePainter.unregister();
            biomePainter = null;
        }
        if (biomeBackupStore != null) {
            org.bukkit.event.HandlerList.unregisterAll(biomeBackupStore);
            biomeBackupStore.stopRestore();
            biomeBackupStore = null;
        }
        lwConfigs = null;
        ctx = null;
    }
}
