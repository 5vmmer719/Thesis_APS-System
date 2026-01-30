-- ====================================
-- 排产模块 (Schedule Module)
-- ====================================

-- 7.1 排产任务表 (sch_job)
DROP TABLE IF EXISTS sch_job;
CREATE TABLE IF NOT EXISTS sch_job (
  id BIGINT NOT NULL AUTO_INCREMENT,
  job_no VARCHAR(64) NOT NULL COMMENT '任务编号',
  horizon_start DATE NOT NULL COMMENT '排产起始日期',
  horizon_end DATE NOT NULL COMMENT '排产结束日期',
  scope_json JSON NULL COMMENT '范围配置(订单ID列表、工艺、产线等)',
  objective_json JSON NULL COMMENT '目标权重配置',
  constraint_json JSON NULL COMMENT '约束规则配置',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待运行1运行中2成功3失败4不可行',
  engine_trace VARCHAR(128) NULL COMMENT '引擎追踪ID',
  error_msg VARCHAR(1024) NULL COMMENT '错误信息',
  created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  created_by varchar(50) NULL COMMENT '创建人',
  updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  updated_by varchar(50) NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_job_no (job_no),
  KEY idx_status (status),
  KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排产任务';

-- 7.2 排产方案表 (sch_plan)
DROP TABLE IF EXISTS sch_plan;
CREATE TABLE IF NOT EXISTS sch_plan (
  id BIGINT NOT NULL AUTO_INCREMENT,
  job_id BIGINT NOT NULL COMMENT '任务ID',
  plan_no VARCHAR(64) NOT NULL COMMENT '方案编号',
  is_best TINYINT NOT NULL DEFAULT 0 COMMENT '是否最优方案',
  kpi_json JSON NULL COMMENT 'KPI汇总数据',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿1已发布2作废',
  remark VARCHAR(255) NULL COMMENT '备注',
  created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  created_by varchar(50) NULL COMMENT '创建人',
  updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  updated_by varchar(50) NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_job_best (job_id, is_best),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排产方案';

-- 7.3 排产方案明细表 (sch_plan_bucket)
DROP TABLE IF EXISTS sch_plan_bucket;
CREATE TABLE IF NOT EXISTS sch_plan_bucket (
  id BIGINT NOT NULL AUTO_INCREMENT,
  plan_id BIGINT NOT NULL COMMENT '方案ID',
  process_type TINYINT NOT NULL COMMENT '工艺类型:1冲压2焊装3涂装4总装',
  line_id BIGINT NOT NULL COMMENT '产线ID',
  biz_date DATE NOT NULL COMMENT '业务日期',
  shift_code VARCHAR(32) NOT NULL COMMENT '班次编码',
  prod_order_id BIGINT NOT NULL COMMENT '生产订单ID',
  seq_no INT NOT NULL COMMENT '班次内顺序号',
  qty INT NOT NULL COMMENT '分配数量(辆)',
  from_setup_key VARCHAR(64) NULL COMMENT '源换型键',
  to_setup_key VARCHAR(64) NULL COMMENT '目标换型键',
  setup_minutes INT NOT NULL DEFAULT 0 COMMENT '换型时间(分钟)',
  setup_cost DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '换型成本',
  remark VARCHAR(255) NULL COMMENT '备注',
  created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  created_by varchar(50) NULL COMMENT '创建人',
  updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  updated_by varchar(50) NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_plan_slot (plan_id, biz_date, shift_code, line_id),
  KEY idx_order (prod_order_id),
  KEY idx_process (process_type),
  KEY idx_line_date (line_id, biz_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方案明细(班次桶)';

-- 7.4 方案冲突表 (sch_plan_conflict)
DROP TABLE IF EXISTS sch_plan_conflict;
CREATE TABLE IF NOT EXISTS sch_plan_conflict (
  id BIGINT NOT NULL AUTO_INCREMENT,
  plan_id BIGINT NOT NULL COMMENT '方案ID',
  conflict_type VARCHAR(64) NOT NULL COMMENT '冲突类型',
  level TINYINT NOT NULL COMMENT '级别:1提示2警告3致命',
  object_type VARCHAR(32) NULL COMMENT '对象类型',
  object_id BIGINT NULL COMMENT '对象ID',
  message VARCHAR(1024) NOT NULL COMMENT '冲突消息',
  payload JSON NULL COMMENT '详细数据',
  created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  created_by varchar(50) NULL COMMENT '创建人',
  updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  updated_by varchar(50) NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_plan_level (plan_id, level),
  KEY idx_type (conflict_type),
  KEY idx_obj (object_type, object_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方案冲突/不可行原因';

-- 7.5 排产规则快照表 (sch_rule_snapshot)
DROP TABLE IF EXISTS sch_rule_snapshot;
CREATE TABLE IF NOT EXISTS sch_rule_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT,
  job_id BIGINT NOT NULL COMMENT '任务ID',
  rule_json JSON NOT NULL COMMENT '规则快照JSON',
  created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  created_by varchar(50) NULL COMMENT '创建人',
  updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  updated_by varchar(50) NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_job (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排产规则快照';

-- 7.6 引擎原始返回表 (sch_engine_result_raw)
DROP TABLE IF EXISTS sch_engine_result_raw;
CREATE TABLE IF NOT EXISTS sch_engine_result_raw (
  id BIGINT NOT NULL AUTO_INCREMENT,
  job_id BIGINT NOT NULL COMMENT '任务ID',
  raw_json JSON NOT NULL COMMENT '引擎原始返回JSON',
  created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  created_by varchar(50) NULL COMMENT '创建人',
  updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  updated_by varchar(50) NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_job (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='引擎原始返回(调试)';

-- 7.7 四大工艺串联绑定表 (sch_wip_link)
DROP TABLE IF EXISTS sch_wip_link;
CREATE TABLE IF NOT EXISTS sch_wip_link (
  id BIGINT NOT NULL AUTO_INCREMENT,
  prod_order_id BIGINT NOT NULL COMMENT '生产订单ID',
  process_type TINYINT NOT NULL COMMENT '工艺类型',
  plan_bucket_id BIGINT NULL COMMENT '方案桶ID',
  wo_id BIGINT NULL COMMENT '工单ID',
  created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  created_by varchar(50) NULL COMMENT '创建人',
  updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  updated_by varchar(50) NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_process (prod_order_id, process_type),
  KEY idx_bucket (plan_bucket_id),
  KEY idx_wo (wo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='四大工艺串联绑定';

-- 7.8 方案统计表 (sch_plan_stat)
DROP TABLE IF EXISTS sch_plan_stat;
CREATE TABLE IF NOT EXISTS sch_plan_stat (
  id BIGINT NOT NULL AUTO_INCREMENT,
  plan_id BIGINT NOT NULL COMMENT '方案ID',
  otd_rate DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT 'OTD准时交付率',
  setup_times INT NOT NULL DEFAULT 0 COMMENT '换型次数',
  avg_line_load DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '平均产线负荷率',
  created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  created_by varchar(50) NULL COMMENT '创建人',
  updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  updated_by varchar(50) NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_plan (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方案统计';

-- 7.9 手动调整日志表 (sch_manual_adjust_log)
DROP TABLE IF EXISTS sch_manual_adjust_log;
CREATE TABLE IF NOT EXISTS sch_manual_adjust_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  plan_id BIGINT NOT NULL COMMENT '方案ID',
  user_id BIGINT NOT NULL COMMENT '操作用户ID',
  change_json JSON NOT NULL COMMENT '变更内容JSON',
  remark VARCHAR(255) NULL COMMENT '备注',
  created_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  created_by varchar(50) NULL COMMENT '创建人',
  updated_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  updated_by varchar(50) NULL COMMENT '更新人',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_plan_time (plan_id, created_time),
  KEY idx_user_time (user_id, created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手动调整日志';

