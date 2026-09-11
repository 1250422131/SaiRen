import { z } from 'zod';

const username = z.string()
  .trim()
  .min(3, '用户名至少需要 3 个字符。')
  .max(32, '用户名最多 32 个字符。')
  .regex(/^[\p{L}\p{N}_-]+$/u, '用户名只能包含文字、数字、下划线或短横线。');

const password = z.string()
  .min(6, '密码至少需要 6 个字符。')
  .max(72, '密码最多 72 个字符。');

export const AuthCredentialsSchema = z.object({
  username,
  password,
}).strict();

/** 客户端只能提交证券标识；所有分析依据由服务端拉取。 */
export const CreateAnalysisRequestSchema = z.object({
  code: z.string().trim().regex(/^\d{5,8}$/, '证券代码格式不正确。'),
  marketCode: z.enum(['0', '1']),
}).strict();

export const SendChatMessageRequestSchema = z.object({
  content: z.string().trim().min(1, '消息不能为空。').max(2000, '消息最多 2000 个字符。'),
  requestId: z.uuid('requestId 格式不正确。').optional(),
}).strict();

export const ChatStockReferenceSchema = z.object({
  code: z.string().regex(/^[A-Z0-9][A-Z0-9.-]{0,19}$/),
  marketCode: z.enum(['0', '1', '116', '105', '106', '107']),
}).strict().refine(({ code, marketCode }) => {
  if (marketCode === '0' || marketCode === '1') return /^\d{6}$/.test(code);
  if (marketCode === '116') return /^\d{5}$/.test(code);
  return /^[A-Z][A-Z0-9.-]{0,19}$/.test(code);
}, '证券代码与市场不匹配。');

const stockReference = ChatStockReferenceSchema;

export const ChatContentPlanSchema = z.object({
  contents: z.array(z.discriminatedUnion('type', [
    z.object({ type: z.literal('text'), data: z.string().trim().min(1).max(2000) }).strict(),
    z.object({ type: z.literal('markdown'), data: z.string().trim().min(1).max(6000) }).strict(),
    z.object({ type: z.literal('stock_basic'), stock: stockReference }).strict(),
    z.object({ type: z.literal('stock_kline'), stock: stockReference }).strict(),
    z.object({
      type: z.literal('stock_trade_timing'),
      stock: stockReference,
      buy: z.object({ range: z.string().trim().min(1).max(80), rationale: z.string().trim().min(1).max(300) }).strict(),
      sell: z.object({ range: z.string().trim().min(1).max(80), rationale: z.string().trim().min(1).max(300) }).strict(),
      risk: z.string().trim().min(1).max(300),
      disclaimer: z.string().trim().min(1).max(200),
    }).strict(),
    z.object({ type: z.literal('stock_company'), stock: stockReference }).strict(),
  ])).min(1).max(8),
}).strict();

export const AnalysisContentSchema = z.object({
  conclusion: z.object({
    headline: z.string().trim().min(1).max(120),
    description: z.string().trim().min(1).max(600),
  }).strict(),
  buySuggestion: z.object({
    range: z.string().trim().min(1).max(80),
    rationale: z.string().trim().min(1).max(240),
  }).strict(),
  sellSuggestion: z.object({
    range: z.string().trim().min(1).max(80),
    rationale: z.string().trim().min(1).max(240),
  }).strict(),
  trend: z.object({
    label: z.string().trim().min(1).max(40),
    rationale: z.string().trim().min(1).max(240),
  }).strict(),
  risk: z.object({
    level: z.enum(['低', '中低', '中等', '中高', '高']),
    warning: z.string().trim().min(1).max(360),
  }).strict(),
  signals: z.array(z.object({
    title: z.string().trim().min(1).max(40),
    description: z.string().trim().min(1).max(240),
  }).strict()).min(3).max(6),
  summary: z.string().trim().min(1).max(600),
  disclaimer: z.string().trim().min(1).max(240),
}).strict();
