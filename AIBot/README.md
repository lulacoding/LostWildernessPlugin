# Lost Wilderness AI Bot

OpenClaw-powered Discord bot for the Lost Wilderness Minecraft project.

## What This Does

This bot provides an AI assistant that can:
- 🔍 Search project documentation
- 💻 Find code implementations
- 📖 Explain features comprehensively
- 🏗️ Answer architecture questions
- 📊 Report project status

## Setup

### Prerequisites
- Node.js ≥22
- OpenClaw installed: `npm install -g openclaw@latest`
- AWS Bedrock access with Claude models
- Discord bot configured

### Environment Variables

Set these before running OpenClaw:

```bash
# AWS Bedrock (Sydney region)
export AWS_BEARER_TOKEN_BEDROCK="your-token"
export AWS_REGION="ap-southeast-2"

# Discord
export DISCORD_BOT_TOKEN="your-bot-token"
```

### Configure OpenClaw Workspace

OpenClaw needs to know about these custom skills. Add this to `~/.openclaw/openclaw.json`:

```json
{
  "workspace": {
    "paths": [
      "C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/AIBot"
    ]
  }
}
```

Or run:
```bash
openclaw config set workspace.paths '["C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/AIBot"]'
```

### Verify Skills Loaded

```bash
openclaw skills list
```

You should see:
- `search-docs` - Search Lost Wilderness documentation
- `find-code` - Find code implementations
- `explain-feature` - Explain features comprehensively
- `architecture-query` - Answer architecture questions
- `project-status` - Report project status

## Usage

### In Discord

Message the bot directly or mention it:

```
@lost wilderness ai search for personality system
@lost wilderness ai find PartyService class
@lost wilderness ai explain the calendar system
@lost wilderness ai what's the project status?
```

### Available Commands

- `/search-docs <query>` - Search documentation
- `/find-code <query>` - Find code
- `/explain-feature <feature>` - Explain a feature
- `/architecture-query <question>` - Ask about architecture
- `/project-status` - Get project status

## Files

```
AIBot/
├── README.md                          # This file
├── skills/                            # Custom OpenClaw skills
│   ├── search-docs.md                 # Documentation search
│   ├── find-code.md                   # Code finding
│   ├── explain-feature.md             # Feature explanations
│   ├── architecture-query.md          # Architecture Q&A
│   └── project-status.md              # Project status reporting
├── setup-bedrock-bearer.ps1           # AWS setup (PowerShell)
├── setup-bedrock-bearer.sh            # AWS setup (Bash)
└── bedrock-config-snippet-sydney.json # Bedrock config

```

## Maintenance

### Update Skills

Edit any `.md` file in `skills/` directory, then restart OpenClaw:

```bash
# Stop gateway
openclaw gateway stop

# Start gateway
openclaw gateway start
```

### View Logs

```bash
openclaw logs follow
```

### Check Health

```bash
openclaw doctor
```

## Troubleshooting

### Bot Not Responding
1. Check gateway is running: `openclaw gateway status`
2. Verify Discord connection: `openclaw channels status`
3. Check logs: `openclaw logs follow`

### Skills Not Loading
1. Verify workspace path: `openclaw config get workspace.paths`
2. List skills: `openclaw skills list`
3. Check for errors: `openclaw doctor`

### AWS Bedrock Issues
1. Verify token: `echo $AWS_BEARER_TOKEN_BEDROCK`
2. Check region: `echo $AWS_REGION`
3. Test model: `openclaw models list`

## Resources

- OpenClaw Docs: https://docs.openclaw.ai/
- Discord Integration: https://docs.openclaw.ai/channels/discord
- Skills Guide: https://docs.openclaw.ai/tools/skills
- AWS Bedrock: https://docs.openclaw.ai/providers/bedrock
