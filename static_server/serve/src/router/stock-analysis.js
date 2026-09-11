import { Hono } from 'hono';
import { failure, readJson, success } from '../http.js';
import { CreateAnalysisRequestSchema } from '../schema.js';
import { analysisService } from '../services.js';

export const stockAnalysisRouter = new Hono();

stockAnalysisRouter.post('/', async (context) => {
  const body = await readJson(context);
  if (body === null) return context.json(failure(40001, '请求体必须为 JSON。'), 400);
  const parsed = CreateAnalysisRequestSchema.safeParse(body);
  if (!parsed.success) {
    return context.json(failure(40002, '请求必须包含 code 和 marketCode（0=深市/北交所，1=沪市）。'), 400);
  }
  try {
    const result = await analysisService.request(parsed.data);
    return context.json(
      success(result, result.status === 'analyzing' ? '分析中，请使用同一接口继续查询。' : '分析完成。'),
      result.status === 'analyzing' ? 202 : 200,
    );
  } catch (error) {
    if (error?.code === 'AI_NOT_CONFIGURED') {
      return context.json(failure(50301, '服务端未配置 DEEPSEEK_API_KEY。'), 503);
    }
    if (error?.code === 'MARKET_DATA_UNAVAILABLE') {
      return context.json(failure(50202, error.message), 502);
    }
    console.error('Failed to generate stock analysis', error);
    return context.json(failure(50201, '分析服务暂时不可用，请稍后重试。'), 502);
  }
});
