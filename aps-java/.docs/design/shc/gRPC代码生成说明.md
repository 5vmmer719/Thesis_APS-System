# gRPC 代码生成说明

## 问题描述
gRPC 的 Java 代码需要从 proto 文件生成，但是当前还没有生成这些代码。

## Proto 文件位置
```
grpc/src/main/resources/proto/aps.proto
```

## 生成的 Java 包
根据 proto 文件定义：
```protobuf
option java_package = "com.aps.grpc.proto";
```

生成的类将位于：`com.aps.grpc.proto` 包下

## 生成方法

### 方法一：使用 Maven 命令生成（推荐）

```bash
# 在项目根目录执行
mvn clean compile -DskipTests

# 或者只生成 protobuf 代码
mvn protobuf:compile protobuf:compile-custom -pl grpc
```

### 方法二：使用 IDE 生成

在 IntelliJ IDEA 中：
1. 右键点击 `grpc` 模块
2. 选择 Maven → Generate Sources and Update Folders
3. 或者执行 Maven 生命周期：`compile`

## 生成的类列表

执行后会在 `grpc/src/main/java/com/aps/grpc/proto/` 目录下生成以下类：

### 消息类
- `SolveRequest`
- `SolveResponse`
- `Job`
- `SolveParams`
- `Weights`
- `Limits`
- `KpiSummary`
- `ScheduleItem`
- `ShiftViolation`
- `ConvergencePoint`
- `SubmitJobRequest`
- `SubmitJobResponse`
- `GetJobStatusRequest`
- `GetJobStatusResponse`
- `ListJobsRequest`
- `ListJobsResponse`
- `JobInfo`
- `ApsProto` (外部类)

### 服务类
- `ApsServiceGrpc` (包含 Stub 类)
  - `ApsServiceBlockingStub` (同步调用)
  - `ApsServiceStub` (异步调用)
  - `ApsServiceFutureStub` (Future 调用)

## 当前状态

✅ `ScheduleEngineClient.java` 已添加导入语句：
```java
import com.aps.grpc.proto.*;
```

⚠️ 需要生成 gRPC 代码后才能编译通过

## 如果遇到 protoc 下载问题

如果内网环境无法下载 protoc 编译器，可以：

### 选项1：手动下载 protoc
1. 从 https://github.com/protocolbuffers/protobuf/releases 下载对应版本
2. 对于 Mac ARM64，下载：`protoc-3.25.2-osx-aarch_64.zip`
3. 解压后将 `protoc` 放到系统 PATH 中

### 选项2：使用已有的 protoc
如果系统已安装 protoc，可以修改 `grpc/pom.xml`：
```xml
<protocExecutable>protoc</protocExecutable>
```

### 选项3：跳过 proto 生成，直接使用预生成的代码
如果有其他环境已生成的代码，可以直接复制到：
```
grpc/src/main/java/com/aps/grpc/proto/
```

## 验证生成结果

生成后检查：
```bash
# 查看生成的文件
ls -la grpc/src/main/java/com/aps/grpc/proto/

# 应该看到类似输出：
# ApsProto.java
# ApsServiceGrpc.java
# SolveRequest.java
# SolveResponse.java
# ... (其他消息类)
```

## 下一步

1. 生成 gRPC 代码
2. 重新编译项目：`mvn clean compile`
3. 检查是否有编译错误

## 相关文件

- Proto 定义：`grpc/src/main/resources/proto/aps.proto`
- gRPC 客户端：`grpc/src/main/java/com/aps/client/ScheduleEngineClient.java`
- gRPC 配置：`src/main/java/com/aps/config/GrpcClientConfig.java`
- gRPC 模块 POM：`grpc/pom.xml`

