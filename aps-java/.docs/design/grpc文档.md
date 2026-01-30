# APS 排产系统 gRPC 接口文档

> **版本**: v1.0  
> **引擎**: Rust APS Engine  
> **协议**: gRPC (Protocol Buffers 3)  
> **服务名**: `aps.v1.ApsService`

---

## 目录

1. [概述](#1-概述)
2. [服务端点](#2-服务端点)
3. [核心接口详解](#3-核心接口详解)
4. [数据模型](#4-数据模型)
5. [错误处理](#5-错误处理)
6. [性能与限制](#6-性能与限制)
7. [使用示例](#7-使用示例)
8. [最佳实践](#8-最佳实践)

---

## 1. 概述

### 1.1 接口设计理念

APS排产引擎提供两种调用模式：

- **同步模式** (`Solve`): 适合小规模、快速响应场景（≤100辆车，≤5秒）
- **异步模式** (`SubmitJob` + `GetJobStatus`): 适合大规模、长时运算场景（>100辆车，>5秒）

### 1.2 核心能力

- ✅ **四大工艺排产**: 冲压、焊装、涂装、总装全流程调度
- ✅ **换型优化**: 支持模具切换、颜色切换、夹具切换、配置切换
- ✅ **多目标优化**: 交期、切换成本、资源利用率、能耗、排放
- ✅ **约束满足**: 产能限制、能耗限制、排放限制、物料齐套
- ✅ **算法可选**: Baseline（贪婪）、SA（模拟退火）、Hybrid（混合策略）

### 1.3 技术栈

- **传输协议**: HTTP/2 (gRPC)
- **序列化**: Protocol Buffers 3
- **并发模型**: Tokio 异步运行时
- **求解器**: Rust 高性能算法引擎

---

## 2. 服务端点

### 2.1 服务定义

```protobuf
service ApsService {
    // 同步求解（阻塞等待结果）
    rpc Solve(SolveRequest) returns (SolveResponse);
    
    // 异步任务提交
    rpc SubmitJob(SubmitJobRequest) returns (SubmitJobResponse);
    
    // 查询任务状态
    rpc GetJobStatus(GetJobStatusRequest) returns (GetJobStatusResponse);
    
    // 列出所有任务
    rpc ListJobs(ListJobsRequest) returns (ListJobsResponse);
}
```

### 2.2 默认配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 监听地址 | `0.0.0.0:50051` | gRPC服务端口 |
| 最大消息大小 | 4MB | 单次请求/响应上限 |
| 连接超时 | 30s | 建立连接超时 |
| 请求超时 | 无限制 | 由客户端控制 |

---

## 3. 核心接口详解

### 3.1 Solve - 同步求解

#### 3.1.1 接口签名

```protobuf
rpc Solve(SolveRequest) returns (SolveResponse);
```

#### 3.1.2 请求参数 (SolveRequest)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| request_id | string | 是 | 请求唯一标识（建议UUID） |
| plan_start_epoch_ms | int64 | 是 | 排产起始时间（毫秒时间戳） |
| jobs | Job[] | 是 | 待排产订单列表（≤500） |
| params | SolveParams | 否 | 求解参数（缺省使用默认值） |

**Job 结构**:

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| vin | string | 是 | 车辆识别码/订单号 | "VIN001" |
| due_epoch_ms | int64 | 是 | 交期（毫秒时间戳） | 1704110400000 |
| stamping_minutes | int32 | 否 | 冲压工时（分钟） | 15 |
| welding_minutes | int32 | 否 | 焊装工时（分钟） | 20 |
| painting_minutes | int32 | 否 | 涂装工时（分钟） | 30 |
| assemble_minutes | int32 | 是 | 总装工时（分钟） | 60 |
| mold_code | string | 否 | 模具编码（冲压换型键） | "MOLD_A" |
| welding_fixture | string | 否 | 焊装夹具（焊装换型键） | "FIX_B" |
| color | string | 是 | 颜色编码（涂装换型键） | "RED" |
| config | string | 是 | 配置编码（总装换型键） | "BASE" |
| energy_score | double | 否 | 能耗分值 | 100.0 |
| emission_score | double | 否 | 排放分值 | 50.0 |

**SolveParams 结构**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| algorithm | string | 否 | "baseline" | 算法选择: baseline/sa/hybrid |
| time_budget_sec | int32 | 否 | 5 | 求解时间预算（秒，1-60） |
| seed | int64 | 否 | 42 | 随机种子（可复现） |
| weights | Weights | 否 | 见下表 | 目标权重 |
| limits | Limits | 否 | 见下表 | 资源限制 |

**Weights 结构**（目标权重）:

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| tardiness | double | 10.0 | 延迟惩罚权重 |
| color_change | double | 50.0 | 颜色切换权重 |
| config_change | double | 30.0 | 配置切换权重 |
| energy_excess | double | 2.0 | 能耗超限权重 |
| emission_excess | double | 3.0 | 排放超限权重 |
| material_shortage | double | 0.0 | 物料短缺权重 |

**Limits 结构**（资源限制）:

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| max_energy_per_shift | double | 5000.0 | 班次能耗上限 |
| max_emission_per_shift | double | 2500.0 | 班次排放上限 |

#### 3.1.3 响应结果 (SolveResponse)

| 字段 | 类型 | 说明 |
|------|------|------|
| request_id | string | 请求ID（回显） |
| summary | KpiSummary | 优化方案KPI |
| baseline_summary | KpiSummary | 基准方案KPI（对比用） |
| order | string[] | 排产顺序（VIN列表） |
| schedule | ScheduleItem[] | 简化调度表（每车一条） |
| detailed_schedule | ScheduleItem[] | 详细调度表（每工序一条） |
| violations | ShiftViolation[] | 约束违反列表 |
| convergence | ConvergencePoint[] | 收敛曲线 |
| engine_version | string | 引擎版本号 |
| warnings | string[] | 警告信息 |

**KpiSummary 结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| cost | double | 总成本（目标函数值） |
| total_tardiness_min | int64 | 总延迟时间（分钟） |
| max_tardiness_min | int64 | 最大延迟时间（分钟） |
| color_changes | int32 | 颜色切换次数 |
| config_changes | int32 | 配置切换次数 |
| energy_excess | double | 能耗超限总量 |
| emission_excess | double | 排放超限总量 |
| material_shortage | double | 物料短缺总量 |
| elapsed_ms | int64 | 求解耗时（毫秒） |

**ScheduleItem 结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| seq | int32 | 全局顺序号 |
| vin | string | 车辆识别码 |
| process_type | int32 | 工艺类型（1冲压/2焊装/3涂装/4总装） |
| line_id | int64 | 产线ID |
| seq_in_shift | int32 | 班次内顺序 |
| start_epoch_ms | int64 | 开始时间（毫秒时间戳） |
| end_epoch_ms | int64 | 结束时间（毫秒时间戳） |
| due_epoch_ms | int64 | 交期（毫秒时间戳） |
| tardiness_min | int64 | 延迟时间（分钟，0表示准时） |
| color | string | 颜色编码 |
| config | string | 配置编码 |
| shift_id | string | 班次ID（如"D1"） |

**ShiftViolation 结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| shift_id | string | 班次ID |
| vtype | string | 违反类型（ENERGY/EMISSION/MATERIAL） |
| excess | double | 超限量 |

**ConvergencePoint 结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| t_ms | int64 | 时间点（毫秒） |
| best_cost | double | 当前最优成本 |

#### 3.1.4 调用示例

**请求示例**（JSON表示）:

```json
{
  "request_id": "req-20260129-001",
  "plan_start_epoch_ms": 1704106800000,
  "jobs": [
    {
      "vin": "VIN001",
      "due_epoch_ms": 1704110400000,
      "stamping_minutes": 15,
      "welding_minutes": 20,
      "painting_minutes": 30,
      "assemble_minutes": 60,
      "mold_code": "MOLD_A",
      "welding_fixture": "FIX_B",
      "color": "RED",
      "config": "BASE",
      "energy_score": 100.0,
      "emission_score": 50.0
    },
    {
      "vin": "VIN002",
      "due_epoch_ms": 1704114000000,
      "stamping_minutes": 15,
      "welding_minutes": 20,
      "painting_minutes": 30,
      "assemble_minutes": 60,
      "mold_code": "MOLD_A",
      "welding_fixture": "FIX_B",
      "color": "BLUE",
      "config": "PREMIUM",
      "energy_score": 120.0,
      "emission_score": 60.0
    }
  ],
  "params": {
    "algorithm": "sa",
    "time_budget_sec": 10,
    "seed": 42,
    "weights": {
      "tardiness": 10.0,
      "color_change": 50.0,
      "config_change": 30.0,
      "energy_excess": 2.0,
      "emission_excess": 3.0,
      "material_shortage": 0.0
    },
    "limits": {
      "max_energy_per_shift": 5000.0,
      "max_emission_per_shift": 2500.0
    }
  }
}
```

**响应示例**（部分字段）:

```json
{
  "request_id": "req-20260129-001",
  "summary": {
    "cost": 150.5,
    "total_tardiness_min": 0,
    "max_tardiness_min": 0,
    "color_changes": 1,
    "config_changes": 1,
    "energy_excess": 0.0,
    "emission_excess": 0.0,
    "material_shortage": 0.0,
    "elapsed_ms": 8523
  },
  "baseline_summary": {
    "cost": 180.0,
    "total_tardiness_min": 0,
    "max_tardiness_min": 0,
    "color_changes": 1,
    "config_changes": 1,
    "energy_excess": 0.0,
    "emission_excess": 0.0,
    "material_shortage": 0.0,
    "elapsed_ms": 5
  },
  "order": ["VIN001", "VIN002"],
  "schedule": [
    {
      "seq": 1,
      "vin": "VIN001",
      "process_type": 4,
      "line_id": 1,
      "seq_in_shift": 1,
      "start_epoch_ms": 1704106800000,
      "end_epoch_ms": 1704110400000,
      "due_epoch_ms": 1704110400000,
      "tardiness_min": 0,
      "color": "RED",
      "config": "BASE",
      "shift_id": "D1"
    }
  ],
  "detailed_schedule": [
    {
      "seq": 1,
      "vin": "VIN001",
      "process_type": 1,
      "line_id": 101,
      "start_epoch_ms": 1704106800000,
      "end_epoch_ms": 1704107700000,
      "shift_id": "D1"
    },
    {
      "seq": 2,
      "vin": "VIN001",
      "process_type": 2,
      "line_id": 201,
      "start_epoch_ms": 1704107700000,
      "end_epoch_ms": 1704108900000,
      "shift_id": "D1"
    }
  ],
  "violations": [],
  "convergence": [
    {"t_ms": 0, "best_cost": 200.0},
    {"t_ms": 1000, "best_cost": 180.0},
    {"t_ms": 5000, "best_cost": 150.5}
  ],
  "engine_version": "0.1.0",
  "warnings": []
}
```

#### 3.1.5 错误码

| gRPC Status | 说明 | 解决方案 |
|-------------|------|----------|
| INVALID_ARGUMENT | 参数错误（空订单/超限） | 检查jobs非空且≤500 |
| DEADLINE_EXCEEDED | 超时 | 增加客户端超时或减少订单量 |
| RESOURCE_EXHAUSTED | 资源耗尽 | 降低并发或使用异步模式 |
| INTERNAL | 内部错误 | 查看服务端日志 |

---

### 3.2 SubmitJob - 异步任务提交

#### 3.2.1 接口签名

```protobuf
rpc SubmitJob(SubmitJobRequest) returns (SubmitJobResponse);
```

#### 3.2.2 请求参数 (SubmitJobRequest)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| request | SolveRequest | 是 | 求解请求（同Solve） |

#### 3.2.3 响应结果 (SubmitJobResponse)

| 字段 | 类型 | 说明 |
|------|------|------|
| job_id | string | 任务ID（UUID格式） |
| message | string | 提交结果消息 |

#### 3.2.4 调用示例

**请求**:

```json
{
  "request": {
    "request_id": "req-20260129-002",
    "plan_start_epoch_ms": 1704106800000,
    "jobs": [/* ... */],
    "params": {/* ... */}
  }
}
```

**响应**:

```json
{
  "job_id": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Job submitted successfully"
}
```

---

### 3.3 GetJobStatus - 查询任务状态

#### 3.3.1 接口签名

```protobuf
rpc GetJobStatus(GetJobStatusRequest) returns (GetJobStatusResponse);
```

#### 3.3.2 请求参数 (GetJobStatusRequest)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| job_id | string | 是 | 任务ID（由SubmitJob返回） |

#### 3.3.3 响应结果 (GetJobStatusResponse)

| 字段 | 类型 | 说明 |
|------|------|------|
| job_id | string | 任务ID |
| status | string | 状态: QUEUED/RUNNING/COMPLETED/FAILED |
| created_at | int64 | 创建时间（Unix秒） |
| updated_at | int64 | 更新时间（Unix秒） |
| result | SolveResponse | 求解结果（仅COMPLETED时有值） |
| error_message | string | 错误信息（仅FAILED时有值） |
| engine_version | string | 引擎版本 |

#### 3.3.4 调用示例

**请求**:

```json
{
  "job_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**响应（运行中）**:

```json
{
  "job_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "RUNNING",
  "created_at": 1704106800,
  "updated_at": 1704106810,
  "engine_version": "0.1.0"
}
```

**响应（已完成）**:

```json
{
  "job_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "created_at": 1704106800,
  "updated_at": 1704106860,
  "result": {
    "request_id": "req-20260129-002",
    "summary": {/* ... */},
    "schedule": [/* ... */]
  },
  "engine_version": "0.1.0"
}
```

---

### 3.4 ListJobs - 列出所有任务

#### 3.4.1 接口签名

```protobuf
rpc ListJobs(ListJobsRequest) returns (ListJobsResponse);
```

#### 3.4.2 请求参数 (ListJobsRequest)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | int32 | 否 | 返回数量限制（0表示不限制） |

#### 3.4.3 响应结果 (ListJobsResponse)

| 字段 | 类型 | 说明 |
|------|------|------|
| jobs | JobInfo[] | 任务信息列表（按更新时间倒序） |

**JobInfo 结构**:

| 字段 | 类型 | 说明 |
|------|------|------|
| job_id | string | 任务ID |
| status | string | 状态: QUEUED/RUNNING/COMPLETED/FAILED |
| created_at | int64 | 创建时间（Unix秒） |
| updated_at | int64 | 更新时间（Unix秒） |

#### 3.4.4 调用示例

**请求**:

```json
{
  "limit": 10
}
```

**响应**:

```json
{
  "jobs": [
    {
      "job_id": "550e8400-e29b-41d4-a716-446655440000",
      "status": "COMPLETED",
      "created_at": 1704106800,
      "updated_at": 1704106860
    },
    {
      "job_id": "660e8400-e29b-41d4-a716-446655440001",
      "status": "RUNNING",
      "created_at": 1704106900,
      "updated_at": 1704106910
    }
  ]
}
```

---

## 4. 数据模型

### 4.1 工艺类型枚举 (ProcessType)

| 值 | 名称 | 说明 |
|----|------|------|
| 1 | Stamping | 冲压 |
| 2 | Welding | 焊装 |
| 3 | Painting | 涂装 |
| 4 | Assembly | 总装 |

### 4.2 算法选择 (algorithm)

| 值 | 名称 | 特点 | 适用场景 |
|----|------|------|----------|
| baseline | 基准算法 | 贪婪策略，快速 | 快速预览、基准对比 |
| sa | 模拟退火 | 全局优化，质量高 | 生产计划、精细排产 |
| hybrid | 混合策略 | 平衡速度与质量 | 通用场景 |

### 4.3 约束违反类型 (vtype)

| 值 | 说明 | 严重级别 |
|----|------|----------|
| ENERGY | 能耗超限 | 警告 |
| EMISSION | 排放超限 | 警告 |
| MATERIAL | 物料短缺 | 错误 |
| CAPACITY | 产能超限 | 错误 |

---

## 5. 错误处理

### 5.1 gRPC 标准错误码

| Status Code | HTTP映射 | 说明 | 示例 |
|-------------|----------|------|------|
| OK (0) | 200 | 成功 | - |
| INVALID_ARGUMENT (3) | 400 | 参数错误 | 订单列表为空 |
| NOT_FOUND (5) | 404 | 资源不存在 | 任务ID不存在 |
| RESOURCE_EXHAUSTED (8) | 429 | 资源耗尽 | 并发数超限 |
| INTERNAL (13) | 500 | 内部错误 | 算法崩溃 |
| DEADLINE_EXCEEDED (4) | 504 | 超时 | 客户端超时 |

### 5.2 业务错误处理

引擎通过 `warnings` 字段返回非致命警告：

```json
{
  "warnings": [
    "Time budget 100s truncated to 60s",
    "Unknown algorithm 'genetic', falling back to baseline"
  ]
}
```

### 5.3 错误重试策略

| 错误类型 | 是否重试 | 重试策略 |
|----------|----------|----------|
| INVALID_ARGUMENT | 否 | 修正参数 |
| NOT_FOUND | 否 | 检查任务ID |
| RESOURCE_EXHAUSTED | 是 | 指数退避（1s, 2s, 4s） |
| INTERNAL | 是 | 最多重试3次 |
| DEADLINE_EXCEEDED | 是 | 增加超时或减少负载 |

---

## 6. 性能与限制

### 6.1 系统限制

| 限制项 | 值 | 说明 |
|--------|-----|------|
| 最大订单数 | 500 | 单次请求订单上限 |
| 最大时间预算 | 60秒 | 超出会被截断 |
| 最小时间预算 | 1秒 | 低于1秒会使用默认5秒 |
| 最大消息大小 | 4MB | gRPC消息上限 |
| 任务保留时间 | 24小时 | 内存中任务自动清理 |

### 6.2 性能基准

**测试环境**: Apple M1 Pro, 10核, 16GB RAM

| 场景 | 订单数 | 算法 | 时间预算 | 平均耗时 | P95耗时 |
|------|--------|------|----------|----------|---------|
| 快速预览 | 10 | baseline | 1s | 50ms | 80ms |
| 小规模 | 50 | sa | 5s | 3.2s | 4.8s |
| 中等规模 | 100 | sa | 10s | 8.5s | 9.8s |
| 大规模 | 200 | hybrid | 30s | 25s | 28s |

### 6.3 并发能力

- **同步模式**: 建议并发 ≤ CPU核心数（避免阻塞）
- **异步模式**: 支持高并发提交（>100 QPS），后台队列执行

---

## 7. 使用示例

### 7.1 Rust 客户端示例

```rust
use tonic::Request;
use aps_engine::gen::aps::{
    aps_service_client::ApsServiceClient,
    SolveRequest, SolveParams, Weights, Limits, Job,
};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // 连接服务
    let mut client = ApsServiceClient::connect("http://127.0.0.1:50051").await?;

    // 构造请求
    let request = Request::new(SolveRequest {
        request_id: "demo-001".to_string(),
        plan_start_epoch_ms: 1704106800000,
        jobs: vec![
            Job {
                vin: "VIN001".to_string(),
                due_epoch_ms: 1704110400000,
                stamping_minutes: 15,
                welding_minutes: 20,
                painting_minutes: 30,
                assemble_minutes: 60,
                mold_code: "MOLD_A".to_string(),
                welding_fixture: "FIX_B".to_string(),
                color: "RED".to_string(),
                config: "BASE".to_string(),
                energy_score: 100.0,
                emission_score: 50.0,
            },
        ],
        params: Some(SolveParams {
            algorithm: "sa".to_string(),
            time_budget_sec: 10,
            seed: 42,
            weights: Some(Weights {
                tardiness: 10.0,
                color_change: 50.0,
                config_change: 30.0,
                energy_excess: 2.0,
                emission_excess: 3.0,
                material_shortage: 0.0,
            }),
            limits: Some(Limits {
                max_energy_per_shift: 5000.0,
                max_emission_per_shift: 2500.0,
            }),
        }),
    });

    // 调用接口
    let response = client.solve(request).await?;
    let result = response.into_inner();

    // 处理结果
    println!("Request ID: {}", result.request_id);
    println!("Total Cost: {:.2}", result.summary.unwrap().cost);
    println!("Schedule Items: {}", result.schedule.len());

    Ok(())
}
```

### 7.2 Python 客户端示例

```python
import grpc
from aps_pb2 import SolveRequest, SolveParams, Job, Weights, Limits
from aps_pb2_grpc import ApsServiceStub

def main():
    # 连接服务
    channel = grpc.insecure_channel('127.0.0.1:50051')
    client = ApsServiceStub(channel)

    # 构造请求
    request = SolveRequest(
        request_id="demo-001",
        plan_start_epoch_ms=1704106800000,
        jobs=[
            Job(
                vin="VIN001",
                due_epoch_ms=1704110400000,
                stamping_minutes=15,
                welding_minutes=20,
                painting_minutes=30,
                assemble_minutes=60,
                mold_code="MOLD_A",
                welding_fixture="FIX_B",
                color="RED",
                config="BASE",
                energy_score=100.0,
                emission_score=50.0,
            )
        ],
        params=SolveParams(
            algorithm="sa",
            time_budget_sec=10,
            seed=42,
            weights=Weights(
                tardiness=10.0,
                color_change=50.0,
                config_change=30.0,
                energy_excess=2.0,
                emission_excess=3.0,
                material_shortage=0.0,
            ),
            limits=Limits(
                max_energy_per_shift=5000.0,
                max_emission_per_shift=2500.0,
            ),
        ),
    )

    # 调用接口
    response = client.Solve(request)

    # 处理结果
    print(f"Request ID: {response.request_id}")
    print(f"Total Cost: {response.summary.cost:.2f}")
    print(f"Schedule Items: {len(response.schedule)}")

if __name__ == '__main__':
    main()
```

### 7.3 异步模式示例（Rust）

```rust
use tokio::time::{sleep, Duration};

async fn async_solve_example() -> Result<(), Box<dyn std::error::Error>> {
    let mut client = ApsServiceClient::connect("http://127.0.0.1:50051").await?;

    // 1. 提交任务
    let submit_response = client.submit_job(Request::new(SubmitJobRequest {
        request: Some(/* SolveRequest */),
    })).await?;
    
    let job_id = submit_response.into_inner().job_id;
    println!("Job submitted: {}", job_id);

    // 2. 轮询状态
    loop {
        let status_response = client.get_job_status(Request::new(GetJobStatusRequest {
            job_id: job_id.clone(),
        })).await?;
        
        let status = status_response.into_inner();
        println!("Status: {}", status.status);

        match status.status.as_str() {
            "COMPLETED" => {
                println!("Result: {:?}", status.result);
                break;
            }
            "FAILED" => {
                eprintln!("Error: {}", status.error_message);
                break;
            }
            _ => {
                sleep(Duration::from_secs(2)).await;
            }
        }
    }

    Ok(())
}
```

---

## 8. 最佳实践

### 8.1 接口选择策略

| 场景 | 推荐接口 | 理由 |
|------|----------|------|
| 订单数 ≤ 50 | Solve | 快速响应，用户体验好 |
| 订单数 > 50 | SubmitJob | 避免阻塞，支持进度查询 |
| 时间预算 ≤ 5s | Solve | 同步等待可接受 |
| 时间预算 > 10s | SubmitJob | 异步避免超时 |
| 高并发场景 | SubmitJob | 后台队列削峰 |

### 8.2 参数调优建议

#### 8.2.1 算法选择

- **baseline**: 仅用于快速验证或基准对比，不建议生产使用
- **sa**: 推荐生产环境使用，质量稳定
- **hybrid**: 平衡速度与质量，适合时间预算紧张场景

#### 8.2.2 时间预算

- **小规模（≤50车）**: 3-5秒足够
- **中等规模（50-100车）**: 10-15秒
- **大规模（>100车）**: 20-30秒

#### 8.2.3 权重配置

**强调交期**:
```json
{
  "tardiness": 100.0,
  "color_change": 10.0,
  "config_change": 10.0
}
```

**强调减少切换**:
```json
{
  "tardiness": 10.0,
  "color_change": 100.0,
  "config_change": 50.0
}
```

**平衡配置**（推荐）:
```json
{
  "tardiness": 10.0,
  "color_change": 50.0,
  "config_change": 30.0,
  "energy_excess": 2.0,
  "emission_excess": 3.0
}
```

### 8.3 错误处理最佳实践

```rust
use tonic::Status;

async fn robust_solve(client: &mut ApsServiceClient<Channel>, request: SolveRequest) 
    -> Result<SolveResponse, Box<dyn std::error::Error>> 
{
    let mut retries = 3;
    let mut delay = Duration::from_secs(1);

    loop {
        match client.solve(Request::new(request.clone())).await {
            Ok(response) => return Ok(response.into_inner()),
            Err(status) => {
                match status.code() {
                    // 不可重试错误
                    tonic::Code::InvalidArgument | tonic::Code::NotFound => {
                        return Err(status.into());
                    }
                    // 可重试错误
                    tonic::Code::ResourceExhausted | tonic::Code::Internal => {
                        if retries == 0 {
                            return Err(status.into());
                        }
                        retries -= 1;
                        sleep(delay).await;
                        delay *= 2; // 指数退避
                    }
                    _ => return Err(status.into()),
                }
            }
        }
    }
}
```

### 8.4 性能优化建议

1. **连接复用**: 使用连接池，避免频繁建立连接
2. **批量提交**: 合并多个订单到一个请求（不超过500）
3. **并发控制**: 同步模式限制并发数 ≤ CPU核心数
4. **超时设置**: 设置合理的客户端超时（建议 time_budget + 10s）
5. **结果缓存**: 对相同输入缓存结果（使用request_id作为键）

### 8.5 监控与可观测性

建议监控以下指标：

- **QPS**: 每秒请求数
- **延迟**: P50/P95/P99响应时间
- **成功率**: 成功请求比例
- **任务状态分布**: QUEUED/RUNNING/COMPLETED/FAILED数量
- **引擎版本**: 确保版本一致性

---

## 附录

### A. Proto 文件完整定义

参见项目文件: `proto/aps.proto`

### B. 常见问题 (FAQ)

**Q1: 为什么返回两个KPI（summary和baseline_summary）？**

A: baseline_summary提供基准对比，帮助评估优化效果。例如：
- summary.cost = 150.5（优化后）
- baseline_summary.cost = 180.0（基准）
- 优化提升 = (180.0 - 150.5) / 180.0 = 16.4%

**Q2: schedule 和 detailed_schedule 的区别？**

A:
- `schedule`: 简化版，每辆车一条记录（通常为总装工序）
- `detailed_schedule`: 详细版，每道工序一条记录（冲压+焊装+涂装+总装）

**Q3: 如何处理不可行解？**

A: 检查 `violations` 字段，根据 `vtype` 调整：
- CAPACITY: 增加班次或产线
- MATERIAL: 补充物料
- ENERGY/EMISSION: 放宽限制或调整权重

**Q4: 异步任务保留多久？**

A: 内存中保留24小时，建议客户端在任务完成后立即获取结果并持久化。

### C. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| v1.0 | 2026-01-29 | 初始版本，支持四大工艺排产 |

### D. 联系方式

- **技术支持**: aps-support@example.com
- **GitHub**: https://github.com/example/aps-engine
- **文档**: https://docs.example.com/aps

---

**文档版本**: v1.0  
**最后更新**: 2026-01-29  
**维护者**: APS开发团队

