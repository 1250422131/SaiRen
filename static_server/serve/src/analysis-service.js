import { randomUUID } from 'node:crypto';

const ANALYZING = 'analyzing';
const COMPLETED = 'completed';
const FAILED = 'failed';

export class AnalysisService {
  #store;
  #generator;
  #marketDataProvider;
  #cacheTtlMs;
  #waitTimeoutMs;
  #jobs = new Map();
  #requestQueue = Promise.resolve();

  constructor({ store, generator, marketDataProvider, cacheTtlMs, waitTimeoutMs }) {
    this.#store = store;
    this.#generator = generator;
    this.#marketDataProvider = marketDataProvider;
    this.#cacheTtlMs = cacheTtlMs;
    this.#waitTimeoutMs = waitTimeoutMs;
  }

  async request(input) {
    const stockId = `${input.marketCode}.${input.code}`;
    const decision = await this.#withRequestLock(async () => {
      const latest = await this.#store.getLatest(stockId);
      if (latest?.status === COMPLETED && this.#isFresh(latest)) {
        return { record: latest, cached: true, job: null };
      }

      if (latest?.status === ANALYZING) {
        return { record: latest, cached: false, job: this.#resume(latest) };
      }

      const now = new Date().toISOString();
      const record = {
        id: randomUUID(),
        stockId,
        status: ANALYZING,
        createdAt: now,
        updatedAt: now,
        input,
      };
      await this.#store.deleteByStockId(stockId);
      await this.#store.create(record);
      return { record, cached: false, job: this.#start(record) };
    });

    if (decision.cached) {
      return this.#toResponse(decision.record, true);
    }

    try {
      const completed = await waitFor(decision.job, this.#waitTimeoutMs);
      return this.#toResponse(completed, false);
    } catch (error) {
      if (error?.code === 'WAIT_TIMEOUT') {
        const current = await this.#store.getLatest(stockId);
        return this.#toResponse(current ?? decision.record, false);
      }
      throw error;
    }
  }

  #resume(record) {
    return this.#jobs.get(record.id) ?? this.#start(record);
  }

  #start(record) {
    const existing = this.#jobs.get(record.id);
    if (existing) {
      return existing;
    }

    const job = this.#marketDataProvider(record.input)
      .then((input) => this.#generator(input))
      .then((analysis) => this.#store.update(record.id, {
        status: COMPLETED,
        analysis,
        updatedAt: new Date().toISOString(),
      }))
      .catch(async (error) => {
        await this.#store.update(record.id, {
          status: FAILED,
          error: 'AI 分析服务暂时不可用，请稍后重试。',
          updatedAt: new Date().toISOString(),
        });
        throw error;
      })
      .finally(() => {
        this.#jobs.delete(record.id);
      });
    this.#jobs.set(record.id, job);
    return job;
  }

  #isFresh(record) {
    return Date.now() - Date.parse(record.updatedAt) < this.#cacheTtlMs;
  }

  #toResponse(record, cached) {
    return {
      id: record.id,
      stockId: record.stockId,
      status: record.status,
      cached,
      createdAt: record.createdAt,
      updatedAt: record.updatedAt,
      analysis: record.analysis ?? null,
      error: record.error ?? null,
    };
  }

  #withRequestLock(operation) {
    const result = this.#requestQueue.then(operation, operation);
    this.#requestQueue = result.catch(() => undefined);
    return result;
  }
}

function waitFor(promise, timeoutMs) {
  let timeoutId;
  const timeout = new Promise((_, reject) => {
    timeoutId = setTimeout(() => {
      const error = new Error('Analysis is still running');
      error.code = 'WAIT_TIMEOUT';
      reject(error);
    }, timeoutMs);
  });
  return Promise.race([promise, timeout]).finally(() => clearTimeout(timeoutId));
}
