import assert from 'node:assert/strict';
import test from 'node:test';
import { createMarketDataProvider, createStockSearchProvider } from '../src/market-data-provider.js';

const quote = { f57: '600519', f58: '贵州茅台', f43: 1309.3 };
const searchItem = { Code: '600519', Name: '贵州茅台', MktNum: '1', Classify: 'AStock' };

test('search encodes names and returns deduplicated A shares with market IDs', async () => {
  const search = createStockSearchProvider({ fetchImpl: async (url) => {
    assert.equal(new URL(url).searchParams.get('input'), '贵州茅台');
    return Response.json({ QuotationCodeTable: { Status: 0, Data: [
      searchItem, searchItem,
      { ...searchItem, Code: '000001', Name: '平安银行', MktNum: 0 },
      { ...searchItem, Code: '920002', Name: '万达轴承', MktNum: '0', Classify: 'NEEQ', SecurityTypeName: '京A' },
      { ...searchItem, Code: '430001', MktNum: '0', Classify: 'NEEQ', SecurityTypeName: '三板' },
      { ...searchItem, Classify: 'Index' },
      { ...searchItem, Code: '00700', MktNum: '116' },
    ] } });
  } });
  assert.deepEqual(await search({ keyword: ' 贵州茅台 ' }), { stocks: [
    { code: '600519', name: '贵州茅台', marketCode: '1' },
    { code: '000001', name: '平安银行', marketCode: '0' },
    { code: '920002', name: '万达轴承', marketCode: '0' },
  ] });
});

test('search distinguishes no matches from unavailable upstream', async () => {
  const search = createStockSearchProvider({ fetchImpl: async () => Response.json({
    QuotationCodeTable: { Status: 0, Data: null, TotalCount: 0 },
  }) });
  assert.deepEqual(await search({ keyword: '不存在的股票' }), { stocks: [] });
  const unavailable = createStockSearchProvider({ fetchImpl: async () => { throw new Error('fetch failed'); } });
  await assert.rejects(unavailable({ keyword: '茅台' }), { code: 'MARKET_DATA_UNAVAILABLE' });
});

test('valid primary quote survives optional company and K line failures', async () => {
  const provider = createMarketDataProvider({ fetchImpl: async (url) => {
    if (url.includes('push2delay')) return Response.json({ data: quote });
    assert.ok(!url.includes('qt.gtimg.cn'));
    throw new Error('optional upstream failed');
  } });
  const data = await provider({ code: '600519', marketCode: '1' });
  assert.equal(data.stock.latestPrice, '1309.3');
  assert.deepEqual(data.company, {});
  assert.deepEqual(data.marketData.kLines, []);
});

test('falls back to Tencent, decoding GBK and converting amounts to yuan', async () => {
  const fields = Array(60).fill('');
  // GBK bytes for 贵州茅台, assembled below instead of relying on UTF-8 fixtures.
  Object.assign(fields, { 0: '1', 1: 'NAME', 2: '600519', 3: '1309.30', 4: '1316.01',
    5: '1318.00', 6: '17534', 31: '-6.71', 32: '-0.51', 33: '1323.00',
    34: '1309.05', 37: '230282', 38: '0.14', 44: '16367.32', 45: '16367.32' });
  const [prefix, suffix] = `v_sh600519="${fields.join('~')}";`.split('NAME');
  const bytes = Buffer.concat([Buffer.from(prefix), Buffer.from('b9f3d6ddc3a9cca8', 'hex'), Buffer.from(suffix)]);
  const provider = createMarketDataProvider({ fetchImpl: async (url) => {
    if (url.includes('qt.gtimg.cn')) {
      assert.equal(new URL(url).searchParams.get('q'), 'sh600519');
      return new Response(bytes);
    }
    throw new Error('primary unavailable');
  } });
  const { stock } = await provider({ code: '600519', marketCode: '1' });
  assert.equal(stock.name, '贵州茅台');
  assert.equal(stock.latestPrice, '1309.30');
  assert.equal(stock.amount, '2302820000');
  assert.equal(stock.totalMarketValue, '1636732000000');
  assert.equal(stock.changePercent, '-0.51');
});

test('wrong stock or malformed fallback cannot become a successful quote', async () => {
  const provider = createMarketDataProvider({ fetchImpl: async (url) => url.includes('push2delay')
    ? Response.json({ data: { ...quote, f57: '000001' } }) : new Response('v_pv_none_match="1";') });
  await assert.rejects(provider({ code: '600519', marketCode: '1' }), { code: 'MARKET_DATA_UNAVAILABLE' });
});

test('Alibaba search retains HK and US stocks but excludes related derivatives and funds', async () => {
  const search = createStockSearchProvider({ fetchImpl: async () => Response.json({ QuotationCodeTable: {
    Status: 0, Data: [
      { Code: 'BABA', Name: '阿里巴巴', MktNum: '106', Classify: 'UsStock', TypeUS: '3' },
      { Code: '09988', Name: '阿里巴巴-W', MktNum: '116', Classify: 'HK', TypeUS: '3' },
      { Code: '89988', Name: '阿里巴巴-WR', MktNum: '116', Classify: 'HK', TypeUS: '3' },
      { Code: 'BABO', Name: '阿里巴巴ETF', MktNum: '107', Classify: 'UsStock', TypeUS: '5' },
      { Code: '29988', Name: '认沽证', MktNum: '116', Classify: 'HK', TypeUS: '6' },
      { Code: '05593', Name: 'BABA债券', MktNum: '116', Classify: 'HK', TypeUS: '2' },
    ],
  } }) });
  assert.deepEqual((await search({ keyword: '阿里巴巴' })).stocks.map(s => `${s.marketCode}.${s.code}`),
    ['106.BABA', '116.09988', '116.89988']);
});

for (const [code, marketCode, currency] of [['09988', '116', 'HKD'], ['BABA', '106', 'USD']]) {
  test(`overseas quote and daily K lines use the correct market: ${marketCode}.${code}`, async () => {
    const provider = createMarketDataProvider({ fetchImpl: async url => {
      assert.equal(new URL(url).searchParams.get('secid'), `${marketCode}.${code}`);
      if (url.includes('stock/get')) return Response.json({ data: { f57: code, f58: '阿里巴巴', f43: 100 } });
      assert.ok(url.includes('push2his.eastmoney.com'));
      return Response.json({ data: { code, market: Number(marketCode), klines: ['2026-09-08,98,100,101,97,12345'] } });
    } });
    const data = await provider({ code, marketCode });
    assert.equal(data.stock.currency, currency);
    assert.deepEqual(data.company, {});
    assert.deepEqual(data.marketData.kLines, [{ day: '2026-09-08', open: '98', close: '100', high: '101', low: '97', volume: '12345' }]);
  });
}

for (const [keyword, expected] of [['BABA', 'BABA'], ['baba', 'BABA'], ['9988', '09988']]) {
  test(`explicit code ${keyword} does not return other listings of the same company`, async () => {
    const search = createStockSearchProvider({ fetchImpl: async () => Response.json({ QuotationCodeTable: {
      Status: 0, Data: [
        { Code: 'BABA', Name: '阿里巴巴', MktNum: '106', Classify: 'UsStock', TypeUS: '3' },
        { Code: '09988', Name: '阿里巴巴-W', MktNum: '116', Classify: 'HK', TypeUS: '3' },
      ],
    } }) });
    assert.deepEqual((await search({ keyword })).stocks.map(s => s.code), [expected]);
  });
}

test('STAR Market classification 23 is retained alongside Shanghai and Shenzhen shares', async () => {
  const search = createStockSearchProvider({ fetchImpl: async () => Response.json({ QuotationCodeTable: {
    Status: 0, Data: [
      { Code: '688836', Name: '宇树科技-W', MktNum: '1', Classify: '23', SecurityType: '25', SecurityTypeName: '科创板' },
      { Code: '688981', Name: '中芯国际', MktNum: 1, Classify: 23, SecurityType: 25 },
      { Code: '688256', Name: '寒武纪', MktNum: '1', Classify: '23', SecurityType: '25' },
      { Code: '300750', Name: '宁德时代', MktNum: '0', Classify: 'AStock' },
      { Code: '600519', Name: '贵州茅台', MktNum: '1', Classify: 'AStock' },
      { Code: '000001', Name: '平安银行', MktNum: '0', Classify: 'AStock' },
      { Code: '000688', Name: '科创50', MktNum: '1', Classify: 'Index', SecurityType: '5' },
      { Code: '588000', Name: '科创50ETF', MktNum: '1', Classify: 'Fund', SecurityType: '3' },
      { Code: '688999', Name: '错误市场', MktNum: '0', Classify: '23', SecurityType: '25' },
    ],
  } }) });
  assert.deepEqual((await search({ keyword: '科技' })).stocks.map(s => `${s.marketCode}.${s.code}`),
    ['1.688836', '1.688981', '1.688256', '0.300750', '1.600519', '0.000001']);
});
