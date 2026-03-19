# Plugin placement

- **Calendar** and **portal** JARs go in **each backend server’s plugins/ folder only** (Survival and Amplified each have their own SurvivalCalendarPlugin/AmplifiedCalendarPlugin and SurvivalPortalPlugin/AmplifiedPortalPlugin).
- **Nothing** in **BungeeCord plugins/** except what the **portal plugins** need: they use **Bungee Messenger** to communicate between the two backends. So BungeeCord does **not** run the Calendar or Portal plugin JARs.
- From chat: *“Could you put the whole test server onto Dropbox... amp files in amp plugin etc survival in survival”* and *“nah no plugins in bungee folder but portals plugin uses bungee mesenger to communicate.”*

## Source

- Direct Messages (Grovyle187); Bible section 6; 02-infrastructure proxy-and-vps, 03-plugins calendar and portals.
