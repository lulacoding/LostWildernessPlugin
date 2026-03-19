# Clans package structure

Reference for the Plugin `common` clans package layout.

---

## model/

- `Clan.java`, `Invite.java`, `Alliance.java`, `AllianceInvite.java` — Data classes for clans and alliances.

## repository/

- `ClanRepository.java`, `AllianceRepository.java`, `WarRepository.java` — Database access and persistence.

## command/

- `ClanCommand.java`, `AllianceCommand.java`, `WarCommand.java` — Bukkit command executors.

## manager/

- `ClanManager.java`, `AllianceManager.java` — Business logic and coordination.

## listeners/

- Event listeners (e.g. `ClanDisplayListener.java`).

## integrations/

- Integration classes (e.g. `BotApiClient.java`).

---

**Conventions:** One class per file; add subfolders as needed for new features.
