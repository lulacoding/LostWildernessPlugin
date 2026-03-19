# Centralized Configuration System

## Overview

The LostWildernessPlugin now uses a centralized configuration system where all plugin modules share a common configuration structure. This means:

1. All configs are stored in one location
2. Modules can share common settings (like database credentials)
3. Admins can manage settings through a common interface
4. Plugin modules automatically depend on the Common plugin

## Structure

The configuration files are now organized under the `/plugins/LostWilderness-Common/configs/` directory:

```
/plugins/LostWilderness-Common/configs/
  ├── common/
  │   └── config.yml
  ├── calendar/
  │   └── config.yml
  ├── clans/
  │   └── config.yml
  ├── events/
  │   └── config.yml
  ├── portals/
  │   └── config.yml
  ├── survival/
  │   └── config.yml
  └── amplified/
      └── config.yml
```

## For Server Admins

### Migration

When you upgrade to this new version, your existing config files will be automatically migrated to the new centralized location. No manual steps are required for migration.

### Commands

Use the `/lwconfig` command to manage all plugin configurations:

- `/lwconfig list` - List all available configuration modules
- `/lwconfig get <module> <path>` - Get a config value
- `/lwconfig set <module> <path> <value>` - Set a config value
- `/lwconfig reload <module|all>` - Reload configs

### Common Configuration

The `common/config.yml` file contains shared settings:

```yaml
# Server type (survival, amplified, lobby, etc.)
server-name: survival

# Database connection information shared by all plugins
mysql:
  host: localhost
  port: 3306
  database: minecraft
  username: minecraft
  password: changeme123
```

## For Developers

### Accessing Configuration

To access a module's configuration:

```java
// In any plugin that depends on LostWilderness-Common
FileConfiguration config = ConfigHelper.getConfig(this);
String value = config.getString("your.config.path", "default");
```

To access the shared database config:

```java
FileConfiguration dbConfig = ConfigHelper.getDatabaseConfig();
String host = dbConfig.getString("host", "localhost");
```

### Saving Configuration

To save changes to a module's configuration:

```java
// Make changes to config
config.set("your.config.path", "new value");

// Save changes
ConfigHelper.saveConfig(this);
```

### Plugin Dependencies

All plugins must include `LostWilderness-Common` as a dependency in their `plugin.yml`:

```yaml
name: LostWilderness-MyPlugin
main: com.lula0802.LWEvents.MyPluginMain
version: 2.0.0
api-version: 1.20
depend: [LostWilderness-Common]
```

## Plugins Load Order

1. LostWilderness-Common (loads first)
2. Module plugins (calendar, clans, events, portals)
3. Server plugins (survival, amplified)

This ensures that the centralized configuration system is available before any module attempts to access it.
