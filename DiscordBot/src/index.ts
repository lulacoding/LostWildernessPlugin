import 'dotenv/config';
import { Client, GatewayIntentBits, REST, Routes, SlashCommandBuilder } from 'discord.js';
import mcUtil from 'minecraft-server-util';
import * as db from './db';

const DISCORD_TOKEN = process.env.DISCORD_TOKEN;
const CLIENT_ID = process.env.DISCORD_CLIENT_ID;
const GUILD_ID = process.env.DISCORD_GUILD_ID;
const LOBBY_HOST = process.env.LOBBY_HOST ?? '127.0.0.1';
const LOBBY_PORT = parseInt(process.env.LOBBY_PORT ?? '25568', 10);
const SURVIVAL_HOST = process.env.SURVIVAL_HOST ?? '127.0.0.1';
const SURVIVAL_PORT = parseInt(process.env.SURVIVAL_PORT ?? '25566', 10);
const AMPLIFIED_HOST = process.env.AMPLIFIED_HOST ?? '127.0.0.1';
const AMPLIFIED_PORT = parseInt(process.env.AMPLIFIED_PORT ?? '25567', 10);

if (!DISCORD_TOKEN) {
  console.error('Set DISCORD_TOKEN environment variable.');
  process.exit(1);
}
if (!CLIENT_ID) {
  console.error('Set DISCORD_CLIENT_ID (Application ID) environment variable.');
  process.exit(1);
}
if (!GUILD_ID) {
  console.error('Set DISCORD_GUILD_ID (Guild / Server ID) environment variable.');
  process.exit(1);
}

// Ensure DB and tables exist before handling requests
db.getPool().then(() => console.log('Database connected.')).catch((e) => {
  console.error('Database connection failed:', e);
  process.exit(1);
});

// ─── Discord bot ──────────────────────────────────────────────────────────
const client = new Client({
  intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMembers],
});

const verifyCommand = new SlashCommandBuilder()
  .setName('verify')
  .setDescription('Link your Discord account to your Minecraft username (run /verify in-game first)')
  .addStringOption((opt) =>
    opt.setName('mc_username').setDescription('Your Minecraft username').setRequired(true)
  )
  .toJSON();

const playersCommand = new SlashCommandBuilder()
  .setName('players')
  .setDescription('Show player counts for all Lost Wilderness servers')
  .toJSON();

client.once('ready', async () => {
  console.log('Discord bot ready:', client.user?.tag);
  const rest = new REST().setToken(DISCORD_TOKEN!);
  await rest.put(Routes.applicationGuildCommands(CLIENT_ID!, GUILD_ID!), {
    body: [verifyCommand, playersCommand],
  });
  console.log('Slash commands /verify and /players registered for guild', GUILD_ID);
});

client.on('interactionCreate', async (interaction) => {
  if (!interaction.isChatInputCommand()) return;

  if (interaction.commandName === 'verify') {
    const mcUsername = interaction.options.getString('mc_username', true).trim();
    if (!mcUsername) {
      await interaction.reply({ content: 'Please provide your Minecraft username.', ephemeral: true });
      return;
    }

    const has = await db.hasPending(mcUsername);
    if (!has) {
      await interaction.reply({
        content: `No pending verification for **${mcUsername}**. Run \`/verify\` in-game first, then run \`/verify ${mcUsername}\` here.`,
        ephemeral: true,
      });
      return;
    }

    const member = interaction.guild?.members.resolve(interaction.user.id);
    if (!member) {
      await interaction.reply({ content: 'Could not resolve your member. Try again in the server.', ephemeral: true });
      return;
    }

    try {
      await db.removePending(mcUsername);
      await db.setLinked(interaction.user.id, mcUsername);
      await member.setNickname(mcUsername).catch((err) => {
        console.warn('Could not set nickname:', err.message);
      });
      await interaction.reply({
        content: `Verified! Your Discord nickname has been set to **${mcUsername}**. You can now use the teleporter in the lobby to go to Survival.`,
        ephemeral: true,
      });
    } catch (e) {
      console.error('Verify error:', e);
      await interaction.reply({
        content: 'Something went wrong. Please try again or ask staff.',
        ephemeral: true,
      });
    }
    return;
  }

  if (interaction.commandName === 'players') {
    await interaction.deferReply();
    const options = { timeout: 5000, enableSRV: false as const };

    const queries = [
      { name: 'Lobby', host: LOBBY_HOST, port: LOBBY_PORT },
      { name: 'Survival', host: SURVIVAL_HOST, port: SURVIVAL_PORT },
      { name: 'Amplified', host: AMPLIFIED_HOST, port: AMPLIFIED_PORT },
    ];

    const results = await Promise.all(
      queries.map(async (q) => {
        try {
          const res = await mcUtil.status(q.host, q.port, options);
          return { name: q.name, online: res.players.online, max: res.players.max, up: true };
        } catch {
          return { name: q.name, online: 0, max: 0, up: false };
        }
      }),
    );

    const totalOnline = results.reduce((sum, r) => sum + r.online, 0);
    const totalMax = results.reduce((sum, r) => sum + r.max, 0);

    const lines = [
      `**Total players:** ${totalOnline}${totalMax > 0 ? ` / ${totalMax}` : ''}`,
      ...results.map((r) =>
        `${r.up ? '🟢' : '🔴'} **${r.name}**: ${r.online}${r.max > 0 ? ` / ${r.max}` : ''}${
          r.up ? '' : ' (offline)'
        }`,
      ),
    ];

    await interaction.editReply(lines.join('\n'));
    return;
  }
});

client.login(DISCORD_TOKEN);
