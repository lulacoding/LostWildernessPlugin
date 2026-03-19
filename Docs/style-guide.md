# Lost Wilderness — Documentation Style Guide

All markdown in `Docs/` follows this style so the docs read as one set, not random files in folders.

---

## 0. File and folder naming

- **Folders:** `lowercase-with-hyphens`. Design section folders keep a numeric prefix for order: `01-overview`, `02-infrastructure`, `03-plugins`, `04-resource-packs`, `05-worlds-gameplay`, `06-technical-decisions`, `07-audio-visual`, `08-open-questions`.
- **Files:** `lowercase-with-hyphens.md`. Exception: `README.md` stays (convention for index pages).
- **No spaces, no Title Case, no UPPERCASE** in file or folder names. Use hyphens for word separation (e.g. `calendar-and-time.md`, `link-approval.md`, `dev-guide.md`).
- **Wiki helpers:** In `player/`, use `sidebar.md` and `footer.md` (not `_Sidebar.md` / `_Footer.md`) so everything matches the same pattern.
- **Links:** Always point at the real filename (e.g. `[Calendar and Time](calendar-and-time.md)`). Display text can be Title Case; the path must match the file name.

---

## 1. Document title (H1)

- **One H1 per file:** `# Page Title` (Title Case).
- **Project name:** Use em dash only for the main project title, e.g. `# Lost Wilderness — Centralised documentation`.
- **No other H1** in the file. One blank line after the H1.

---

## 2. Lead and meta

- **First line after H1:** Optional one-sentence description. No `---` between H1 and this line.
- **Implementation (design only):** If the page has an implementation note, use one short line:  
  `Implementation: See [implementation-status](path). Implemented: X. Not implemented: Y.`  
  Then add `---` before the first section.
- **Part of Docs (player/development):** One line linking back, e.g. `Part of the [centralised Docs](../README.md).` then continue.

---

## 3. Sections and headings

- **Levels:** Use `##` for main sections, `###` for subsections. No deeper unless needed.
- **Capitalisation:** Title Case for all headings (e.g. `## How the Calendar Works`, `### Commands`).
- **No trailing punctuation** on headings (no colons or full stops).
- **Spacing:** One blank line before each `##`; one blank line after each heading before body.

---

## 4. Horizontal rules

- Use `---` (three hyphens, blank line above and below) **only between major sections**.
- Use after the intro/lead block, then between each `##` section for clarity.
- Do not use `---` after every paragraph or before every list.

---

## 5. Tables

- **Header row** then a separator row with dashes: `| --- | --- |` (spaces optional but consistent).
- **Alignment:** Prefer `| --- | --- |` for simplicity. No need for `:---:` unless you want centred columns.
- **Consistency:** Same table style across all docs (e.g. command tables, quick links).

---

## 6. Lists

- **Unordered:** Use `-` and one space. No `*` or `+`.
- **Spacing:** One blank line before a list when it follows a paragraph.
- **Nested:** Indent with two spaces for nested bullets.

---

## 7. Links

- **Same folder:** `[Display text](page-name.md)` — use the actual filename (lowercase-with-hyphens.md).
- **Parent/other folders:** `[Display text](../path/to/Page.md)`.
- **Anchor:** `[Text](page.md#section-name)` when linking to a section.
- Use the `.md` extension for file links so they work in editors and static site generators.

---

## 8. Code blocks

- **Fenced:** Always use triple backticks.
- **Language tag:** Always set a language (e.g. `sql`, `yaml`, `java`, `text`) for syntax highlighting.

---

## 9. Design section (Bible)

- **Structure:** H1 → optional implementation line → `---` → `## Purpose` or `## Concept` (or first section) → body → `---` → `## Source` at the end when content is from Discord/chat.
- **Source:** One line or short list, e.g. `*Source: Direct Messages (Grovyle187), channel X.*`
- **Cross-links:** Use relative paths to other design or Docs pages (e.g. `[Portal design](../06-technical-decisions/portal-design.md)`).

---

## 10. Player section (wiki)

- **Structure:** H1 → `## Overview` (one short paragraph) → `---` → further sections (e.g. `## Commands`, `## Tips`).
- **Commands:** Use a table: `| Command | Description |` then `| --- | --- |` then rows.
- **See also:** Optional `## See also` at the end with links to related player pages.
- **No Source block** (player docs are written for players, not attributed to chat).

---

## 11. Development section

- **Structure:** H1 → optional "Part of centralised Docs" → `## Overview` or `## Table of Contents` → `---` → sections.
- **Code examples:** Always fenced with a language tag. Use `**Example:**` or `**Usage:**` before the block when helpful.
- **Sections:** Title Case, `---` between major `##` blocks.

---

## 12. Index and README pages

- **Structure:** H1 → one-line description → `---` → `## Quick links` or `## Section Name` with a table or bullet list of links.
- **Link list:** Use consistent format: `- [Page title](path/to/page.md) — Short description.`
- **Tables:** Use when listing multiple columns (e.g. Section | Description).

---

*Apply this guide when adding or editing any file in `Docs/`.*
