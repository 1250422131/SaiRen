import { desc, eq } from 'drizzle-orm';
import { stockAnalyses } from './schema.js';

export class AnalysisStore {
  #db;
  #sqlite;

  constructor({ db, sqlite }) {
    this.#db = db;
    this.#sqlite = sqlite;
  }

  async getLatest(stockId) {
    return this.#db.select().from(stockAnalyses)
      .where(eq(stockAnalyses.stockId, stockId))
      .orderBy(desc(stockAnalyses.createdAt)).limit(1).get() ?? null;
  }

  async create(analysis) {
    this.#db.insert(stockAnalyses).values(toRow(analysis)).run();
    return analysis;
  }

  async deleteByStockId(stockId) {
    this.#db.delete(stockAnalyses).where(eq(stockAnalyses.stockId, stockId)).run();
  }

  async update(id, changes) {
    const current = this.#db.select().from(stockAnalyses).where(eq(stockAnalyses.id, id)).get();
    if (!current) return null;

    const next = { ...current, ...changes };
    this.#db.update(stockAnalyses).set(toRow(next)).where(eq(stockAnalyses.id, id)).run();
    return next;
  }

  close() {
    this.#sqlite.close();
  }
}

function toRow(analysis) {
  return {
    id: analysis.id,
    stockId: analysis.stockId,
    status: analysis.status,
    input: analysis.input,
    analysis: analysis.analysis ?? null,
    error: analysis.error ?? null,
    createdAt: analysis.createdAt,
    updatedAt: analysis.updatedAt,
  };
}
