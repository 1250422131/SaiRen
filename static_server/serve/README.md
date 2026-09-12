# SaiRen AI Analysis Service

Hono 服务。客户端只提交证券标识，服务端自行从行情源获取实时行情、公司资料和最近 120 根日 K 后生成分析，客户端提交的内容不会作为分析依据。

## 启动

需要 Node.js 22.13 或更高版本。服务使用 Hono 组织路由，使用 Drizzle ORM 访问 SQLite。

```bash
nvm use 22
cp .env.example .env
# 在 .env 中配置 DEEPSEEK_API_KEY
pnpm install
pnpm dev
```

分析记录会保存在 SQLite 数据库 `data/sairen.db`。首次启动时，如果存在旧版 `data/analyses.json`，服务会自动将其中的数据导入 SQLite；旧文件不会被删除，也不会被重复导入。

如需将数据库放在其他位置，可通过 `DATABASE_PATH` 覆盖默认路径。

数据库使用 WAL 模式，运行期间可能同时出现 `sairen.db-wal` 和 `sairen.db-shm` 文件，服务正常关闭或执行 checkpoint 后数据仍归档在 `sairen.db` 中。数据库文件和运行时辅助文件均已加入 `.gitignore`。

同一股票在完成后 1 小时内会直接返回缓存；若已有同股票任务处于 `analyzing`，重复调用同一个接口会等待已有任务，最长等待时间由 `REQUEST_WAIT_TIMEOUT_MS` 控制，超时则返回 `202`，客户端可继续使用同一接口轮询。

## 接口

`POST /v1/stock-analyses`

```json
{
  "code": "600519",
  "marketCode": "1"
}
```

`marketCode` 使用东方财富市场号：`1` 为沪市，`0` 为深市或北交所。服务端会验证并使用此标识自行拉取实时行情、F10 公司资料和日 K；其中公司资料或 K 线拉取失败时会降级为缺省数据，实时行情不可用时返回错误。

所有接口均返回 `{ "code": 数字, "data": 对象或 null, "msg": "说明" }`。成功时 `code` 为 `0`；失败时 `data` 为 `null`，`msg` 说明失败原因。`data` 中的分析结果包含 `status`、缓存标识与 `analysis`。`analysis` 固定包含结论、买卖观察区间、趋势、风险、三到六条信号和行情总结，能够直接映射到 Android 的 AI 分析卡片。

## AI 聊天

- `GET /v1/chat/history?beforeId=<消息ID>&limit=50`：按时间正序返回当前用户的历史消息，`limit` 最大为 100；通过返回的 `nextBeforeId` 继续向前分页。
- `POST /v1/chat/messages`：提交 `{ "content": "消息", "requestId": "可选 UUID" }`，同步返回本轮用户消息和 AI 消息。重复提交相同 `requestId` 和内容不会重复生成。

聊天接口需要 Bearer token。每条消息通过 `role` 区分 `user` 和 `assistant`，`contents` 是可组合数组，支持 `text`、`markdown`、`stock_basic`、`stock_kline`、`stock_trade_timing`、`stock_company`。直接发送股票名称、简称或代码即可查询 A 股、港股、美股，例如“平安银行”“阿里巴巴股票怎么样”“9988”“BABA”。按名称查询时，AI 先调用 `search_stocks`（东方财富搜索接口）得到代码和市场号，再调用 `get_stock_data` 获取行情；多个候选无法唯一确定时会请用户选择，没有匹配时会明确告知。代码也支持在聊天中使用 `sh600519`、`600519.SH` 等形式。名称查询的证券引用必须来自本轮搜索，后端拒绝模型猜测或从旧对话带入未经确认的代码。精确代码搜索优先返回该代码；同名公司的港美股可分别展示，卡片标明市场和币种。

行情使用东方财富；A 股失败时尝试腾讯行情备用源；备用源的 GBK 文本会解码，成交额和市值统一转换为元。港美股日 K 使用东方财富历史行情接口，不调用 A 股 F10 或新浪 A 股 K 线接口；港美股公司资料暂缺。工具失败会返回可供 AI 更正查询或向用户说明的错误，不允许回填未经成功查询的数据卡片。

涉及具体股票时，AI 会调用服务端行情工具；股票基本行情、企业资料和日 K 数据由服务端直接回填到卡片，模型不能自行填写这些数据。

聊天股票查询必须附文字解读或买卖建议；询问“怎么样”、分析或买卖点位时，每只已查询股票返回 `stock_trade_timing` 点位建议卡片，包含买入区间、卖出/减仓区间、依据、风险和币种。遗漏时自动补写一次，仍缺失则明确展示暂不提供点位及原因，不编造交易价格。

JSON 模式返回空白时，会单独重试一次并关闭 `response_format`，仍严格校验 JSON 和卡片来源；空白重试不会提前关闭股票工具。若再次空白，保留已查到的行情并明确提示分析未生成；没有行情时不生成股票卡片。日志记录去空白后的长度、是否重试和已查询股票数。

## 登录与注册

- `POST /v1/auth/register`：提交 `{ "username": "用户名", "password": "密码" }`，注册成功后直接返回 token。
- `POST /v1/auth/login`：提交相同结构，登录成功后返回 token。
- `GET /v1/auth/me`：请求头携带 `Authorization: Bearer <token>`，用于校验登录状态。

用户名长度为 3～32 个字符，只允许文字、数字、下划线和短横线；密码长度为 6～72 个字符。密码使用带随机盐的 scrypt 哈希保存，数据库不保存明文密码；数据库中也只保存 token 的 SHA-256 摘要。token 默认有效期为 30 天，可通过 `AUTH_TOKEN_TTL_SECONDS` 修改。

## Neon 数据库迁移

生产环境使用 `DATABASE_URL` 连接 Neon，并在迁移工具中执行 `migrations/0001_init.sql`。本地开发继续使用 SQLite。
