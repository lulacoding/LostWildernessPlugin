---
title: Deployment
description: Plugin and server deployment procedures.
tags:
  - operations
  - deployment
status: reference
phase: ongoing
owner: ops
action: none
---
# Deployment

How to deploy the PluginV2 jar and config per server role.


## Config and secrets

- **db.yml:** Use env vars or a secrets manager for passwords; avoid committing real credentials.
- **core.yml:** enabled-modules and server-role per environment (dev/stage/prod).


## DB migrations

- Prefer separate migration scripts or a migration tool (e.g. Flyway) for V2 tables; document in implementation-status or a dedicated migrations doc.
