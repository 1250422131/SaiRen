import { createMiddleware } from 'hono/factory';
import { failure } from '../http.js';
import { readBearerToken } from '../router/auth.js';
import { authService } from '../services.js';

const UNAUTHENTICATED_CODE = 4001;

const isPublicPath = (path) => [
  '/health',
  '/v1/auth/register',
  '/v1/auth/login',
  '/v1/stock-analyses',
].includes(path);

/** 全局 Bearer 鉴权，业务路由可通过 c.get('user') 获取当前登录用户。 */
export const authMiddleware = createMiddleware(async (context, next) => {
  if (isPublicPath(context.req.path)) return next();
  const user = authService.authenticate(readBearerToken(context.req.header('Authorization')));
  if (!user) return context.json(failure(UNAUTHENTICATED_CODE, '登录状态无效或已过期。'), 401);
  context.set('user', user);
  await next();
});
