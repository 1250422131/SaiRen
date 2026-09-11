import assert from 'node:assert/strict';
import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { mkdtemp, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import test from 'node:test';
import { AnalysisStore } from '../src/db/analysis-store.js';
import { createDatabase } from '../src/db/db.js';

test('imports legacy JSON once and supports CRUD operations', async (context) => {
  const directory = await mkdtemp(join(tmpdir(), 'sairen-sqlite-store-'));
  context.after(() => rm(directory, { recursive: true, force: true }));

  const dataDirectory = join(directory, 'data');
  const legacyJsonPath = join(dataDirectory, 'analyses.json');
  const databasePath = join(dataDirectory, 'sairen.db');
  mkdirSync(dataDirectory, { recursive: true });

  const legacy = createAnalysis({ id: 'legacy', stockId: '1.600519' });
  writeFileSync(legacyJsonPath, JSON.stringify({ version: 1, analyses: [legacy] }));

  let store = new AnalysisStore(createDatabase(databasePath, { legacyJsonPath }));
  assert.deepEqual(await store.getLatest(legacy.stockId), { ...legacy, analysis: null, error: null });

  const created = createAnalysis({
    id: 'created',
    stockId: legacy.stockId,
    createdAt: '2026-08-31T10:00:00.000Z',
    updatedAt: '2026-08-31T10:00:00.000Z',
  });
  await store.create(created);
  assert.equal((await store.getLatest(legacy.stockId)).id, created.id);

  const completed = await store.update(created.id, {
    status: 'completed',
    analysis: { summary: '完成' },
    updatedAt: '2026-08-31T10:00:01.000Z',
  });
  assert.equal(completed.status, 'completed');
  assert.deepEqual(completed.analysis, { summary: '完成' });
  store.close();

  writeFileSync(legacyJsonPath, JSON.stringify({
    version: 1,
    analyses: [legacy, createAnalysis({ id: 'should-not-import', stockId: '0.000001' })],
  }));
  store = new AnalysisStore(createDatabase(databasePath, { legacyJsonPath }));
  assert.equal(await store.getLatest('0.000001'), null);

  await store.deleteByStockId(legacy.stockId);
  assert.equal(await store.getLatest(legacy.stockId), null);
  store.close();

  assert.doesNotThrow(() => readFileSync(databasePath));
});

function createAnalysis({
  id,
  stockId,
  createdAt = '2026-08-31T09:00:00.000Z',
  updatedAt = createdAt,
}) {
  return {
    id,
    stockId,
    status: 'analyzing',
    createdAt,
    updatedAt,
    input: { code: stockId.split('.').at(-1) },
  };
}
