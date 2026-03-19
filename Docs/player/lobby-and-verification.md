# Lobby and verification

When you connect to Lost Wilderness, you join the **lobby** first. To reach **Survival**, you must verify your account with Discord, then use the lobby teleporter.

---

## Join flow

1. You connect to the server (proxy sends you to the **lobby**).
2. In the lobby you can run **/verify** and use the **teleporter** to Survival — but the teleporter only works after you are **verified**.
3. To verify: run **/verify** in-game, then run **/verify &lt;your_minecraft_username&gt;** in our Discord server. Your Discord nickname will be set to your in-game name (IGN).
4. After verification, step on the **teleporter** (End Gateway block) in the lobby to go to **Survival**.

---

## Commands (Lobby)

| Command | Description |
| --- | --- |
| **/verify** | Start verification. Creates a pending link; you then go to Discord and run **/verify &lt;your_mc_name&gt;** to complete. If you are already verified, the command tells you to use the teleporter. |

---

## Verification steps (player)

1. **In-game (lobby):** Run **/verify**.
   - You’ll see: *"Join our Discord server and run: /verify &lt;YourName&gt;"*
2. **In Discord:** Join the server’s Discord (if you haven’t already), then run:
   - **/verify YourMinecraftUsername**
   - Use your exact Minecraft username (case doesn’t matter).
3. The bot will link your Discord to that Minecraft account and set your Discord **server nickname** to your IGN.
4. **Back in the lobby:** Walk onto the **teleporter** (End Gateway). You’ll be sent to **Survival**.

If you’re not verified and step on the teleporter, you’ll get a message to run **/verify** and complete the steps in Discord.

---

## Tips

- You only need to verify once; after that you can use the teleporter whenever you’re in the lobby.
- The lobby is a simple “selector” area; Survival and Amplified are the main gameplay servers. You can still use **/server survival** or **/server amplified** from the proxy if you have permission.
- If **/verify** in Discord says *"No pending verification"*, run **/verify** in-game first, then run **/verify &lt;name&gt;** in Discord.
- In Discord, you can run **/players** to see how many players are online in total and on each backend (Lobby, Survival, Amplified).
