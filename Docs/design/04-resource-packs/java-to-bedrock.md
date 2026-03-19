# Java to Bedrock (resource packs)

## Goal

Get the **custom Java resource pack** (custom birds, auroras, particles, sounds, etc.) working **on Bedrock via Geyser**. That requires **converting** the pack to a Bedrock-compatible format and **serving** it through Geyser and Floodgate.

## Key differences (from Graphical Design channel)

| Feature | Java resource pack | Bedrock |
|--------|---------------------|--------|
| Models | JSON (Blockbench) | GeoJSON or `.geo` (entity/item) |
| Sounds | `sounds.json` + `.ogg` | `sounds.json` + `.ogg` (compatible) |
| Item predicates | CustomModelData, NBT | **NOT supported** |
| Entity variants | Via NBT, tags, name | Usually behavior packs/plugins |
| Pack format | `pack.mcmeta` | `manifest.json` |

## GeyserPackConverter

- **GitHub:** https://github.com/GeyserMC/PackConverter  
- **Run:** `python3 pack_converter.py /path/to/java-pack`  
- **Converts:** Textures, sounds, language files, simple models.  
- **Limitations:** CustomModelData on items (e.g. different birds on same entity) doesn’t work natively on Bedrock; **no OptiFine**; complex model swaps via predicates/NBT don’t carry over.

## Manual port (entities)

- For **custom parrot/bird models**: export from **Blockbench** as **Bedrock Entity Format**; add **entity.geo.json** and texture to a Bedrock resource pack; adjust **animation_controllers** and **entity.json** as needed.

## Cross-platform behaviour (from lw-resource-pack)

- **Works on both:** Core gameplay (server-side logic), custom AI (plugins), redstone from custom slabs if server-side, tornadoes/wind/flooding, environmental effects, block height changes (tides), thirst/tide/volcano systems (with Bedrock UI adaptation).
- **Thirst bar:** Java = full HUD via pack; Bedrock = replicate with **ui/_global_variables.json** and **ui/_global_resources.json**; test on real Bedrock devices.
- **Custom sounds:** Bedrock **sounds.json** + `.ogg`; test with **/playsound**.

## Source

- Lost Wilderness - Graphical Design - lw-resource-pack (Grovyle187): full table, GeyserPackConverter, Bedrock UI, Thirst, cross-platform summary.
- Bible section 4.
