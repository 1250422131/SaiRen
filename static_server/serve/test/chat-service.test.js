import assert from 'node:assert/strict';
import { mkdtemp, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import test from 'node:test';
import { AuthService } from '../src/auth-service.js';
import { ChatService } from '../src/chat-service.js';
import { AuthStore } from '../src/db/auth-store.js';
import { ChatStore } from '../src/db/chat-store.js';
import { createDatabase } from '../src/db/db.js';

test('persists multi-content chat messages and paginates history', async (context) => {
  const directory = await mkdtemp(join(tmpdir(), 'sairen-chat-'));
  context.after(() => rm(directory, { recursive: true, force: true }));
  const database = createDatabase(join(directory, 'sairen.db'));
  const store = new ChatStore(database);
  context.after(() => store.close());
  const auth = new AuthService({ store: new AuthStore(database), tokenTtlMs: 60_000 });
  const session = await auth.register({ username: '聊天用户', password: 'secret123' });
  let calls = 0;
  const service = new ChatService({
    store,
    generator: async () => {
      calls += 1;
      return [
        { type: 'markdown', data: '**行情如下**' },
        { type: 'stock_basic', data: { code: '600519', name: '贵州茅台' } },
        { type: 'stock_kline', data: { stock: { code: '600519' }, kLines: [{ day: '2026-09-07', close: '1500' }] } },
      ];
    },
  });

  const requestId = 'ecb72de4-8eae-4b40-a0a1-a5bcbad62abc';
  const result = await service.send(session.user.id, { content: '看看 600519', requestId });
  assert.equal(result.messages.length, 2);
  assert.equal(result.messages[0].role, 'user');
  assert.equal('userId' in result.messages[0], false);
  assert.equal(result.messages[1].role, 'assistant');
  assert.equal(result.messages[1].contents.length, 3);

  const repeated = await service.send(session.user.id, { content: '看看 600519', requestId });
  assert.equal(repeated.repeated, true);
  assert.equal(calls, 1);

  const latest = service.history(session.user.id, { limit: 1 });
  assert.equal(latest.messages.length, 1);
  assert.equal(latest.hasMore, true);
  const previous = service.history(session.user.id, { beforeId: latest.nextBeforeId, limit: 1 });
  assert.equal(previous.messages[0].role, 'user');
  assert.equal(previous.hasMore, false);
});

test('marks the assistant message failed when generation fails', async (context) => {
  const directory = await mkdtemp(join(tmpdir(), 'sairen-chat-error-'));
  context.after(() => rm(directory, { recursive: true, force: true }));
  const database = createDatabase(join(directory, 'sairen.db'));
  const store = new ChatStore(database);
  context.after(() => store.close());
  const auth = new AuthService({ store: new AuthStore(database), tokenTtlMs: 60_000 });
  const session = await auth.register({ username: '失败用户', password: 'secret123' });
  const service = new ChatService({ store, generator: async () => { throw new Error('AI unavailable'); } });

  await assert.rejects(service.send(session.user.id, { content: '你好' }), /AI unavailable/);
  const history = service.history(session.user.id, { limit: 10 });
  assert.equal(history.messages.at(-1).status, 'failed');
  assert.equal(history.messages.at(-1).contents[0].type, 'text');
});

test('retry during generation shares the same job and persists only one turn', async (context) => {
  const directory = await mkdtemp(join(tmpdir(), 'sairen-chat-retry-'));
  context.after(() => rm(directory, { recursive: true, force: true }));
  const database = createDatabase(join(directory, 'sairen.db'));
  const store = new ChatStore(database);
  context.after(() => store.close());
  const auth = new AuthService({ store: new AuthStore(database), tokenTtlMs: 60_000 });
  const session = await auth.register({ username: '重试用户', password: 'secret123' });
  let complete, calls = 0;
  const pending = new Promise(resolve => { complete = resolve; });
  const service = new ChatService({ store, generator: async () => { calls++; await pending; return [{ type: 'text', data: '回复' }]; } });
  const request = { content: '稳健选股', requestId: 'ecb72de4-8eae-4b40-a0a1-a5bcbad62abc' };
  const first = service.send(session.user.id, request);
  const retried = service.send(session.user.id, request);
  complete();
  const [a, b] = await Promise.all([first, retried]);
  assert.equal(calls, 1);
  assert.equal(b.repeated, true);
  assert.deepEqual(a.messages, b.messages);
  assert.equal(service.history(session.user.id, { limit: 10 }).messages.length, 2);
});
