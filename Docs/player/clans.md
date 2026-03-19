# Clans

## Overview

**Clans** let you form groups with a name and a color. Leaders can invite members, set the clan color, promote or demote members, and declare other clans as enemies or opposition. Your clan name/color can be shown in-game (e.g. in tab or chat) depending on server setup.

---

## Commands

| Command | Description |
| --- | --- |
| **/clan create &lt;name&gt; &lt;#color&gt;** | Create a clan. Name can be several words; color must be hex (e.g. #FFAA00). You must not already be in a clan. |
| **/clan invite &lt;player&gt;** | Invite a player to your clan (leader only). They must be online. |
| **/clan accept** | Accept your most recent clan invite. |
| **/clan deny** | Deny your most recent clan invite. |
| **/clan leave** | Leave your current clan. If you were the last member, the clan is removed. |
| **/clan color &lt;#color&gt;** | Change your clan's color (leader only). Use hex e.g. #RRGGBB. |
| **/clan promote &lt;player&gt;** | Promote a member to leader (leader only). |
| **/clan demote &lt;player&gt;** | Demote a leader to member (leader only). |
| **/clan enemy &lt;clan name&gt;** | Declare another clan as an enemy (leader only). |
| **/clan opposition &lt;clan name&gt;** | Declare another clan as opposition (leader only). |
| **/clan info** | Show your clan's info and members. |
| **/clan info &lt;name&gt;** | Show another clan's info by name. |
| **/clan list** | List clans (if implemented). |

---

## Alliances

- **/alliance** has subcommands such as **create**, **invite**, **join**, **leave**, **info**, **list**. Some may show "not yet implemented" – check in-game or with staff.

---

## War

- Leaders can **/war declare &lt;clan&gt;** to declare war, **/war ceasefire &lt;clan&gt;** for a ceasefire, and **/war end &lt;clan&gt;** to end the war. **/war status** lists your clan's wars. When war is declared or ended, the server may post to Discord if configured.
- **Player head drops:** If your clan is at war with another and you kill an enemy (or are killed), there is a small chance the victim's **player head** drops as an item (1 in 1000 in war, 1 in 10000 otherwise).

---

## Typical flow

1. **Create a clan:** `/clan create My Clan #FFAA00`
2. **Invite players:** `/clan invite PlayerName` (they get a message to use `/clan accept` or `/clan deny`).
3. **Manage:** Use **/clan color**, **/clan promote**, **/clan demote** as leader; **/clan leave** to leave.
4. **Relations:** Use **/clan enemy** or **/clan opposition** to set relations with other clans (leader only).

---

## Tips

- Clan names must be 1–32 characters. Color must be a valid hex code (e.g. #FF0000 for red).
- Only one pending invite is used at a time; **/clan accept** or **/clan deny** applies to the most recent.
- If the server uses Discord integration, creating a clan or updating roles may sync to Discord (e.g. roles); that's configured by the server.
