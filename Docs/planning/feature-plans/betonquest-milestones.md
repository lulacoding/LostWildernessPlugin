---
title: BetonQuest Milestones
description: BetonQuest integration plan for quest bridge and progression tags.
tags:
  - planning
  - quests
  - progression
status: implemented
phase: phase-1
owner: dev
action: none
---
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


## How BetonQuest fits (from their docs)

- **Tags** – Set with the `tag` action, checked with the `tag` condition. Used to track “has done X” (e.g. `milestone_first_join`). All tags are bound to a **package** (e.g. `default.milestone_first_join`).
- **Journal** – Book from `/journal` or backpack. Updated with the `journal` action; entries are defined in the package’s `journal` section (with optional translations).
- **Actions** – Things that happen at a moment: e.g. `tag add …`, `journal add …`. You define named **events** that run one or more actions (e.g. `run ^tag add x ^journal add y`).
- **API (Legacy)** – From another plugin you can fire an event for a player:  
  `BetonQuest.event(profile, new EventID(questPackage, "eventID"));`  
  So when we unlock a milestone we fire a BQ event that runs “tag add” + “journal add”.


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
