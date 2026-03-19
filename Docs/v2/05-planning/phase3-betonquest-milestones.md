# BetonQuest integration – quests and milestones

> **Status Update (2026-03-19):** Bridge is **IMPLEMENTED**. BetonQuestBridge fires BQ events on milestone unlock/claim via console commands.
>
> **✅ Implemented:**
> - `BetonQuestBridge.java` — fires `bq event <player> <eventId>` for first_join, join_3_times, join_10_times
> - Hooks into ProgressionJoinListener
> - `/rewards` menu exists and works
>
> **⚠️ Not Verified:**
> - BetonQuest package setup (`milestone_rewards.yml`) on server
> - Journal entries displaying correctly
> - Tag conditions for gating quests

Use **BetonQuest** for quests and for **showing milestones** in the same place (journal, conditions). PluginV2 stays the **source of truth** for when milestones unlock (our DB and join logic); BetonQuest is the **display and quest engine**.

---

## Roles

| System | Role |
|--------|------|
| **PluginV2 progression** | Tracks join count, unlocks `first_join` / `join_3_times` in our DB. Decides *when* a milestone is unlocked. |
| **BetonQuest** | Quests, conversations, objectives, journal (`/journal`). Use it to *show* milestones and gate quest content by tags. |

So: **keep your current progression** (ProgressionModule, `player_achievements`, `progression_counters`, `/v2progress`). Add an optional **bridge** that, when a V2 milestone unlocks, tells BetonQuest to set a tag and optionally add a journal entry. Then players see milestones in the same journal as quests. Survival and Amplified both run `RPG_Core_V2` (and the same BetonQuest package); Lobby does not run V2 or BetonQuest.

---

## How BetonQuest fits (from their docs)

- **Tags** – Set with the `tag` action, checked with the `tag` condition. Used to track “has done X” (e.g. `milestone_first_join`). All tags are bound to a **package** (e.g. `default.milestone_first_join`).
- **Journal** – Book from `/journal` or backpack. Updated with the `journal` action; entries are defined in the package’s `journal` section (with optional translations).
- **Actions** – Things that happen at a moment: e.g. `tag add …`, `journal add …`. You define named **events** that run one or more actions (e.g. `run ^tag add x ^journal add y`).
- **API (Legacy)** – From another plugin you can fire an event for a player:  
  `BetonQuest.event(profile, new EventID(questPackage, "eventID"));`  
  So when we unlock a milestone we fire a BQ event that runs “tag add” + “journal add”.

---

## Recommended setup

### 1. Keep V2 progression as is

- ProgressionModule, first_join, join_3_times, `/v2progress` all stay.
- No need to move “when” milestones unlock into BetonQuest.

### 2. Optional: PluginV2 → BetonQuest bridge

When a milestone unlocks in V2, we can call BetonQuest’s (legacy) API to run an event for that player. That event does:

- `tag add &lt;milestone_tag&gt;` – so BQ conditions can gate quests (“only if milestone_first_join”).
- `journal add &lt;journal_entry_id&gt;` – so the player sees the milestone in `/journal`.

You define in BetonQuest (in a package, e.g. `default` or `milestones`):

- **events** – e.g. `milestone_first_join: "tag add milestone_first_join ^journal add milestone_first_join"`.
- **journal** – e.g. `milestone_first_join: "&aYou joined for the first time!"`.

We only need to fire the right event ID when we unlock; BQ does the rest.

### 3. Use BetonQuest for quests and milestone display

- All real quests: conversations, objectives, rewards in BetonQuest.
- Milestones: either only in BQ journal (via bridge above) or keep `/v2progress` as an extra/debug; main player-facing view = `/journal` with both quests and milestone entries.

### 4. Redeem milestone XP in a BetonQuest menu

You can give players a **menu** (or conversation) in BetonQuest where they **claim** their milestone reward (AuraSkills XP) instead of it being granted automatically on join.

- **Command:** PluginV2 provides `/v2claim <milestone>` and **`/rewards`** (opens the milestone rewards menu). Journal stays separate: use BetonQuest's `/journal` for the quest journal; use `/rewards` for the rewards GUI. Milestones: `first_join` (50 Fighting XP), `join_3_times` (25 Fighting XP), `join_10_times` (100 Fighting XP).
- **BetonQuest:** In a menu or conversation, add an option like “Redeem 50 Fighting XP (First Join)”. When the player clicks it, run the **chat** action so the command runs as the player: `chat v2claim first_join` (or `chat v2claim join_3_times` / `join_10_times`). (The "command" action runs from console; "chat" runs as the player.)
- **Behaviour:** The command checks that the player has the milestone unlocked and has not already claimed. If both are true, it grants the AuraSkills XP and marks the reward as claimed. If they already claimed (or got it auto on join), they see “You already claimed this reward.”
- **Auto-grant vs menu:** If you want **all** rewards to go through the menu, you can disable auto-grant on join later (config or code). For now, XP is still granted automatically on join when the bridge is ready; the same reward can also be “claimed” via `/v2claim` if they didn’t get it (e.g. AuraSkills was down at join). Claiming is tracked so they can’t get double XP.

---

## Step-by-step: BetonQuest menu for milestone redeem

### 1. Package and items (current survival-1 setup)

In `Server/backends/survival-1/plugins/BetonQuest/QuestPackages/default/milestone_rewards.yml` we now have:

- `items` for the menu (rewards book opener, emerald/gold icons, filler).
- `tags` and `journal` entries for:
  - `first_join_unlocked` / `first_join_claimed`
  - `join3_unlocked` / `join3_claimed`
  - `join10_unlocked` / `join10_claimed`
- `conditions`:
  - `can_claim_first_join`, `can_claim_join3`, `can_claim_join10` using those tags.
- `events`:
  - `claim_first_join`, `claim_join_3_times`, `claim_join_10_times` → run `/v2claim` as player + set claimed tags.
  - `milestone_*_unlocked` → add unlocked tag + journal entry.
  - `milestone_*_claimed` → add claimed tag.

The menu (`menus.milestoneRewards`) has three buttons (First Join, Join 3 Times, Join 10 Times) each with a `conditions:` line so a button only shows if the milestone is unlocked and not yet claimed.

### 2. Get the menu opener and test

- Reload BetonQuest: `/bq reload`.
- Give yourself the menu opener: `/bq give default>rewardsMenu` (book named “Rewards”).
- Open the menu with the item or `/rewards`, then click a reward. PluginV2 will reply (e.g. "Claimed 50 fighting XP!") and BetonQuest will:
  - Add a journal entry when the milestone unlocks.
  - Hide the button once the reward is claimed.

---

## Summary

- **Yes, use BetonQuest for quests** and to **show milestones** in the journal.
- **Keep V2 progression** for *when* milestones unlock (and any cross-server / DB logic you need).
- **Optional bridge**: on milestone unlock, fire a BetonQuest event that sets a tag and adds a journal entry so everything appears in `/journal` and you can gate quests with `tag` conditions.

If you want the bridge implemented next, we can add a soft dependency on BetonQuest and, when it’s present, call their API on `first_join` / `join_3_times` unlock with configurable event IDs (e.g. in `config/progression.yml` or `config/betonquest.yml`).
