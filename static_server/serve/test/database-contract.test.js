import assert from 'node:assert/strict';
import test from 'node:test';
import { randomUUID } from 'node:crypto';
import { mkdtemp, rm, access } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { eq } from 'drizzle-orm';
import { createRuntimeDatabase } from '../src/db/runtime.js';
import { AuthService } from '../src/auth-service.js';
import { ChatService } from '../src/chat-service.js';
import { AuthStore } from '../src/db/auth-store.js';
import { ChatStore } from '../src/db/chat-store.js';
import { AnalysisStore } from '../src/db/analysis-store.js';

for (const mode of ['sqlite', 'neon']) {
  test(`${mode}: authentication, atomic turns, pagination, persistence and analysis CRUD`, {
    skip: mode === 'neon' && !process.env.NEON_TEST_DATABASE_URL,
  }, async t => {
    const directory = await mkdtemp(join(tmpdir(), 'sairen-contract-'));
    const path = join(directory, 'test.db');
    const env = mode === 'neon'
      ? { VERCEL: '1', DATABASE_URL: process.env.NEON_TEST_DATABASE_URL, DATABASE_PATH: path }
      : { DATABASE_PATH: path };
    const database = await createRuntimeDatabase(env);
    const other = await createRuntimeDatabase(env);
    const username = `test_${randomUUID().slice(0, 20)}`;
    const stockId = `test.${randomUUID()}`;
    t.after(async () => {
      try {
        await database.db.delete(database.schema.users).where(eq(database.schema.users.usernameNormalized, username));
        await database.db.delete(database.schema.stockAnalyses).where(eq(database.schema.stockAnalyses.stockId, stockId));
      } finally {
        await other.close();
        await database.close();
        await rm(directory, { recursive: true, force: true });
      }
    });
    const auth = new AuthService({ store: new AuthStore(database), tokenTtlMs: 60_000 });
    const session = await auth.register({ username, password: 'test-password-123' });
    assert.deepEqual(await auth.authenticate(session.token), session.user);
    assert.equal(await auth.authenticate('invalid'), null);
    assert.equal((await auth.login({ username, password: 'test-password-123' })).user.id, session.user.id);
    await assert.rejects(auth.register({ username, password: 'test-password-123' }), { code: 'USERNAME_EXISTS' });
    await assert.rejects(auth.login({ username, password: 'wrong' }), { code: 'INVALID_CREDENTIALS' });
    const otherAuth = new AuthService({ store: new AuthStore(other), tokenTtlMs: 60_000 });
    assert.deepEqual(await otherAuth.authenticate(session.token), session.user);
    const expired = await new AuthService({ store: new AuthStore(database), tokenTtlMs: -1000 })
      .login({ username, password: 'test-password-123' });
    assert.equal(await auth.authenticate(expired.token), null);

    const store = new ChatStore(database), otherStore = new ChatStore(other);
    const input = { userId: session.user.id, requestId: randomUUID(), content: '并发写入', now: new Date().toISOString() };
    const [a, b] = await Promise.all([store.createTurn(input), otherStore.createTurn(input)]);
    assert.deepEqual(a, b);
    assert.equal(a.length, 2);
    assert.ok(a.every(m => Number.isSafeInteger(m.id)));
    await store.completeAssistant(input.userId, input.requestId, [{ type: 'text', data: '完成' }], input.now);
    await otherStore.failAssistant(input.userId, input.requestId, '不能覆盖完成结果', input.now);
    assert.equal((await store.findByRequestId(input.userId, input.requestId))[1].status, 'completed');
    await assert.rejects(store.createTurn({ ...input, userId: randomUUID(), requestId: randomUUID() }));

    let calls = 0;
    const service = new ChatService({ store, generator: async ({ history }) => {
      calls++;
      assert.ok(history.some(m => m.contents[0]?.data === '完成'));
      return [{ type: 'markdown', data: '**测试回复**' }];
    } });
    const request = { content: '第二轮', requestId: randomUUID() };
    const sent = await service.send(input.userId, request);
    assert.equal(sent.messages[1].contents[0].data, '**测试回复**');
    assert.equal((await service.send(input.userId, request)).repeated, true);
    assert.equal(calls, 1);
    await assert.rejects(service.send(input.userId, { ...request, content: '不同问题' }), { code: 'REQUEST_ID_CONFLICT' });
    const latest = await service.history(input.userId, { limit: 2 });
    assert.equal(latest.hasMore, true);
    assert.equal((await service.history(input.userId, { beforeId: latest.nextBeforeId, limit: 2 })).messages.length, 2);
    assert.equal((await service.history('absent-user', { limit: 2 })).messages.length, 0);
    const failed = new ChatService({ store, generator: async () => { throw new Error('test failure'); } });
    await assert.rejects(failed.send(input.userId, { content: '失败测试' }), /test failure/);
    assert.equal((await otherStore.getHistory(input.userId, { limit: 1 })).messages[0].status, 'failed');

    const analyses = new AnalysisStore(database);
    assert.equal(await analyses.getLatest(stockId), null);
    const record = { id: randomUUID(), stockId, status: 'analyzing', input: { code: '600519', marketCode: '1' },
      createdAt: input.now, updatedAt: input.now };
    await analyses.create(record);
    const updated = await analyses.update(record.id, { status: 'completed', analysis: { summary: '测试分析', values: [1, 2] } });
    assert.deepEqual((await new AnalysisStore(other).getLatest(stockId)).analysis, updated.analysis);
    await analyses.deleteByStockId(stockId);
    assert.equal(await analyses.getLatest(stockId), null);
    assert.equal(await analyses.update(randomUUID(), { status: 'failed' }), null);
    if (mode === 'neon') await assert.rejects(access(path));
  });
}

test('Vercel without DATABASE_URL fails instead of falling back to SQLite', async () => {
  await assert.rejects(createRuntimeDatabase({ VERCEL: '1' }), /DATABASE_URL/);
});
