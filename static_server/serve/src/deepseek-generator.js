import OpenAI from 'openai';
import { AnalysisContentSchema } from './schema.js';

export function createDeepSeekGenerator({ apiKey, baseURL, model, timeout }) {
  if (!apiKey) {
    return async () => {
      const error = new Error('DEEPSEEK_API_KEY is not configured');
      error.code = 'AI_NOT_CONFIGURED';
      throw error;
    };
  }

  const client = new OpenAI({ apiKey, baseURL, timeout });
  return async (input) => {
    const completion = await client.chat.completions.create({
      model,
      temperature: 0.2,
      response_format: { type: 'json_object' },
      messages: [
        {
          role: 'system',
          content: [
            '你是证券行情信息解读助手。只能根据用户提供的股票、企业和K线数据进行分析，不能编造数据。',
            '输出必须是纯 JSON，不含 Markdown，不构成投资建议。',
            '买入建议位和卖出建议位应使用“观察区间”表述；数据不足时明确说明数据不足。',
            '严格返回以下结构：',
            '{"conclusion":{"headline":"","description":""},"buySuggestion":{"range":"","rationale":""},"sellSuggestion":{"range":"","rationale":""},"trend":{"label":"","rationale":""},"risk":{"level":"中等","warning":""},"signals":[{"title":"","description":""},{"title":"","description":""},{"title":"","description":""}],"summary":"","disclaimer":"内容仅供信息参考，不构成投资建议。"}',
          ].join('\n'),
        },
        {
          role: 'user',
          content: JSON.stringify(input),
        },
      ],
    });

    const content = completion.choices[0]?.message?.content;
    if (!content) {
      throw new Error('DeepSeek returned an empty response');
    }
    const parsed = AnalysisContentSchema.safeParse(parseJsonContent(content));
    if (!parsed.success) {
      const error = new Error('DeepSeek returned an invalid analysis payload');
      error.cause = parsed.error;
      throw error;
    }
    return parsed.data;
  };
}

function parseJsonContent(content) {
  const json = content.trim().replace(/^```json\s*/i, '').replace(/\s*```$/, '');
  return JSON.parse(json);
}
