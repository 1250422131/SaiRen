import { readFile } from 'node:fs/promises';
import { neon } from '@neondatabase/serverless';
const sql = neon(process.env.DATABASE_URL);
if (!process.env.DATABASE_URL) throw new Error('DATABASE_URL is required');
const source = await readFile(new URL('../migrations/0001_init.sql', import.meta.url), 'utf8');
for (const statement of source.split(';').map((s) => s.trim()).filter(Boolean)) await sql.query(statement);
console.log('Neon migration completed');
