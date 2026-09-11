import { Hono } from 'hono';
import { failure, readJson, success } from '../http.js';
import { AuthCredentialsSchema } from '../schema.js';
import { authService } from '../services.js';

export const authRouter = new Hono();

authRouter.post('/register', async (context) => {
  const credentials = await readCredentials(context);
  if (!credentials.success) return context.json(failure(40010, credentials.message), 400);
  try {
    return context.json(success(await authService.register(credentials.data), '注册成功。'), 201);
  } catch (error) {
    if (error?.code === 'USERNAME_EXISTS') return context.json(failure(40901, error.message), 409);
    throw error;
  }
});

authRouter.post('/login', async (context) => {
  const credentials = await readCredentials(context);
  if (!credentials.success) return context.json(failure(40010, credentials.message), 400);
  try {
    return context.json(success(await authService.login(credentials.data), '登录成功。'));
  } catch (error) {
    if (error?.code === 'INVALID_CREDENTIALS') return context.json(failure(40101, error.message), 401);
    throw error;
  }
});

authRouter.get('/me', (context) => context.json(success({ user: context.get('user') })));

export function readBearerToken(authorization) {
  const match = /^Bearer\s+(.+)$/i.exec(authorization ?? '');
  return match?.[1] ?? '';
}

async function readCredentials(context) {
  const body = await readJson(context);
  if (body === null) return { success: false, message: '请求体必须为 JSON。' };
  const parsed = AuthCredentialsSchema.safeParse(body);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? '用户名或密码格式不正确。' };
  }
  return { success: true, data: parsed.data };
}
