import { and, eq, gt } from 'drizzle-orm';
import { authTokens, users } from './schema.js';

export class AuthStore {
  #db;
  #sqlite;

  constructor({ db, sqlite }) {
    this.#db = db;
    this.#sqlite = sqlite;
  }

  findUserByNormalizedUsername(usernameNormalized) {
    return this.#db.select().from(users)
      .where(eq(users.usernameNormalized, usernameNormalized)).get() ?? null;
  }

  createUser(user) {
    this.#db.insert(users).values(user).run();
    return user;
  }

  createToken(token) {
    this.#db.insert(authTokens).values(token).run();
  }

  findUserByTokenHash(tokenHash, now) {
    const row = this.#db.select({ user: users })
      .from(authTokens)
      .innerJoin(users, eq(users.id, authTokens.userId))
      .where(and(eq(authTokens.tokenHash, tokenHash), gt(authTokens.expiresAt, now)))
      .get();
    if (!row) return null;

    this.#db.update(authTokens).set({ lastUsedAt: now })
      .where(eq(authTokens.tokenHash, tokenHash)).run();
    return row.user;
  }

  close() {
    this.#sqlite.close();
  }
}
