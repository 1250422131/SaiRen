import { serve } from '@hono/node-server';
import { Hono } from 'hono';
import { failure, success } from './http.js';
import { authMiddleware } from './middleware/auth.js';
import { authRouter } from './router/auth.js';
import { chatRouter } from './router/chat.js';
import { stockAnalysisRouter } from './router/stock-analysis.js';

const port = readNumberEnv('PORT', 8017);
const app = new Hono();

app.get('/health', (context) => context.json(success({ status: 'ok' })));
app.use('*', authMiddleware);
app.route('/v1/auth', authRouter);
app.route('/v1/chat', chatRouter);
app.route('/v1/stock-analyses', stockAnalysisRouter);

app.notFound((context) => context.json(failure(40402, '接口不存在。'), 404));
app.onError((error, context) => {
  console.error('Unhandled service error', error);
  return context.json(failure(50001, '服务内部错误。'), 500);
});

serve({ fetch: app.fetch, port }, () => {
  console.log(`SaiRen AI analysis service is listening on http://localhost:${port}`);
});

function readNumberEnv(name, fallback) {
  const value = Number(process.env[name]);
  return Number.isFinite(value) && value > 0 ? value : fallback;
}
