# Serving packs (Geyser / Floodgate)

## Geyser config

1. Upload the **converted Bedrock pack** (ZIP) to a **public URL** (Dropbox, CDN, etc.).
2. In **Geyser config.yml** under **bedrock.resource-packs**:
   - **pack-id:** UUID (e.g. from uuidgenerator.net)
   - **pack-name:** e.g. "CustomBirds" / "Lost Wilderness"
   - **pack-version:** e.g. "1.0.0"
   - **link:** URL to the .zip

## Floodgate

- **Floodgate** lets Bedrock players join **without** a custom client; the pack is **downloaded automatically** when they connect.
- **force-resource-packs** in **Paper server.properties** must be **false** for Bedrock compatibility so decliners aren’t disconnected when the pack fails.
- **DefaultPackListener** (and PackServer **/packs/*.zip**); **Floodgate bypass** if Bedrock pack fails so they can still play.

## Lost Wilderness pack policy

- **Forced** Lost Wilderness pack for Java (kick if declined); **lobby pack** first, then **LW pack** on accept; **xray packs** get **kicked**.

## Source

- lw-resource-pack channel; Bible section 4; Plugin DefaultPackListener.
