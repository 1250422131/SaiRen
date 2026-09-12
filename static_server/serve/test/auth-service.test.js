import assert from 'node:assert/strict';
import { mkdtemp, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import test from 'node:test';
import { AuthService } from '../src/auth-service.js';
import { AuthStore } from '../src/db/auth-store.js';
import { createDatabase } from '../src/db/db.js';

test('registers, logs in and authenticates a user', async (context) => {
  const directory = await mkdtemp(join(tmpdir(), 'sairen-auth-'));
  context.after(() => rm(directory, { recursive: true, force: true }));

  const store = new AuthStore(createDatabase(join(directory, 'sairen.db')));
  context.after(() => store.close());
  const service = new AuthService({ store, tokenTtlMs: 60_000 });

  const registered = await service.register({ username: '测试用户', password: 'secret123' });
  assert.equal(registered.user.username, '测试用户');
  assert.equal((await service.authenticate(registered.token)).username, '测试用户');

  const loggedIn = await service.login({ username: '测试用户', password: 'secret123' });
  assert.notEqual(loggedIn.token, registered.token);
  assert.equal((await service.authenticate(loggedIn.token)).id, registered.user.id);

  await assert.rejects(
    service.register({ username: '测试用户', password: 'secret123' }),
    { code: 'USERNAME_EXISTS' },
  );
  await assert.rejects(
    service.login({ username: '测试用户', password: 'wrong-password' }),
    { code: 'INVALID_CREDENTIALS' },
  );
  assert.equal((await service.authenticate('invalid-token')), null);
});
