-- =====================================================
-- APS 排产系统 - 计划模块 (Plan Module)
-- 包含: MPS主生产计划、MRP物料需求计划
-- MySQL 8.0+, InnoDB, utf8mb4
-- =====================================================

-- 6.5 MPS主表
CREATE TABLE IF NOT EXISTS plan_mps (
    id BIGINT NOT NULL COMMENT 'PK 雪花ID',
    mps_no VARCHAR(64) NOT NULL COMMENT 'MPS计划编号',
    start_date DATE NOT NULL COMMENT '计划开始日期',
    end_date DATE NOT NULL COMMENT '计划结束日期',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-审批中, 2-已批准, 3-关闭',
    remark VARCHAR(255) NULL COMMENT '备注',
    created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    created_by VARCHAR(50) NULL COMMENT '创建人',
    updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    updated_by VARCHAR(50) NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mps_no (mps_no),
    KEY idx_range (start_date, end_date),
    KEY idx_status (status),
    KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MPS主生产计划';

-- 6.6 MPS明细
CREATE TABLE IF NOT EXISTS plan_mps_item (
    id BIGINT NOT NULL COMMENT 'PK 雪花ID',
    mps_id BIGINT NOT NULL COMMENT 'MPS主表ID',
    biz_date DATE NOT NULL COMMENT '计划日期',
    model_id BIGINT NOT NULL COMMENT '车型ID',
    qty INT NOT NULL COMMENT '计划数量(辆)',
    created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    created_by VARCHAR(50) NULL COMMENT '创建人',
    updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    updated_by VARCHAR(50) NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
    PRIMARY KEY (id),
    KEY idx_mps (mps_id, biz_date),
    KEY idx_model_date (model_id, biz_date),
    KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MPS明细';

-- 6.7 MRP主表
CREATE TABLE IF NOT EXISTS plan_mrp (
    id BIGINT NOT NULL COMMENT 'PK 雪花ID',
    mrp_no VARCHAR(64) NOT NULL COMMENT 'MRP计划编号',
    mps_id BIGINT NOT NULL COMMENT 'MPS主表ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-生成中, 1-完成, 2-失败',
    result_payload JSON NULL COMMENT '运算结果数据',
    remark VARCHAR(255) NULL COMMENT '备注',
    created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    created_by VARCHAR(50) NULL COMMENT '创建人',
    updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    updated_by VARCHAR(50) NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mrp_no (mrp_no),
    KEY idx_mps (mps_id),
    KEY idx_status (status),
    KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MRP物料需求计划';

-- 6.8 MRP明细
CREATE TABLE IF NOT EXISTS plan_mrp_item (
    id BIGINT NOT NULL COMMENT 'PK 雪花ID',
    mrp_id BIGINT NOT NULL COMMENT 'MRP主表ID',
    item_code VARCHAR(64) NOT NULL COMMENT '物料编码',
    req_date DATE NOT NULL COMMENT '需求日期',
    req_qty DECIMAL(18,6) NOT NULL DEFAULT 0 COMMENT '需求数量',
    supply_qty DECIMAL(18,6) NOT NULL DEFAULT 0 COMMENT '供应数量',
    shortage_qty DECIMAL(18,6) NOT NULL DEFAULT 0 COMMENT '缺口数量',
    created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    created_by VARCHAR(50) NULL COMMENT '创建人',
    updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    updated_by VARCHAR(50) NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否, 1-是',
    PRIMARY KEY (id),
    KEY idx_mrp_item (mrp_id, item_code),
    KEY idx_date (req_date),
    KEY idx_item_code (item_code),
    KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MRP明细';

-- =====================================================
-- 索引说明
-- =====================================================
-- plan_mps:
--   - uk_mps_no: 计划编号唯一索引
--   - idx_range: 日期范围查询索引
--   - idx_status: 状态查询索引
--
-- plan_mps_item:
--   - idx_mps: MPS主表关联查询
--   - idx_model_date: 车型+日期查询
--
-- plan_mrp:
--   - uk_mrp_no: 计划编号唯一索引
--   - idx_mps: MPS关联查询
--   - idx_status: 状态查询索引
--
-- plan_mrp_item:
--   - idx_mrp_item: MRP主表+物料查询
--   - idx_date: 日期查询
--   - idx_item_code: 物料编码查询
-- =====================================================

