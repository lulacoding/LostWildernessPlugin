---
title: Style Guide
description: Code style, naming conventions, and formatting standards.
tags:
  - development
  - style
status: reference
phase: ongoing
owner: dev
action: none
---
# Lost Wilderness — Documentation Style Guide

All markdown in `Docs/` follows this style so the docs read as one set, not random files in folders.


## 1. Document title (H1)

- **One H1 per file:** `# Page Title` (Title Case).
- **Project name:** Use em dash only for the main project title, e.g. `# Lost Wilderness — Centralised documentation`.
- **No other H1** in the file. One blank line after the H1.


## 3. Sections and headings

- **Levels:** Use `##` for main sections, `###` for subsections. No deeper unless needed.
- **Capitalisation:** Title Case for all headings (e.g. `## How the Calendar Works`, `### Commands`).
- **No trailing punctuation** on headings (no colons or full stops).
- **Spacing:** One blank line before each `##`; one blank line after each heading before body.


## 5. Tables

- **Header row** then a separator row with dashes: `| --- | --- |` (spaces optional but consistent).
- **Alignment:** Prefer `| --- | --- |` for simplicity. No need for `:---:` unless you want centred columns.
- **Consistency:** Same table style across all docs (e.g. command tables, quick links).


## 7. Links

- **Same folder:** `[Display text](page-name.md)` — use the actual filename (lowercase-with-hyphens.md).
- **Parent/other folders:** `[Display text](../path/to/Page.md)`.
- **Anchor:** `[Text](page.md#section-name)` when linking to a section.
- Use the `.md` extension for file links so they work in editors and static site generators.


## 9. Design section (Bible)

- **Structure:** H1 → optional implementation line → `---` → `## Purpose` or `## Concept` (or first section) → body → `---` → `## Source` at the end when content is from Discord/chat.
- **Source:** One line or short list, e.g. `*Source: Direct Messages (Grovyle187), channel X.*`
- **Cross-links:** Use relative paths to other design or Docs pages (e.g. `[Portal design](../06-technical-decisions/portal-design.md)`).


## 11. Development section

- **Structure:** H1 → optional "Part of centralised Docs" → `## Overview` or `## Table of Contents` → `---` → sections.
- **Code examples:** Always fenced with a language tag. Use `**Example:**` or `**Usage:**` before the block when helpful.
- **Sections:** Title Case, `---` between major `##` blocks.


*Apply this guide when adding or editing any file in `Docs/`.*
