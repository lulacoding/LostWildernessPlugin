import mysql from 'mysql2/promise';

export interface DbConfig {
  host: string;
  port: number;
  database: string;
  user: string;
  password: string;
}

let pool: mysql.Pool | null = null;

export function getDbConfig(): DbConfig {
  return {
    host: process.env.MYSQL_HOST ?? 'localhost',
    port: parseInt(process.env.MYSQL_PORT ?? '3306', 10),
    database: process.env.MYSQL_DATABASE ?? 'minecraft',
    user: process.env.MYSQL_USER ?? 'minecraft',
    password: process.env.MYSQL_PASSWORD ?? '',
  };
}

export async function getPool(): Promise<mysql.Pool> {
  if (!pool) {
    const c = getDbConfig();
    pool = mysql.createPool({
      host: c.host,
      port: c.port,
      user: c.user,
      password: c.password,
      database: c.database,
      waitForConnections: true,
      connectionLimit: 10,
    });
    await initTables(pool);
  }
  return pool;
}

async function initTables(p: mysql.Pool): Promise<void> {
  const conn = await p.getConnection();
  try {
    await conn.execute(`
      CREATE TABLE IF NOT EXISTS pending_verification (
        mc_username VARCHAR(64) PRIMARY KEY,
        created_at BIGINT NOT NULL
      )
    `);
    await conn.execute(`
      CREATE TABLE IF NOT EXISTS linked_accounts (
        discord_id VARCHAR(64) NOT NULL,
        mc_username VARCHAR(64) PRIMARY KEY
      )
    `);
  } finally {
    conn.release();
  }
}

export async function hasPending(mcUsername: string): Promise<boolean> {
  const p = await getPool();
  const [rows] = await p.execute<mysql.RowDataPacket[]>(
    'SELECT 1 FROM pending_verification WHERE LOWER(mc_username) = LOWER(?) LIMIT 1',
    [mcUsername.trim()]
  );
  return rows.length > 0;
}

export async function removePending(mcUsername: string): Promise<boolean> {
  const p = await getPool();
  const [result] = await p.execute(
    'DELETE FROM pending_verification WHERE LOWER(mc_username) = LOWER(?)',
    [mcUsername.trim()]
  );
  return (result as mysql.ResultSetHeader).affectedRows > 0;
}

export async function setLinked(discordId: string, mcUsername: string): Promise<void> {
  const user = mcUsername.trim();
  const p = await getPool();
  await p.execute(
    'INSERT INTO linked_accounts (discord_id, mc_username) VALUES (?, ?) ON DUPLICATE KEY UPDATE discord_id = ?',
    [discordId, user, discordId]
  );
}
