import { and, asc, desc, eq, lt } from 'drizzle-orm';
import { chatMessages } from './schema.js';

export class ChatStore {
  #db;
  #sqlite;

  constructor({ db, sqlite }) {
    this.#db = db;
    this.#sqlite = sqlite;
  }

  createTurn({ userId, requestId, content, now }) {
    return this.#sqlite.transaction(() => {
      const existing = this.findByRequestId(userId, requestId);
      if (existing.length) return existing;

      this.#db.insert(chatMessages).values([
        {
          userId, requestId, role: 'user', contents: [{ type: 'text', data: content }],
          status: 'completed', createdAt: now, updatedAt: now,
        },
        {
          userId, requestId, role: 'assistant', contents: [],
          status: 'pending', createdAt: now, updatedAt: now,
        },
      ]).run();
      return this.findByRequestId(userId, requestId);
    })();
  }

  findByRequestId(userId, requestId) {
    return this.#db.select().from(chatMessages).where(and(
      eq(chatMessages.userId, userId), eq(chatMessages.requestId, requestId),
    )).orderBy(asc(chatMessages.id)).all();
  }

  completeAssistant(userId, requestId, contents, now) {
    this.#db.update(chatMessages).set({ contents, status: 'completed', updatedAt: now }).where(and(
      eq(chatMessages.userId, userId),
      eq(chatMessages.requestId, requestId),
      eq(chatMessages.role, 'assistant'),
      eq(chatMessages.status, 'pending'),
    )).run();
    return this.findByRequestId(userId, requestId);
  }

  failAssistant(userId, requestId, message, now) {
    this.#db.update(chatMessages).set({
      contents: [{ type: 'text', data: message }], status: 'failed', updatedAt: now,
    }).where(and(
      eq(chatMessages.userId, userId),
      eq(chatMessages.requestId, requestId),
      eq(chatMessages.role, 'assistant'),
      eq(chatMessages.status, 'pending'),
    )).run();
  }

  getHistory(userId, { beforeId, limit }) {
    const condition = beforeId
      ? and(eq(chatMessages.userId, userId), lt(chatMessages.id, beforeId))
      : eq(chatMessages.userId, userId);
    const rows = this.#db.select().from(chatMessages).where(condition)
      .orderBy(desc(chatMessages.id)).limit(limit + 1).all();
    const hasMore = rows.length > limit;
    const messages = rows.slice(0, limit).reverse();
    return { messages, hasMore, nextBeforeId: hasMore ? messages[0]?.id ?? null : null };
  }

  getCompletedContext(userId, beforeId, limit = 20) {
    return this.#db.select().from(chatMessages).where(and(
      eq(chatMessages.userId, userId),
      eq(chatMessages.status, 'completed'),
      lt(chatMessages.id, beforeId),
    )).orderBy(desc(chatMessages.id)).limit(limit).all().reverse();
  }

  close() {
    this.#sqlite.close();
  }
}
