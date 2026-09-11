import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { AnalysisService } from './analysis-service.js';
import { AuthService } from './auth-service.js';
import { createChatGenerator } from './chat-generator.js';
import { ChatService } from './chat-service.js';
import { AnalysisStore } from './db/analysis-store.js';
import { AuthStore } from './db/auth-store.js';
import { ChatStore } from './db/chat-store.js';
import { createDatabase } from './db/db.js';
import { createDeepSeekGenerator } from './deepseek-generator.js';
import { createMarketDataProvider, createStockSearchProvider } from './market-data-provider.js';

const databasePath = process.env.DATABASE_PATH
  ? resolve(process.env.DATABASE_PATH)
  : fileURLToPath(new URL('../data/sairen.db', import.meta.url));
const database = createDatabase(databasePath, {
  legacyJsonPath: fileURLToPath(new URL('../data/analyses.json', import.meta.url)),
});
const marketDataProvider = createMarketDataProvider({
  timeoutMs: readNumberEnv('MARKET_DATA_TIMEOUT_MS', 15_000),
});

export const authService = new AuthService({
  store: new AuthStore(database),
  tokenTtlMs: readNumberEnv('AUTH_TOKEN_TTL_SECONDS', 30 * 24 * 60 * 60) * 1000,
});

export const analysisService = new AnalysisService({
  store: new AnalysisStore(database),
  generator: createDeepSeekGenerator({
    apiKey: process.env.DEEPSEEK_API_KEY,
    baseURL: process.env.DEEPSEEK_BASE_URL ?? 'https://api.deepseek.com',
    model: process.env.DEEPSEEK_MODEL ?? 'deepseek-chat',
    timeout: readNumberEnv('AI_TIMEOUT_MS', 90_000),
  }),
  marketDataProvider,
  cacheTtlMs: readNumberEnv('ANALYSIS_CACHE_TTL_MS', 60 * 60 * 1000),
  waitTimeoutMs: readNumberEnv('REQUEST_WAIT_TIMEOUT_MS', 120_000),
});

export const chatService = new ChatService({
  store: new ChatStore(database),
  generator: createChatGenerator({
    apiKey: process.env.DEEPSEEK_API_KEY,
    baseURL: process.env.DEEPSEEK_BASE_URL ?? 'https://api.deepseek.com',
    model: process.env.DEEPSEEK_MODEL ?? 'deepseek-chat',
    timeout: readNumberEnv('AI_TIMEOUT_MS', 90_000),
    marketDataProvider,
    stockSearchProvider: createStockSearchProvider({
      timeoutMs: readNumberEnv('MARKET_DATA_TIMEOUT_MS', 15_000),
    }),
  }),
});

function readNumberEnv(name, fallback) {
  const value = Number(process.env[name]);
  return Number.isFinite(value) && value > 0 ? value : fallback;
}
