# Lost Wilderness – Proxy (BungeeCord)

The proxy lets players connect to one address (e.g. `localhost:25565`) and be sent to **survival** or **amplified** without changing the server in the client.

## 1. Install the proxy JAR (first time only)

**Option A – Script (recommended)**  
Double‑click **`install-proxy.bat`**. It will download BungeeCord and save it as `bungeecord.jar` in this folder.

**Option B – Manual**  
1. Open: https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/  
2. Download **BungeeCord.jar**  
3. Put it in this folder (`Server\proxy\`) and rename it to **`bungeecord.jar`**

## 2. Start order

1. Start **survival**: run `Server\servers\survival\start.bat` (port 25566)  
2. Start **amplified**: run `Server\servers\amplified\start.bat` (port 25567)  
3. Start **proxy**: run `Server\proxy\start.bat` (port 25565)

## 3. How to connect

- Players must connect to the **proxy** port: **25565** (or your public IP and that port).  
- Do **not** connect directly to 25566 (survival) or 25567 (amplified), or you will get “IP forwarding” / connection errors.

## 4. Config

- **`config.yml`** – Already set for survival (25566) and amplified (25567), with **`ip_forward: true`**.  
- After any change to `config.yml`, **restart the proxy** (close the window and run `start.bat` again).

## 5. If you see “enable it in your BungeeCord config”

- Make sure **`ip_forward: true`** is in `config.yml` (it already is).  
- Restart the proxy after editing the config.  
- Connect to the proxy (25565), not to survival (25566) or amplified (25567).
