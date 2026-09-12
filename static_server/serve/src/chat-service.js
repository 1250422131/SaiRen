import { randomUUID } from 'node:crypto';

export class ChatService {
  #store;
  #generator;
  #jobs = new Map();

  constructor({ store, generator }) {
    this.#store = store;
    this.#generator = generator;
  }

  async history(userId, options) {
    const history = await this.#store.getHistory(userId, options);
    return { ...history, messages: history.messages.map(publicMessage) };
  }

  async send(userId, { content, requestId = randomUUID() }) {
    const now = new Date().toISOString();
    const turn = await this.#store.createTurn({ userId, requestId, content, now });
    const userMessage = turn.find((message) => message.role === 'user');
    const assistantMessage = turn.find((message) => message.role === 'assistant');

    if (userMessage?.contents[0]?.data !== content) {
      throw chatError('REQUEST_ID_CONFLICT', 'requestId 已被其他消息使用。');
    }
    if (assistantMessage?.status === 'completed') {
      return { messages: turn.map(publicMessage), repeated: true };
    }
    if (assistantMessage?.status !== 'pending') {
      throw chatError('MESSAGE_ALREADY_FAILED', '该消息生成失败，请使用新的 requestId 重试。');
    }

    const jobKey = `${userId}:${requestId}`;
    const running = this.#jobs.get(jobKey);
    if (running) {
      return { messages: (await running).map(publicMessage), repeated: true };
    }

    const job = this.#generate(userId, requestId, content, userMessage.id);
    this.#jobs.set(jobKey, job);
    try {
      return { messages: (await job).map(publicMessage), repeated: false };
    } finally {
      if (this.#jobs.get(jobKey) === job) this.#jobs.delete(jobKey);
    }
  }

  async #generate(userId, requestId, content, userMessageId) {
    try {
      const history = await this.#store.getCompletedContext(userId, userMessageId);
      const contents = await this.#generator({ history, question: content });
      return await this.#store.completeAssistant(userId, requestId, contents, new Date().toISOString());
    } catch (error) {
      await this.#store.failAssistant(userId, requestId, '暂时无法生成回复，请稍后重试。', new Date().toISOString());
      throw error;
    }
  }
}

function publicMessage({ userId: _userId, ...message }) {
  return message;
}

function chatError(code, message) {
  const error = new Error(message);
  error.code = code;
  return error;
}
