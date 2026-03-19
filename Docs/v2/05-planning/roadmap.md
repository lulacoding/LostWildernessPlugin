# Roadmap - V2 Build Phases

> **Project-Level Roadmap:** For overall project priorities (operations, scaling, features), see the main [Docs/roadmap.md](../../roadmap.md)

Phased plan for building PluginV2. No fixed dates; adjust to your pace.

---

## Phase 0 – Orientation ✅

- Re-read [V2_ARCHITECTURE_PLAN.md](../../V2_ARCHITECTURE_PLAN.md) and PluginV2 docs.
- Decide: build tool (Gradle/Maven), Java version (e.g. 17 or 21).
- Document which servers you run and what “playable baseline” means (e.g. survival + calendar + portals).
- Define environments: dev (local), stage (test network), prod (when applicable).

---

## Phase 1 – Core & player spine (1–2 weeks) ✅

**Goal:** One jar loads, has infra + player profiles, no gameplay yet.

1. ✅ Create PluginV2 module (Gradle/Maven), `paper-plugin.yml`, main class only.
2. ✅ Implement infra: ConfigService, DatabaseProvider, SchedulerService, stub ClusterMessagingService.
3. ✅ Implement module system: RpgModule, ModuleManager, ModuleContext.
4. ✅ Implement PlayerModule: PlayerProfile (minimal: UUID + lastSeen), PlayerProfileService, PlayerProfileRepository, listeners (pre-login, join, quit).
5. **Test:** dev server with only PluginV2 in `plugins/`; join/leave; profile load/save in logs; no TPS impact.

**Outcome:** Stable base to extend; no RPG features yet.

---

## Phase 2 – First gameplay slice (1–2 weeks) ✅

**Goal:** One visible feature end-to-end in V2.

- ✅ Progression V2 – ProgressionService, ProgressionRepository, milestones (first_join, join_3_times), `/v2progress` command.

**Outcome:** Proof that core + one domain works; players see something new.

---

## Phase 3 – Core RPG systems (4–8+ weeks, iterative)

Add one vertical at a time; game stays playable after each step. **Full remake:** every feature from the current plugin and Docs must be reimplemented—see [v2-feature-parity.md](../04-features/v2-feature-parity.md). Target: **three servers (Lobby, Survival, Amplified)** with a shared core (`RPG_Core_V2`) and thin per-backend wrappers (Lobby/LW-Survival-V2/LW-Amplified-V2).

1. **Skills (Phase 3.1)** ✅ – AuraSkills integration: optional module registers `AuraSkillsBridge` (addSkillXp, getSkillLevel); progression grants optional Fighting XP on first_join (50) and join_3_times (25). See [phase3-skills-auraskills.md](phase3-skills-auraskills.md).
2. **Economy** ❌ – WalletService, WalletRepository, TransactionLogRepository; `/balance`, `/pay`; optional small shop.
3. **Calendar + events** ✅ – CalendarService (leader/follower); EventEngine with all current events (Eclipse, Fog, Blizzard, Frost, Heatwave, Thunder, Paranoia, cascades, etc.). **Aeternum-style expansion:** ✅ Biome painting + backup, Blood Moon, Tornado, Magic Storm, Festivals, seasonal crops, wildlife migration, seasonal weather. See [plan-aeternum-features-in-lw.md](plan-aeternum-features-in-lw.md). **Missing:** `/season guide` command.
4. **Portals** ✅ – PortalService, PortalEngine (frame + DB + transfer, entity passthrough, cross-server); same behaviour as current.
5. **Clans / war** ✅ – ClanService, AllianceService, WarService; full commands, head drops, Discord webhook, etc.
6. **Party** ✅ – PartyModule: PartyService, cross-server messaging, display/friendly-fire/buff listeners, `/party` command.
7. **Classes** ✅ – ClassesModule: 5 classes, abilities, passives, skill tree (7 levels), mastery XP, Ultimate items. See [plan-class-skill-tree.md](plan-class-skill-tree.md).
8. **Zodiac** ✅ – ZodiacModule: 13 signs, 14 spirit animals, Good/Evil variants, sync bonus, Epochian status, clan leader bonus.
9. **Personality** 🚧 – PersonalityModule: TraitService, 13 traits, 5 elements, Holy Enchants. **Missing:** Quiz system, TraitEffectListener, `/trait` command. See [plan-personality-elemental-quest.md](plan-personality-elemental-quest.md).
10. **Lobby** ❌ – Verification, teleporter to Survival/Amplified, linked accounts (same as current).
11. **Bosses / progression** ❌ – Milestones, RoofWither/Devoider/El Diablo, arena, 200% completion, disc reward.
12. **Quests / storyline** ❌ – QuestService, definitions, chapters, gates (per Docs).
13. **Nether Events** ❌ – NetherFishingDerby, PiglinMarket, QuartzRush, FungusBloom, BlazeSurge, MagmaTides, GhastAlert, WitherLoose, WitherSkeletonSwarm. See [phase3-events.md](phase3-events.md).

**Outcome:** Full feature parity with current plugin + Docs; legacy plugins can be retired when each domain is stable.

---

## Phase 4 – Unification & cleanup (ongoing)

- Once a domain is stable in V2: disable that feature in the old plugin, then remove the old JAR.
- Keep [implementation-status.md](../06-operations/implementation-status.md) updated: V1 vs V2 (dev/stage/prod), V1 removed.
