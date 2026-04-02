---
title: External API
description: External API integrations and third-party service contracts.
tags:
  - reference
  - api
status: reference
phase: ongoing
owner: dev
action: none
---
# External API

Stable API surface for other plugins or services (e.g. Discord bot, web dashboard). Internal-only APIs are not listed here.


## Rules

- Only types and methods explicitly marked as “public API” are stable; internal packages can change without notice.
- Prefer interfaces for API contracts so implementations can evolve.
