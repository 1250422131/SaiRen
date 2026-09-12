import { and, eq, gt } from 'drizzle-orm';


export class AuthStore {
  #db;
  #schema;
  #close;

  constructor({ db, schema, close }) {
    this.#db = db;
    this.#schema = schema;
    this.#close = close;
  }

  async findUserByNormalizedUsername(usernameNormalized) {
    return (await this.#db.select().from(this.#schema.users)
      .where(eq(this.#schema.users.usernameNormalized, usernameNormalized)).limit(1))[0] ?? null;
  }

  async createUser(user) {
    await this.#db.insert(this.#schema.users).values(user);
    return user;
  }

  async createToken(token) {
    await this.#db.insert(this.#schema.authTokens).values(token);
  }

  async findUserByTokenHash(tokenHash, now) {
    const [row] = await this.#db.select({ user: this.#schema.users })
      .from(this.#schema.authTokens)
      .innerJoin(this.#schema.users, eq(this.#schema.users.id, this.#schema.authTokens.userId))
      .where(and(eq(this.#schema.authTokens.tokenHash, tokenHash), gt(this.#schema.authTokens.expiresAt, now)))
      .limit(1);
    if (!row) return null;

    await this.#db.update(this.#schema.authTokens).set({ lastUsedAt: now })
      .where(eq(this.#schema.authTokens.tokenHash, tokenHash));
    return row.user;
  }

  close() {
    return this.#close();
  }
}
