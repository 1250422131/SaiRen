import { desc, eq } from 'drizzle-orm';


export class AnalysisStore {
  #db;
  #schema;
  #close;

  constructor({ db, schema, close }) {
    this.#db = db;
    this.#schema = schema;
    this.#close = close;
  }

  async getLatest(stockId) {
    return (await this.#db.select().from(this.#schema.stockAnalyses)
      .where(eq(this.#schema.stockAnalyses.stockId, stockId))
      .orderBy(desc(this.#schema.stockAnalyses.createdAt)).limit(1))[0] ?? null;
  }

  async create(analysis) {
    await this.#db.insert(this.#schema.stockAnalyses).values(toRow(analysis));
    return analysis;
  }

  async deleteByStockId(stockId) {
    await this.#db.delete(this.#schema.stockAnalyses).where(eq(this.#schema.stockAnalyses.stockId, stockId));
  }

  async update(id, changes) {
    const [current] = await this.#db.select().from(this.#schema.stockAnalyses).where(eq(this.#schema.stockAnalyses.id, id)).limit(1);
    if (!current) return null;

    const next = { ...current, ...changes };
    await this.#db.update(this.#schema.stockAnalyses).set(toRow(next)).where(eq(this.#schema.stockAnalyses.id, id));
    return next;
  }

  close() {
    return this.#close();
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
