import { Hono } from 'hono';
import { failure, readJson, success } from '../http.js';
import { SendChatMessageRequestSchema } from '../schema.js';
import { chatService } from '../services.js';

export const chatRouter = new Hono();

chatRouter.get('/history', (context) => {
  const beforeIdQuery = context.req.query('beforeId');
  const limitQuery = context.req.query('limit');
  const beforeId = parsePositiveInteger(beforeIdQuery);
  const limit = limitQuery === undefined ? 50 : parsePositiveInteger(limitQuery);
  if (beforeIdQuery !== undefined && !beforeId) {
    return context.json(failure(40020, 'beforeId 必须为正整数。'), 400);
  }
  if (!limit || limit > 100) return context.json(failure(40020, 'limit 必须在 1 到 100 之间。'), 400);
  return context.json(success(chatService.history(context.get('user').id, { beforeId, limit })));
});

chatRouter.post('/messages', async (context) => {
  const body = await readJson(context);
  if (body === null) return context.json(failure(40001, '请求体必须为 JSON。'), 400);
  const parsed = SendChatMessageRequestSchema.safeParse(body);
  if (!parsed.success) {
    return context.json(failure(40021, parsed.error.issues[0]?.message ?? '消息格式不正确。'), 400);
  }

  try {
    return context.json(success(
      await chatService.send(context.get('user').id, parsed.data),
      '回复完成。',
    ));
  } catch (error) {
    if (error?.code === 'REQUEST_ID_CONFLICT' || error?.code === 'MESSAGE_ALREADY_FAILED') {
      return context.json(failure(40902, error.message), 409);
    }
    if (error?.code === 'AI_NOT_CONFIGURED') {
      return context.json(failure(50301, '服务端未配置 DEEPSEEK_API_KEY。'), 503);
    }
    if (error?.code === 'MARKET_DATA_UNAVAILABLE') {
      return context.json(failure(50202, error.message), 502);
    }
    console.error('AI 对话失败', error);
    return context.json(failure(50203, '暂时无法生成回复，请稍后重试。'), 502);
  }
});

function parsePositiveInteger(value) {
  if (value === undefined) return undefined;
  if (!/^\d+$/.test(value)) return null;
  const number = Number(value);
  return Number.isSafeInteger(number) && number > 0 ? number : null;
}
