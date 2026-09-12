import { existsSync, mkdirSync, readFileSync } from 'node:fs';
import { dirname } from 'node:path';
import Database from 'better-sqlite3';
import { drizzle } from 'drizzle-orm/better-sqlite3';
import { eq } from 'drizzle-orm';
import * as schema from './schema.js';

const JSON_MIGRATION_KEY = 'analyses_json_imported';

export function createDatabase(databasePath, { legacyJsonPath } = {}) {
  mkdirSync(dirname(databasePath), { recursive: true });
  const sqlite = new Database(databasePath);
  sqlite.pragma('journal_mode = WAL');
  sqlite.pragma('foreign_keys = ON');
  sqlite.pragma('busy_timeout = 5000');
  const db = drizzle(sqlite, { schema });

  createSchema(sqlite);
  migrateLegacyJson(db, sqlite, legacyJsonPath);
  return { db, sqlite, schema, close: () => sqlite.close() };
}

function createSchema(sqlite) {
  sqlite.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      username TEXT NOT NULL,
      username_normalized TEXT NOT NULL UNIQUE,
      password_hash TEXT NOT NULL,
      password_salt TEXT NOT NULL,
      created_at TEXT NOT NULL,
      updated_at TEXT NOT NULL
    ) STRICT;
    CREATE TABLE IF NOT EXISTS auth_tokens (
      token_hash TEXT PRIMARY KEY,
      user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      created_at TEXT NOT NULL,
      expires_at TEXT NOT NULL,
      last_used_at TEXT NOT NULL
    ) STRICT;
    CREATE INDEX IF NOT EXISTS idx_auth_tokens_user_id ON auth_tokens (user_id);
    CREATE INDEX IF NOT EXISTS idx_auth_tokens_expires_at ON auth_tokens (expires_at);
    CREATE TABLE IF NOT EXISTS schema_metadata (
      key TEXT PRIMARY KEY,
      value TEXT NOT NULL
    ) STRICT;
    CREATE TABLE IF NOT EXISTS stock_analyses (
      id TEXT PRIMARY KEY,
      stock_id TEXT NOT NULL,
      status TEXT NOT NULL CHECK (status IN ('analyzing', 'completed', 'failed')),
      input_json TEXT NOT NULL,
      analysis_json TEXT,
      error TEXT,
      created_at TEXT NOT NULL,
      updated_at TEXT NOT NULL
    ) STRICT;
    CREATE INDEX IF NOT EXISTS idx_stock_analyses_stock_created
    ON stock_analyses (stock_id, created_at DESC);
    CREATE TABLE IF NOT EXISTS chat_messages (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      request_id TEXT NOT NULL,
      role TEXT NOT NULL CHECK (role IN ('user', 'assistant')),
      contents_json TEXT NOT NULL,
      status TEXT NOT NULL CHECK (status IN ('pending', 'completed', 'failed')),
      created_at TEXT NOT NULL,
      updated_at TEXT NOT NULL,
      UNIQUE (user_id, request_id, role)
    ) STRICT;
    CREATE INDEX IF NOT EXISTS idx_chat_messages_user_id
    ON chat_messages (user_id, id);
  `);
}

function migrateLegacyJson(db, sqlite, legacyJsonPath) {
  const migration = db.select().from(schema.schemaMetadata)
    .where(eq(schema.schemaMetadata.key, JSON_MIGRATION_KEY)).get();
  if (migration) return;

  let analyses = [];
  if (legacyJsonPath && existsSync(legacyJsonPath)) {
    const legacyDatabase = JSON.parse(readFileSync(legacyJsonPath, 'utf8'));
    if (!Array.isArray(legacyDatabase.analyses)) {
      throw new Error('Invalid legacy analyses database format');
    }
    analyses = legacyDatabase.analyses;
  }

  const transaction = sqlite.transaction(() => {
    for (const analysis of analyses) {
      db.insert(schema.stockAnalyses).values({
        id: analysis.id,
        stockId: analysis.stockId,
        status: analysis.status,
        input: analysis.input ?? {},
        analysis: analysis.analysis ?? null,
        error: analysis.error ?? null,
        createdAt: analysis.createdAt,
        updatedAt: analysis.updatedAt,
      }).onConflictDoNothing().run();
    }
    db.insert(schema.schemaMetadata)
      .values({ key: JSON_MIGRATION_KEY, value: new Date().toISOString() })
      .run();
  });
  transaction();
}
