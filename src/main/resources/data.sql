-- =====================================================================
-- data.sql —— H2 持久化基座初始化演示数据（设计模式实战示例工程）
-- ---------------------------------------------------------------------
-- 用途：H2 内存库建表（schema.sql）后自动写入演示数据。
-- 约定：
--   1. 为保证可重复初始化（幂等），每张预置表先 DELETE 清理再 INSERT。
--   2. 预置行显式指定主键 id，便于演示入口按固定 id 触发；
--      随后用 ALTER TABLE ... RESTART WITH 100 抬高自增起点，
--      避免后续代码自动插入（如 ADD 命令）与预置 id 冲突。
--   3. threshold/price/amount 统一以两位小数书写，匹配 DECIMAL(12,2)。
-- =====================================================================

-- 幂等清理：清空四张表的历史数据，确保多次初始化结果一致
DELETE FROM sys_command_history;
DELETE FROM biz_order;
DELETE FROM biz_product;
DELETE FROM risk_rule_dict;

-- ---------------------------------------------------------------------
-- risk_rule_dict：风控规则字典（享元模式核心演示数据）
-- 预置 5 条启用规则 + 1 条停用规则，停用项用于演示 selectAllEnabled 的过滤效果。
-- 规则编码取自风控业务术语，与责任链风控校验节点（金额/频次/黑名单等）相呼应。
-- ---------------------------------------------------------------------
INSERT INTO risk_rule_dict (id, rule_code, rule_name, threshold, enabled) VALUES
(1, 'AMOUNT_LIMIT',       '单笔交易金额上限',       10000.00, 1),
(2, 'DAILY_AMOUNT_LIMIT', '单日累计交易金额上限',   50000.00, 1),
(3, 'FREQUENCY_LIMIT',    '单位时间交易频次上限',      10.00, 1),
(4, 'BLACKLIST',          '黑名单账户拦截',             0.00, 1),
(5, 'RISK_SCORE_LIMIT',   '风险评分拦截阈值',          80.00, 1),
(6, 'NIGHT_TRADE_LIMIT',  '夜间大额交易限制（停用）', 20000.00, 0);
-- 抬高自增起点，避免与预置 id 冲突
ALTER TABLE risk_rule_dict ALTER COLUMN id RESTART WITH 100;

-- ---------------------------------------------------------------------
-- biz_order：订单（状态模式演示数据）
-- 覆盖全部 5 种状态，便于演示合法流转（如 CREATED→PAID）与非法流转（如 COMPLETED→PAID）。
-- create_time/update_time 采用默认值 CURRENT_TIMESTAMP，无需显式赋值。
-- ---------------------------------------------------------------------
INSERT INTO biz_order (id, order_no, amount, status) VALUES
(1, 'ORD20240001',  199.00, 'CREATED'),
(2, 'ORD20240002',  599.50, 'PAID'),
(3, 'ORD20240003', 1299.00, 'SHIPPED'),
(4, 'ORD20240004',   89.90, 'COMPLETED'),
(5, 'ORD20240005',  320.00, 'CANCELLED');
-- 抬高自增起点，预留后续扩展
ALTER TABLE biz_order ALTER COLUMN id RESTART WITH 100;

-- ---------------------------------------------------------------------
-- biz_product：商品（命令模式演示数据）
-- 预置 3 件正常商品，供 UpdateProductCommand / DeleteProductCommand 直接操作；
-- ADD 命令新增的商品从自增起点 100 开始，不与预置 id 冲突。
-- ---------------------------------------------------------------------
INSERT INTO biz_product (id, product_name, price, status) VALUES
(1, '机械键盘',   399.00, 1),
(2, '无线鼠标',   129.00, 1),
(3, '4K 显示器', 1899.00, 1);
-- 抬高自增起点，避免后续 ADD 命令自增插入与预置 id 冲突
ALTER TABLE biz_product ALTER COLUMN id RESTART WITH 100;
