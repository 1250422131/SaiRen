import { and, asc, desc, eq, lt } from 'drizzle-orm';

export class ChatStore {
  #db;
  #table;
  #close;

  constructor({ db, schema, close }) {
    this.#db = db;
    this.#table = schema.chatMessages;
    this.#close = close;
  }

  async createTurn({ userId, requestId, content, now }) {
    // 两条消息由同一个 INSERT 原子写入；唯一约束处理跨实例的重复请求。
    await this.#db.insert(this.#table).values([
      { userId, requestId, role: 'user', contents: [{ type: 'text', data: content }],
        status: 'completed', createdAt: now, updatedAt: now },
      { userId, requestId, role: 'assistant', contents: [],
        status: 'pending', createdAt: now, updatedAt: now },
    ]).onConflictDoNothing();
    return this.findByRequestId(userId, requestId);
  }

  async findByRequestId(userId, requestId) {
    const t = this.#table;
    return this.#db.select().from(t).where(and(
      eq(t.userId, userId), eq(t.requestId, requestId),
    )).orderBy(asc(t.id));
  }

  async completeAssistant(userId, requestId, contents, now) {
    const t = this.#table;
    await this.#db.update(t).set({ contents, status: 'completed', updatedAt: now }).where(and(
      eq(t.userId, userId), eq(t.requestId, requestId), eq(t.role, 'assistant'), eq(t.status, 'pending'),
    ));
    return this.findByRequestId(userId, requestId);
  }

  async failAssistant(userId, requestId, message, now) {
    const t = this.#table;
    await this.#db.update(t).set({ contents: [{ type: 'text', data: message }], status: 'failed', updatedAt: now }).where(and(
      eq(t.userId, userId), eq(t.requestId, requestId), eq(t.role, 'assistant'), eq(t.status, 'pending'),
    ));
  }

  async getHistory(userId, { beforeId, limit }) {
    const t = this.#table;
    const condition = beforeId ? and(eq(t.userId, userId), lt(t.id, beforeId)) : eq(t.userId, userId);
    const rows = await this.#db.select().from(t).where(condition).orderBy(desc(t.id)).limit(limit + 1);
    const hasMore = rows.length > limit;
    const messages = rows.slice(0, limit).reverse();
    return { messages, hasMore, nextBeforeId: hasMore ? messages[0]?.id ?? null : null };
  }

  async getCompletedContext(userId, beforeId, limit = 20) {
    const t = this.#table;
    const rows = await this.#db.select().from(t).where(and(
      eq(t.userId, userId), eq(t.status, 'completed'), lt(t.id, beforeId),
    )).orderBy(desc(t.id)).limit(limit);
    return rows.reverse();
  }

  close() { return this.#close(); }
}
