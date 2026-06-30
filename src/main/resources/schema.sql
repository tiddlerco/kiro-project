-- =====================================================================
-- schema.sql —— H2 持久化基座建表脚本（设计模式实战示例工程）
-- ---------------------------------------------------------------------
-- 用途：H2 内存库（MySQL 兼容模式 MODE=MySQL）启动时自动建表。
-- 适配：仅状态、命令、享元三个模式需要持久化，共四张表。
-- 约定：
--   1. 主键统一使用 BIGINT AUTO_INCREMENT（H2 MySQL 模式兼容 IDENTITY）。
--   2. 字段命名采用下划线风格，与 MyBatis map-underscore-to-camel-case 对应。
--   3. 全部使用 CREATE TABLE IF NOT EXISTS，避免重复初始化报错。
--   4. 时间字段默认填充 CURRENT_TIMESTAMP，业务无需显式赋值。
-- 字段定义严格依据 design.md「表结构与实体设计」章节。
-- =====================================================================

-- ---------------------------------------------------------------------
-- biz_order：订单表（状态模式 State 的持久化载体）
-- 状态机基于 status 字段流转：CREATED → PAID → SHIPPED → COMPLETED，
-- CREATED/PAID 可流转至 CANCELLED；非法流转由应用层拒绝并保持原状态。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_order (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,           -- 主键
    order_no    VARCHAR(32)   NOT NULL,                             -- 订单号（业务唯一）
    amount      DECIMAL(12,2) NOT NULL,                             -- 订单金额
    status      VARCHAR(16)   NOT NULL DEFAULT 'CREATED',           -- 订单状态：CREATED/PAID/SHIPPED/COMPLETED/CANCELLED
    create_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- 创建时间
    update_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- 更新时间（状态流转时刷新）
    CONSTRAINT uk_biz_order_order_no UNIQUE (order_no)              -- 订单号唯一约束
);

-- ---------------------------------------------------------------------
-- biz_product：商品表（命令模式 Command 的操作目标）
-- 采用逻辑删除（status=0）而非物理删除，以便 DeleteProductCommand 撤销恢复。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_product (
    id           BIGINT        AUTO_INCREMENT PRIMARY KEY,          -- 主键
    product_name VARCHAR(64)   NOT NULL,                            -- 商品名称
    price        DECIMAL(12,2) NOT NULL,                            -- 商品价格
    status       TINYINT       NOT NULL DEFAULT 1,                  -- 状态：1 正常 / 0 已删除（逻辑删除）
    create_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP   -- 创建时间
);

-- ---------------------------------------------------------------------
-- sys_command_history：命令历史表（命令模式 Command 的可追溯历史）
-- 每执行一条命令落库一行，保存前后快照（JSON）；撤销时读取 before_snapshot 恢复数据，
-- 并将该行 status 置为 0（已撤销）。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_command_history (
    id              BIGINT         AUTO_INCREMENT PRIMARY KEY,         -- 主键
    command_type    VARCHAR(32)    NOT NULL,                          -- 命令类型：ADD/UPDATE/DELETE
    target_id       BIGINT,                                           -- 目标商品 id
    before_snapshot VARCHAR(1024),                                    -- 执行前数据快照（JSON），用于撤销
    after_snapshot  VARCHAR(1024),                                    -- 执行后数据快照（JSON）
    status          TINYINT        NOT NULL DEFAULT 1,                -- 状态：1 已执行 / 0 已撤销
    operator        VARCHAR(32),                                      -- 操作人
    create_time     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP -- 创建时间
);

-- ---------------------------------------------------------------------
-- risk_rule_dict：风控规则字典表（享元模式 Flyweight 的内蕴状态来源）
-- rule_code 为享元内蕴状态键，工厂按 rule_code 缓存并复用同一共享实例。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS risk_rule_dict (
    id        BIGINT        AUTO_INCREMENT PRIMARY KEY,              -- 主键
    rule_code VARCHAR(32)   NOT NULL,                                -- 规则编码（享元内蕴状态键，唯一）
    rule_name VARCHAR(64)   NOT NULL,                                -- 规则名称
    threshold DECIMAL(12,2),                                         -- 阈值
    enabled   TINYINT       NOT NULL DEFAULT 1,                      -- 是否启用：1 启用 / 0 停用
    CONSTRAINT uk_risk_rule_dict_rule_code UNIQUE (rule_code)        -- 规则编码唯一约束
);
