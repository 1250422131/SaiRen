import assert from 'node:assert/strict';
import test from 'node:test';
import { createChatGenerator } from '../src/chat-generator.js';

test('uses the stock tool and hydrates stock cards from server data', async () => {
  const stockData = {
    stock: { code: '600519', name: '贵州茅台', latestPrice: '1500.00' },
    company: { companyName: '贵州茅台酒股份有限公司' },
    marketData: { kLines: [{ day: '2026-09-07', close: '1500.00' }] },
  };
  const responses = [
    {
      choices: [{ message: {
        role: 'assistant', content: null,
        tool_calls: [{
          id: 'call_1', type: 'function',
          function: { name: 'get_stock_data', arguments: '{"code":"600519","marketCode":"1"}' },
        }],
      } }],
    },
    {
      choices: [{ message: {
        role: 'assistant',
        content: JSON.stringify({ contents: [
          { type: 'markdown', data: '这是最新行情。' },
          { type: 'stock_basic', stock: { code: '600519', marketCode: '1' } },
          { type: 'stock_kline', stock: { code: '600519', marketCode: '1' } },
          { type: 'stock_company', stock: { code: '600519', marketCode: '1' } },
        ] }),
      } }],
    },
  ];
  const requests = [];
  const client = {
    chat: { completions: { create: async (request) => {
      requests.push(request);
      return responses.shift();
    } } },
  };
  let providerCalls = 0;
  const generator = createChatGenerator({
    model: 'test-model',
    client,
    marketDataProvider: async (stock) => {
      providerCalls += 1;
      assert.deepEqual(stock, { code: '600519', marketCode: '1' });
      return stockData;
    },
  });

  const contents = await generator({ history: [], question: '贵州茅台行情和公司资料' });
  assert.equal(providerCalls, 1);
  assert.equal(requests.length, 2);
  assert.equal(requests[1].messages.at(-1).role, 'tool');
  assert.deepEqual(contents[1], { type: 'stock_basic', data: stockData.stock });
  assert.deepEqual(contents[2], {
    type: 'stock_kline', data: { stock: stockData.stock, kLines: stockData.marketData.kLines },
  });
  assert.deepEqual(contents[3], {
    type: 'stock_company', data: { stock: stockData.stock, company: stockData.company },
  });
});

test('repairs a truncated JSON response once', async () => {
  const responses = [
    {
      id: 'response_1',
      model: 'test-model',
      choices: [{
        finish_reason: 'length',
        message: { role: 'assistant', content: '{"contents":[{"type":"text"' },
      }],
    },
    {
      id: 'response_2',
      model: 'test-model',
      choices: [{
        finish_reason: 'stop',
        message: {
          role: 'assistant',
          content: JSON.stringify({ contents: [{ type: 'text', data: '已重新生成完整回答。' }] }),
        },
      }],
    },
  ];
  const requests = [];
  const client = {
    chat: { completions: { create: async (request) => {
      requests.push(request);
      return responses.shift();
    } } },
  };
  const generator = createChatGenerator({
    model: 'test-model',
    client,
    marketDataProvider: async () => assert.fail('不应调用股票工具'),
  });

  const contents = await generator({ history: [], question: '你好' });

  assert.deepEqual(contents, [{ type: 'text', data: '已重新生成完整回答。' }]);
  assert.equal(requests.length, 2);
  assert.equal(requests[1].tools, undefined);
  assert.match(requests[1].messages.at(-1).content, /完整 JSON/);
});

function toolCompletion(name, args, id = 'call_1') {
  return { choices: [{ message: { role: 'assistant', content: null, tool_calls: [
    { id, type: 'function', function: { name, arguments: JSON.stringify(args) } },
  ] } }] };
}
function finalCompletion(contents) {
  return { choices: [{ message: { role: 'assistant', content: JSON.stringify({ contents }) } }] };
}
function queuedClient(responses, requests) {
  return { chat: { completions: { create: async (request) => {
    requests.push(structuredClone(request));
    return responses.shift();
  } } } };
}

test('searches a name before querying the returned stock reference', async () => {
  const reference = { code: '000001', marketCode: '0' };
  const data = { stock: { ...reference, name: '平安银行' }, company: {}, marketData: { kLines: [] } };
  const requests = [];
  const calls = [];
  const generator = createChatGenerator({ model: 'test',
    client: queuedClient([
      toolCompletion('search_stocks', { keyword: '平安银行' }),
      toolCompletion('get_stock_data', reference, 'call_2'),
      finalCompletion([{ type: 'stock_basic', stock: reference }, { type: 'text', data: '等待趋势确认后再考虑参与。' }]),
    ], requests),
    stockSearchProvider: async (args) => {
      calls.push('search');
      assert.equal(args.keyword, '平安银行');
      return { stocks: [{ ...reference, name: '平安银行' }] };
    },
    marketDataProvider: async (args) => {
      calls.push('quote');
      assert.deepEqual(args, reference);
      return data;
    },
  });
  assert.deepEqual((await generator({ history: [], question: '平安银行' }))[0], { type: 'stock_basic', data: data.stock });
  assert.deepEqual(calls, ['search', 'quote']);
  assert.ok(requests[0].tools.some((tool) => tool.function.name === 'search_stocks'));
});

test('quote failure can search and correct market within three tool rounds', async () => {
  const requests = [];
  const reference = { code: '600519', marketCode: '1' };
  const data = { stock: { code: '600519', name: '贵州茅台' } };
  const generator = createChatGenerator({ model: 'test',
    client: queuedClient([
      toolCompletion('get_stock_data', { code: '600519', marketCode: '0' }),
      toolCompletion('search_stocks', { keyword: '600519' }, 'call_2'),
      toolCompletion('get_stock_data', reference, 'call_3'),
      finalCompletion([{ type: 'stock_basic', stock: reference }, { type: 'text', data: '等待趋势确认后再考虑参与。' }]),
    ], requests),
    stockSearchProvider: async () => ({ stocks: [{ ...reference, name: '贵州茅台' }] }),
    marketDataProvider: async ({ marketCode }) => {
      if (marketCode === '0') throw Object.assign(new Error('行情未找到'), { code: 'MARKET_DATA_UNAVAILABLE' });
      return data;
    },
  });
  assert.deepEqual((await generator({ history: [], question: '600519' }))[0], { type: 'stock_basic', data: data.stock });
  assert.equal(JSON.parse(requests[1].messages.at(-1).content).error, 'MARKET_DATA_UNAVAILABLE');
  assert.equal(requests[3].tools, undefined);
});

test('failed queries cannot hydrate invented data cards', async () => {
  const reference = { code: '600519', marketCode: '1' };
  const card = finalCompletion([{ type: 'stock_basic', stock: reference }]);
  const generator = createChatGenerator({ model: 'test',
    client: queuedClient([toolCompletion('get_stock_data', reference), card, card], []),
    marketDataProvider: async () => { throw new Error('offline'); },
  });
  const result = await generator({ history: [], question: '600519' });
  assert.ok(result.every(item => item.type === 'text'));
  assert.match(result[0].data, /未能完成可靠查询/);
});

test('rejects a guessed historic code then searches and hydrates Alibaba HK and US cards', async () => {
  const hk = { code: '09988', marketCode: '116' };
  const us = { code: 'BABA', marketCode: '106' };
  const requests = [], quotes = [];
  const both = toolCompletion('get_stock_data', hk, 'hk');
  both.choices[0].message.tool_calls.push(toolCompletion('get_stock_data', us, 'us').choices[0].message.tool_calls[0]);
  const generator = createChatGenerator({ model: 'test',
    client: queuedClient([
      toolCompletion('get_stock_data', { code: '689009', marketCode: '1' }),
      toolCompletion('search_stocks', { keyword: '阿里巴巴' }),
      both,
      finalCompletion([{ type: 'stock_basic', stock: hk }, { type: 'stock_basic', stock: us }]),
      finalCompletion([{ type: 'stock_basic', stock: hk }, { type: 'stock_basic', stock: us }]),
    ], requests),
    stockSearchProvider: async () => ({ stocks: [{ ...hk, name: '阿里巴巴-W' }, { ...us, name: '阿里巴巴' }] }),
    marketDataProvider: async ref => { quotes.push(ref); return { stock: { code: ref.code, name: '阿里巴巴' } }; },
  });
  const contents = await generator({ question: '阿里巴巴股票怎么样', history: [
    { role: 'user', contents: [{ type: 'text', data: '689009' }] },
    { role: 'assistant', contents: [{ type: 'text', data: '689009 查询失败' }] },
  ] });
  assert.deepEqual(quotes, [hk, us]);
  assert.equal(JSON.parse(requests[1].messages.at(-1).content).error, 'STOCK_NOT_RESOLVED');
  assert.deepEqual(contents.filter(c => c.type === 'stock_basic').map(c => c.data.code), ['09988', 'BABA']);
  assert.equal(contents.filter(c => c.type === 'stock_trade_timing').length, 2);
});

test('repairs a chart-only analysis into a buy/sell advice card using existing market data', async () => {
  const reference = { code: '09988', marketCode: '116' };
  const requests = [];
  const advice = { type: 'stock_trade_timing', stock: reference,
    buy: { range: '暂不提供点位', rationale: '等待成交量确认。' },
    sell: { range: '暂不提供点位', rationale: '根据既定止盈止损计划评估。' },
    risk: '样本不足。', disclaimer: '仅供参考。' };
  const generator = createChatGenerator({ model: 'test',
    client: queuedClient([
      toolCompletion('get_stock_data', reference),
      finalCompletion([{ type: 'stock_kline', stock: reference }]),
      finalCompletion([advice]),
    ], requests),
    marketDataProvider: async () => ({ stock: { code: '09988', name: '阿里巴巴-W', marketCode: '116' }, marketData: { kLines: [] } }),
  });
  const contents = await generator({ history: [], question: '阿里巴巴怎么样' });
  assert.equal(contents[0].type, 'stock_trade_timing');
  assert.equal(contents[0].data.stock.marketCode, '116');
  assert.equal(requests[2].tools, undefined);
  assert.match(requests[2].messages.at(-1).content, /stock_trade_timing/);
});

test('plain quote still gets text when model returns only cards after repair', async () => {
  const reference = { code: '600519', marketCode: '1' };
  const card = finalCompletion([{ type: 'stock_basic', stock: reference }]);
  const generator = createChatGenerator({ model: 'test',
    client: queuedClient([toolCompletion('get_stock_data', reference), card, card], []),
    marketDataProvider: async () => ({ stock: { code: '600519', name: '贵州茅台', latestPrice: '100' } }),
  });
  const contents = await generator({ history: [], question: '600519' });
  assert.equal(contents[0].type, 'text');
  assert.match(contents[0].data, /最新价 100/);
  assert.match(contents[0].data, /买入观察/);
  assert.equal(contents[1].type, 'stock_basic');
});

function blankCompletion() {
  return { choices: [{ finish_reason: 'stop', message: { role: 'assistant', content: ' '.repeat(18) } }] };
}

test('18 whitespace characters retry without JSON mode and retain stock tools', async () => {
  const ref = { code: '688836', marketCode: '1' }, requests = [];
  const generator = createChatGenerator({ model: 'test',
    client: queuedClient([
      blankCompletion(), toolCompletion('search_stocks', { keyword: '宇树科技' }),
      toolCompletion('get_stock_data', ref),
      finalCompletion([{ type: 'text', data: '已有行情，等待趋势确认。' }, { type: 'stock_basic', stock: ref }]),
    ], requests),
    stockSearchProvider: async () => ({ stocks: [{ ...ref, name: '宇树科技-W' }] }),
    marketDataProvider: async () => ({ stock: { code: ref.code, name: '宇树科技-W' } }),
  });
  const result = await generator({ history: [], question: '宇树科技行情' });
  assert.equal(requests[0].response_format.type, 'json_object');
  assert.equal(requests[1].response_format, undefined);
  assert.ok(requests[1].tools.some(t => t.function.name === 'search_stocks'));
  assert.equal(result[1].data.code, ref.code);
  assert.ok(!requests[1].messages.some(m => m.role === 'assistant' && !m.content?.trim()));
});

test('repeated empty replies preserve verified quotes without inventing advice', async () => {
  const ref = { code: '688836', marketCode: '1' };
  const stock = { code: ref.code, name: '宇树科技-W', latestPrice: '100' };
  const generator = createChatGenerator({ model: 'test',
    client: queuedClient([toolCompletion('get_stock_data', ref), blankCompletion(), blankCompletion()], []),
    marketDataProvider: async () => ({ stock }),
  });
  const contents = await generator({ history: [], question: '688836怎么样' });
  assert.match(contents[0].data, /暂未生成分析/);
  assert.deepEqual(contents[1], { type: 'stock_basic', data: stock });
  assert.equal(contents[2].data.buy.range, '暂不提供买入点位');
});

test('repeated empty replies before tools finish with honest nonempty message', async () => {
  const requests = [];
  const generator = createChatGenerator({ model: 'test', client: queuedClient([blankCompletion(), blankCompletion()], requests),
    marketDataProvider: async () => assert.fail('must not invent a lookup') });
  const contents = await generator({ history: [], question: '宇树科技怎么样' });
  assert.equal(requests.length, 2);
  assert.equal(contents.length, 1);
  assert.match(contents[0].data, /未能完成查询和分析/);
});

test('history uses JSON assistant messages and parses JSON inside explanatory fences', async () => {
  const requests = [];
  const generator = createChatGenerator({ model: 'test', client: queuedClient([
    { choices: [{ message: { role: 'assistant', content: '回答如下：\n```json\n{"contents":[{"type":"text","data":"你好"}]}\n```' } }] },
  ], requests) });
  const contents = await generator({ question: '你好', history: [
    { role: 'user', contents: [{ type: 'text', data: '你好' }] },
    { role: 'assistant', contents: [{ type: 'text', data: '上次的普通文本' }] },
  ] });
  assert.equal(JSON.parse(requests[0].messages[2].content).contents[0].data, '上次的普通文本');
  assert.equal(contents[0].data, '你好');
});

test('numeric or absent market IDs normalize only to verified stock references', async () => {
  const ref = { code: '688836', marketCode: '1' };
  const generator = createChatGenerator({ model: 'test', client: queuedClient([
    toolCompletion('get_stock_data', ref),
    finalCompletion([{ type: 'text', data: '已查询' },
      { type: 'stock_basic', stock: { code: ref.code, marketCode: 1 } },
      { type: 'stock_basic', stock: { code: ref.code } }]),
  ], []), marketDataProvider: async () => ({ stock: { code: ref.code, name: '宇树科技-W' } }) });
  const result = await generator({ history: [], question: '688836行情' });
  assert.equal(result[1].data.code, ref.code);
  assert.equal(result[2].data.code, ref.code);
});

test('plain non-JSON after whitespace cannot fabricate query failure or crash the turn', async () => {
  const plain = { choices: [{ message: { role: 'assistant', content: '未查询到宇树科技' } }] };
  const generator = createChatGenerator({ model: 'test', client: queuedClient([blankCompletion(), plain, plain], []) });
  const result = await generator({ history: [], question: '宇树科技怎么样' });
  assert.match(result[0].data, /格式异常/);
  assert.ok(!result[0].data.includes('未查询到宇树科技'));
});

test('current named stock is searched before model and overrides stale not-found history', async () => {
  const requests = [], ref = { code: '688836', marketCode: '1' };
  const gen = createChatGenerator({ model: 'test',
    client: queuedClient([toolCompletion('get_stock_data', ref),
      finalCompletion([{ type: 'text', data: '已取得行情。' }, { type: 'stock_basic', stock: ref }])], requests),
    stockSearchProvider: async ({ keyword }) => { assert.equal(keyword, '宇树科技'); return { stocks: [{ ...ref, name: '宇树科技-W' }] }; },
    marketDataProvider: async () => ({ stock: { code: ref.code, name: '宇树科技-W' } }),
  });
  const result = await gen({ question: '宇树科技行情', history: [{ role: 'assistant', contents: [{ type: 'text', data: '未查询到宇树科技' }] }] });
  assert.equal(requests[0].tool_choice.function.name, 'get_stock_data');
  assert.equal(result[1].data.code, ref.code);
});
