import { index, integer, sqliteTable, text, uniqueIndex } from 'drizzle-orm/sqlite-core';

export const users = sqliteTable('users', {
  id: text('id').primaryKey(),
  username: text('username').notNull(),
  usernameNormalized: text('username_normalized').notNull().unique(),
  passwordHash: text('password_hash').notNull(),
  passwordSalt: text('password_salt').notNull(),
  createdAt: text('created_at').notNull(),
  updatedAt: text('updated_at').notNull(),
});

export const authTokens = sqliteTable('auth_tokens', {
  tokenHash: text('token_hash').primaryKey(),
  userId: text('user_id').notNull().references(() => users.id, { onDelete: 'cascade' }),
  createdAt: text('created_at').notNull(),
  expiresAt: text('expires_at').notNull(),
  lastUsedAt: text('last_used_at').notNull(),
}, (table) => [
  index('idx_auth_tokens_user_id').on(table.userId),
  index('idx_auth_tokens_expires_at').on(table.expiresAt),
]);

export const schemaMetadata = sqliteTable('schema_metadata', {
  key: text('key').primaryKey(),
  value: text('value').notNull(),
});

export const stockAnalyses = sqliteTable('stock_analyses', {
  id: text('id').primaryKey(),
  stockId: text('stock_id').notNull(),
  status: text('status', { enum: ['analyzing', 'completed', 'failed'] }).notNull(),
  input: text('input_json', { mode: 'json' }).notNull(),
  analysis: text('analysis_json', { mode: 'json' }),
  error: text('error'),
  createdAt: text('created_at').notNull(),
  updatedAt: text('updated_at').notNull(),
}, (table) => [
  index('idx_stock_analyses_stock_created').on(table.stockId, table.createdAt),
]);

export const chatMessages = sqliteTable('chat_messages', {
  id: integer('id').primaryKey({ autoIncrement: true }),
  userId: text('user_id').notNull().references(() => users.id, { onDelete: 'cascade' }),
  requestId: text('request_id').notNull(),
  role: text('role', { enum: ['user', 'assistant'] }).notNull(),
  contents: text('contents_json', { mode: 'json' }).notNull(),
  status: text('status', { enum: ['pending', 'completed', 'failed'] }).notNull(),
  createdAt: text('created_at').notNull(),
  updatedAt: text('updated_at').notNull(),
}, (table) => [
  uniqueIndex('uk_chat_messages_user_request_role').on(table.userId, table.requestId, table.role),
  index('idx_chat_messages_user_id').on(table.userId, table.id),
]);
