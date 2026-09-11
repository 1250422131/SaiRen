import { ChatStockReferenceSchema } from './schema.js';

const STOCK_SEARCH_API = 'https://searchapi.eastmoney.com/api/suggest/get';
const TENCENT_QUOTE_API = 'https://qt.gtimg.cn/';

const EAST_MONEY_API = 'https://push2delay.eastmoney.com/api/qt/stock/get';
const COMPANY_PROFILE_API =
  'https://emweb.securities.eastmoney.com/PC_HSF10/CompanySurvey/CompanySurveyAjax';
const SINA_KLINE_API =
  'https://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData';

/** 从可信行情源构建 AI 输入，绝不使用客户端提供的行情数值。 */
export function createMarketDataProvider({ timeoutMs = 15_000, fetchImpl = fetch } = {}) {
  return async ({ code, marketCode }) => {
    if (!ChatStockReferenceSchema.safeParse({ code, marketCode }).success) {
      throw marketDataError('证券代码与市场不匹配，请先按名称或代码搜索。');
    }
    const detail = await getStockDetail(code, marketCode, timeoutMs, fetchImpl).catch(async () => {
      try {
        if (!['0', '1'].includes(marketCode)) throw new Error('No fallback for this market');
        return await getTencentStockDetail(code, marketCode, timeoutMs, fetchImpl);
      } catch {
        throw marketDataError('暂时无法获取该股票行情，请确认股票代码和市场后重试。');
      }
    });
    const [company, kLines] = await Promise.all([
      getCompanyProfile(code, marketCode, timeoutMs, fetchImpl),
      getDailyKLines(code, marketCode, timeoutMs, fetchImpl),
    ]);
    return { stock: { ...detail, ...marketLabels(code, marketCode), marketCode }, company, marketData: { kLines } };
  };
}

async function getStockDetail(code, marketCode, timeoutMs, fetchImpl) {
  const params = new URLSearchParams({
    secid: `${marketCode}.${code}`,
    fltt: '2',
    invt: '2',
    fields: 'f43,f44,f45,f46,f47,f48,f57,f58,f60,f107,f116,f117,f168,f169,f170',
  });
  const payload = await requestJson(`${EAST_MONEY_API}?${params}`, timeoutMs, fetchImpl);
  if (text(payload.data?.f57) !== code || !payload.data?.f58) {
    throw marketDataError('未获取到有效的股票实时行情。');
  }
  const data = payload.data;
  return {
    code: text(data.f57), name: text(data.f58), latestPrice: text(data.f43),
    highestPrice: text(data.f44), lowestPrice: text(data.f45), openingPrice: text(data.f46),
    previousClosePrice: text(data.f60), volume: text(data.f47), amount: text(data.f48),
    turnoverRate: text(data.f168), changeAmount: text(data.f169), changePercent: text(data.f170),
    totalMarketValue: text(data.f116), circulatingMarketValue: text(data.f117),
  };
}

async function getCompanyProfile(code, marketCode, timeoutMs, fetchImpl) {
  const f10Code = toF10Code(code, marketCode);
  if (!f10Code) return {};
  try {
    const payload = await requestJson(`${COMPANY_PROFILE_API}?${new URLSearchParams({ code: f10Code })}`, timeoutMs, fetchImpl);
    const data = payload.jbzl ?? {};
    return {
      companyName: text(data.gsmc), industry: text(data.sshy), exchange: text(data.ssjys),
      summary: text(data.gsjj), businessScope: text(data.jyfw), chairman: text(data.dsz),
      legalRepresentative: text(data.frdb), generalManager: text(data.zjl), website: text(data.gswz),
    };
  } catch {
    return {};
  }
}

async function getDailyKLines(code, marketCode, timeoutMs, fetchImpl) {
  if (!['0', '1'].includes(marketCode)) {
    return getOverseasKLines(code, marketCode, timeoutMs, fetchImpl);
  }
  const symbol = marketCode === '1' ? `sh${code}` : code.startsWith('4') || code.startsWith('8') || code.startsWith('92') ? `bj${code}` : `sz${code}`;
  try {
    const params = new URLSearchParams({ symbol, scale: '240', ma: 'no', datalen: '120' });
    const payload = await requestJson(`${SINA_KLINE_API}?${params}`, timeoutMs, fetchImpl);
    if (!Array.isArray(payload)) return [];
    return payload.slice(-120).map((item) => ({
      day: text(item.day), open: text(item.open), high: text(item.high), low: text(item.low),
      close: text(item.close), volume: text(item.volume),
    }));
  } catch {
    return [];
  }
}

function toF10Code(code, marketCode) {
  if (!['0', '1'].includes(marketCode)) return null;
  if (marketCode === '1') return `SH${code}`;
  if (code.startsWith('4') || code.startsWith('8') || code.startsWith('92')) return `BJ${code}`;
  return `SZ${code}`;
}

async function requestJson(url, timeoutMs, fetchImpl) {
  const response = await fetchImpl(url, {
    signal: AbortSignal.timeout(timeoutMs),
    headers: { 'User-Agent': 'SaiRen-AI-Analysis-Service/1.0' },
  });
  if (!response.ok) throw marketDataError(`行情源请求失败（HTTP ${response.status}）。`);
  return response.json();
}

function text(value) { return value == null || value === '-' ? '' : String(value); }

function marketDataError(message) {
  const error = new Error(message);
  error.code = 'MARKET_DATA_UNAVAILABLE';
  return error;
}

/** 搜索结果保留市场号，接收 A 股及港美股普通股票，排除指数、基金、窝轮等衍生证券。 */
export function createStockSearchProvider({ timeoutMs = 15_000, fetchImpl = fetch } = {}) {
  return async ({ keyword }) => {
    const input = keyword.trim();
    if (!input || input.length > 80) throw marketDataError('请输入有效的股票名称或代码。');
    try {
      const params = new URLSearchParams({ input, type: '14', count: '10' });
      const payload = await requestJson(`${STOCK_SEARCH_API}?${params}`, timeoutMs, fetchImpl);
      const table = payload.QuotationCodeTable;
      if (table?.Status === 0 && table.Data == null && table.TotalCount === 0) return { stocks: [] };
      if (!table || table.Status !== 0 || !Array.isArray(table.Data)) {
        throw new Error('Invalid stock search response');
      }
      const stocks = new Map();
      for (const item of table.Data) {
        const marketCode = String(item.MktNum);
        // 科创板在搜索接口中使用独立分类 23，并不属于 AStock 分类。
        const isStarShare = marketCode === '1' && String(item.Classify) === '23'
          && String(item.SecurityType) === '25';
        const isAShare = ['0', '1'].includes(marketCode)
          && (item.Classify === 'AStock' || isStarShare
            || (item.Classify === 'NEEQ' && item.SecurityTypeName === '京A'));
        const isHKShare = item.Classify === 'HK' && item.TypeUS === '3' && marketCode === '116';
        const isUSShare = item.Classify === 'UsStock' && item.TypeUS === '3'
          && ['105', '106', '107'].includes(marketCode);
        if (!(isAShare || isHKShare || isUSShare) || !item.Name
          || !ChatStockReferenceSchema.safeParse({ code: item.Code, marketCode }).success) continue;
        stocks.set(`${marketCode}.${item.Code}`, {
          code: item.Code, name: item.Name, marketCode,
        });
      }
      const candidates = [...stocks.values()];
      const keywordCode = input.toUpperCase();
      const exactCodes = candidates.filter((stock) => stock.code === keywordCode
        || (stock.marketCode === '116' && /^\d{1,5}$/.test(keywordCode)
          && stock.code === keywordCode.padStart(5, '0')));
      return { stocks: exactCodes.length ? exactCodes : candidates };
    } catch {
      throw marketDataError('股票名称搜索暂时不可用，请稍后重试。');
    }
  };
}

async function getTencentStockDetail(code, marketCode, timeoutMs, fetchImpl) {
  const symbol = toF10Code(code, marketCode).toLowerCase();
  const response = await fetchImpl(`${TENCENT_QUOTE_API}?${new URLSearchParams({ q: symbol })}`, {
    signal: AbortSignal.timeout(timeoutMs),
    headers: { 'User-Agent': 'SaiRen-AI-Analysis-Service/1.0' },
  });
  if (!response.ok) throw marketDataError('备用行情源请求失败。');
  const body = new TextDecoder('gb18030').decode(await response.arrayBuffer());
  const match = body.match(new RegExp(`v_${symbol}="([^"\\r\\n]*)"`));
  const data = match?.[1].split('~');
  if (!data || data.length < 47 || data[2] !== code || !data[1]) {
    throw marketDataError('备用行情源未返回有效股票行情。');
  }
  return {
    code: data[2], name: data[1], latestPrice: text(data[3]),
    highestPrice: text(data[33]), lowestPrice: text(data[34]), openingPrice: text(data[5]),
    previousClosePrice: text(data[4]), volume: text(data[6]), amount: scaledNumber(data[37], 10000),
    turnoverRate: text(data[38]), changeAmount: text(data[31]), changePercent: text(data[32]),
    totalMarketValue: scaledNumber(data[45], 100000000),
    circulatingMarketValue: scaledNumber(data[44], 100000000),
  };
}

function scaledNumber(value, multiplier) {
  return value?.trim() && Number.isFinite(Number(value)) ? String(Number(value) * multiplier) : '';
}

function marketLabels(code, marketCode) {
  if (marketCode === '116') return { marketName: '港股', currency: code.startsWith('8') ? 'CNY' : 'HKD' };
  if (['105', '106', '107'].includes(marketCode)) return { marketName: '美股', currency: 'USD' };
  return { marketName: 'A股', currency: 'CNY' };
}

async function getOverseasKLines(code, marketCode, timeoutMs, fetchImpl) {
  try {
    const params = new URLSearchParams({
      secid: `${marketCode}.${code}`, klt: '101', fqt: '1', end: '20500101', lmt: '120',
      fields1: 'f1,f2,f3,f4,f5,f6', fields2: 'f51,f52,f53,f54,f55,f56',
    });
    const payload = await requestJson(`https://push2his.eastmoney.com/api/qt/stock/kline/get?${params}`, timeoutMs, fetchImpl);
    if (payload.data?.code !== code || String(payload.data?.market) !== marketCode
      || !Array.isArray(payload.data?.klines)) return [];
    return payload.data.klines.slice(-120).flatMap((line) => {
      if (typeof line !== 'string') return [];
      const [day, open, close, high, low, volume] = line.split(',');
      if (!day || [open, close, high, low, volume].some((value) => !value || !Number.isFinite(Number(value)))) return [];
      return [{ day, open, close, high, low, volume }];
    });
  } catch {
    return [];
  }
}
