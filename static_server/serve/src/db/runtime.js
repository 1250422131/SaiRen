import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

export async function createRuntimeDatabase(env = process.env) {
  const postgresUrl = env.DATABASE_URL?.trim() || env.POSTGRES_URL?.trim();
  if (postgresUrl) {
    const { createPostgresDatabase } = await import('./postgres-db.js');
    return createPostgresDatabase(postgresUrl);
  }
  if (env.VERCEL === '1' || env.VERCEL === true) {
    throw new Error('Vercel 部署必须配置 DATABASE_URL 或 POSTGRES_URL。');
  }
  const { createDatabase } = await import('./db.js');
  return createDatabase(env.DATABASE_PATH ? resolve(env.DATABASE_PATH)
    : fileURLToPath(new URL('../../data/sairen.db', import.meta.url)), {
    legacyJsonPath: fileURLToPath(new URL('../../data/analyses.json', import.meta.url)),
  });
}
