import postgres from 'postgres';
import { drizzle } from 'drizzle-orm/postgres-js';
import * as schema from './pg-schema.js';

export function createPostgresDatabase(url) {
  const client = postgres(url, { max: 1, prepare: false, connect_timeout: 15, idle_timeout: 20 });
  return { db: drizzle(client, { schema }), client, schema, close: () => client.end({ timeout: 5 }) };
}
