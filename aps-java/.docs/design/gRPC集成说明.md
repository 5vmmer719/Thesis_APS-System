# gRPC 客户端集成文档

## 📋 概述

本项目已成功集成 gRPC 客户端，用于与 Rust APS 排产引擎通信。

## 🏗️ 架构说明

### 文件结构

```
src/main/
├── java/com/aps/
│   ├── config/
│   │   └── GrpcClientConfig.java          # gRPC 客户端配置
│   └── module/schedule/
│       ├── client/
│       │   └── ScheduleEngineClient.java  # gRPC 客户端封装
│       └── controller/
│           └── GrpcTestController.java    # gRPC 测试接口
└── resources/
    └── proto/
        └── aps.proto                       # Protocol Buffers 定义

target/generated-sources/protobuf/
├── java/                                   # 生成的消息类
└── grpc-java/                              # 生成的 gRPC 服务类
```

### 核心组件

1. **GrpcClientConfig**: 配置 gRPC Channel 和 Stub
2. **ScheduleEngineClient**: 封装 gRPC 调用逻辑
3. **GrpcTestController**: 提供 HTTP 接口测试 gRPC 连接

## ⚙️ 配置说明

### application.yml 配置

```yaml
aps:
  schedule-engine:
    grpc-host: localhost      # gRPC 服务地址
    grpc-port: 50051          # gRPC 服务端口
    timeout-seconds: 300      # 超时时间（秒）
```

### Maven 依赖

已添加以下依赖：
- `io.grpc:grpc-netty-shaded:1.61.0`
- `io.grpc:grpc-protobuf:1.61.0`
- `io.grpc:grpc-stub:1.61.0`
- `com.google.protobuf:protobuf-java:3.25.2`

### Maven 插件

已配置以下插件：
- `os-maven-plugin:1.7.1` - 检测操作系统和架构
- `protobuf-maven-plugin:0.6.1` - 编译 proto 文件

## 🚀 快速开始

### 1. 编译 Proto 文件

```bash
# 使用完整路径（如果未配置环境变量）
/Users/juedu/Downloads/software/apache-maven-3.6.3/bin/mvn clean compile

# 或者使用 mvn（如果已配置环境变量）
mvn clean compile
```

生成的 Java 代码位于：
- `target/generated-sources/protobuf/java/` - 消息类
- `target/generated-sources/protobuf/grpc-java/` - gRPC 服务类

### 2. 启动应用

```bash
# 方式1: 使用 Maven
mvn spring-boot:run

# 方式2: 使用 IDE 运行
# 运行主类: com.aps.ApsApplication
```

### 3. 测试 gRPC 连接

#### 方式1: 使用 Swagger UI

访问: http://localhost:8088/api/v1/doc.html

在 "gRPC 测试" 分组下测试以下接口：

1. **健康检查**: `GET /api/v1/grpc-test/health`
2. **测试同步求解**: `POST /api/v1/grpc-test/test-solve`
3. **测试异步提交**: `POST /api/v1/grpc-test/test-submit-job`
4. **查询任务状态**: `GET /api/v1/grpc-test/job-status/{jobId}`
5. **列出所有任务**: `GET /api/v1/grpc-test/list-jobs`

#### 方式2: 使用 curl 命令

```bash
# 1. 健康检查
curl -X GET "http://localhost:8088/api/v1/grpc-test/health"

# 2. 测试同步求解
curl -X POST "http://localhost:8088/api/v1/grpc-test/test-solve"

# 3. 测试异步提交
curl -X POST "http://localhost:8088/api/v1/grpc-test/test-submit-job"

# 4. 查询任务状态（替换 {jobId}）
curl -X GET "http://localhost:8088/api/v1/grpc-test/job-status/{jobId}"

# 5. 列出所有任务
curl -X GET "http://localhost:8088/api/v1/grpc-test/list-jobs?limit=10"
```

## 📊 gRPC 接口说明

### 同步求解 (Solve)

**用途**: 小规模排产（≤100辆车），快速响应

**请求参数**:
- `request_id`: 请求唯一标识
- `plan_start_epoch_ms`: 排产起始时间（毫秒时间戳）
- `jobs`: 待排产订单列表
- `params`: 求解参数（算法、时间预算、权重等）

**响应结果**:
- `summary`: 优化方案 KPI
- `baseline_summary`: 基准方案 KPI
- `order`: 排产顺序（VIN 列表）
- `schedule`: 简化调度表
- `detailed_schedule`: 详细调度表
- `violations`: 约束违反列表
- `convergence`: 收敛曲线

### 异步任务 (SubmitJob + GetJobStatus)

**用途**: 大规模排产（>100辆车），避免阻塞

**流程**:
1. `SubmitJob` - 提交任务，返回 `jobId`
2. `GetJobStatus` - 轮询任务状态
3. 状态为 `COMPLETED` 时获取结果

**任务状态**:
- `QUEUED`: 排队中
- `RUNNING`: 运行中
- `COMPLETED`: 已完成
- `FAILED`: 失败

## 🔧 故障排查

### 问题1: 连接被拒绝

**错误信息**: `UNAVAILABLE: io exception`

**解决方案**:
1. 确认 Rust 引擎已启动
2. 检查配置的 host 和 port 是否正确
3. 检查防火墙设置

### 问题2: 超时

**错误信息**: `DEADLINE_EXCEEDED`

**解决方案**:
1. 增加 `timeout-seconds` 配置
2. 减少订单数量
3. 使用异步模式

### 问题3: 参数错误

**错误信息**: `INVALID_ARGUMENT`

**解决方案**:
1. 检查必填字段是否完整
2. 检查订单数量是否超限（≤500）
3. 检查时间戳格式是否正确

## 📝 使用示例

### Java 代码示例

```java
@Autowired
private ScheduleEngineClient scheduleEngineClient;

public void scheduleProduction() {
    // 构造请求
    SolveRequest request = SolveRequest.newBuilder()
        .setRequestId("req-" + System.currentTimeMillis())
        .setPlanStartEpochMs(System.currentTimeMillis())
        .addJobs(Job.newBuilder()
            .setVin("VIN001")
            .setDueEpochMs(System.currentTimeMillis() + 3600000)
            .setAssembleMinutes(60)
            .setColor("RED")
            .setConfig("BASE")
            .build())
        .setParams(SolveParams.newBuilder()
            .setAlgorithm("sa")
            .setTimeBudgetSec(10)
            .build())
        .build();
    
    // 调用 gRPC
    SolveResponse response = scheduleEngineClient.solve(request);
    
    // 处理结果
    System.out.println("Cost: " + response.getSummary().getCost());
}
```

## 🎯 后续开发计划

- [x] ✅ gRPC 客户端配置
- [x] ✅ Proto 文件定义
- [x] ✅ 客户端封装
- [x] ✅ 测试接口
- [ ] ⏳ 排产任务管理
- [ ] ⏳ 排产方案管理
- [ ] ⏳ 甘特图数据生成
- [ ] ⏳ 方案手动调整
- [ ] ⏳ 方案发布（生成工单）

## 📚 参考文档

- [gRPC 官方文档](https://grpc.io/docs/languages/java/)
- [Protocol Buffers 文档](https://protobuf.dev/)
- [APS gRPC 接口文档](docs/design/grpc文档.md)

---

**最后更新**: 2026-01-29  
**维护者**: APS 开发团队

