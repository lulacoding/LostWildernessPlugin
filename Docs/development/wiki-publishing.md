---
title: Wiki Publishing
description: How to publish and maintain the player-facing wiki.
tags:
  - development
  - wiki
status: reference
phase: ongoing
owner: dev
action: none
---
# Wiki Publishing

Part of the [development documentation](README.md). Use this page if you want to publish the docs as a GitHub wiki or export them into another docs surface.


## GitHub Wiki Flow

1. Enable **Wiki** in your GitHub repository settings.
2. Clone the wiki repository:

```bash
git clone https://github.com/OWNER/REPO.wiki.git
cd REPO.wiki
```

3. Copy the contents of `Docs/` into that wiki repo.

Windows PowerShell:

```powershell
Copy-Item -Path "..\Lost Wilderness\Docs\*" -Destination ".\REPO.wiki\" -Recurse -Force
```

Windows cmd:

```bash
xcopy /E /Y "..\Lost Wilderness\Docs\*" "REPO.wiki\"
```

Linux / macOS / Git Bash:

```bash
cp -r ../Lost\ Wilderness/Docs/* REPO.wiki/
```

4. Commit and push the wiki repo.

```bash
git add .
git commit -m "Publish docs update"
git push origin main
```

---

## Notes

- Keep file links explicit with `.md` extensions.
- If your wiki platform expects special sidebar/footer naming, copy or rename `player/sidebar.md` and `player/footer.md` to whatever the target platform requires.
- Treat `Docs/` as the source of truth. The wiki should be a published copy, not the editing source.
