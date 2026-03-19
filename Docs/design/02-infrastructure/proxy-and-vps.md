# Proxy and VPS

## BungeeCord (internal)

- **BungeeCord** is used to **link the two backend servers**: normal (Survival) and Amplified. Players switch with **/server amplified** and **/server** (back to normal). From chat: *“At the moment you can change between the normal and amplified with /server amplified n vice versa.”*
- **No /server from Lobby:** The setup is such that **Lobby cannot be bypassed**—i.e. /server is disabled or restricted so players must go through the Lobby (accept resource pack, queue, etc.) before joining Survival or Amplified.
- **Nothing in BungeeCord plugins folder** except what the portal plugins need: *“nah no plugins in bungee folder but portals plugin uses bungee messenger to communicate.”* So Calendar and portal JARs live only on **each backend server’s plugins/** folder; BungeeCord only runs the proxy and the messenger used by the portal plugins.
- **Bind localhost:** For connection issues, the fix mentioned was to set **bind localhost to false** in the BungeeCord config (it was true in an uploaded file). *“also in the bungee config, need to edit the bind local host to false as its on true cuz i uploaded the file before i changed that, apparently it's the fix for the issue but it hasnt changed much.”*
- **BungeeGuard** is planned for secure backend access so only the proxy can connect to the backends.

## Velocity (VPS)

- **Velocity** runs on a **VPS** (e.g. **OVH ~$14/mo**) for **DDoS protection** and the **first anticheat check**. Traffic flow: **play.lostwilderness.net** → **VPS (Velocity)** → **home** (where BungeeCord and the game servers run). So the public connects to the VPS; the VPS then forwards to the home network.
- **TCPShield** is mentioned for DDoS.
- **Static IP** for the home connection so the VPS can reliably reach the backends.
- VPS specs mentioned: **4 core, 4GB RAM, 80GB SSD, 1000/1000** (symmetric).

## Flow summary

1. Player connects to **play.lostwilderness.net** (or equivalent).
2. **Velocity** on VPS handles the connection (DDoS, first AC).
3. Connection is forwarded to **BungeeCord** at home.
4. BungeeCord sends the player to **Lobby** (or, after auth, to Survival/Amplified).
5. **Survival** and **Amplified** are the two backend Paper servers; BungeeCord switches between them when the player uses the custom portal (or /server during testing).

## Source

- Direct Messages (Grovyle187): /server amplified, BungeeCord bind localhost false, no plugins in Bungee folder, Bungee Messenger for portals.
- Bible sections 2 and 6: Velocity VPS, play.lostwilderness.net, BungeeGuard, Lobby cannot be bypassed.
