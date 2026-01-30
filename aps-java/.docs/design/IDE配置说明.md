# IDE 配置说明

## ⚠️ 重要：解决 IDE 无法识别生成代码的问题

如果 IDE 提示 "无法解析符号 'ApsServiceGrpc'" 等错误，这是因为 IDE 还没有识别到 Maven 生成的 protobuf 代码。

## 🔧 解决方案

### IntelliJ IDEA

#### 方法1: 重新导入 Maven 项目（推荐）

1. 点击右侧 Maven 工具栏
2. 点击刷新按钮（Reload All Maven Projects）
3. 等待项目重新构建完成

#### 方法2: 手动标记生成的源代码目录

1. 右键点击 `target/generated-sources/protobuf/java` 目录
2. 选择 "Mark Directory as" → "Generated Sources Root"
3. 右键点击 `target/generated-sources/protobuf/grpc-java` 目录
4. 选择 "Mark Directory as" → "Generated Sources Root"

#### 方法3: 使用 Maven 命令

```bash
# 清理并重新编译
mvn clean compile

# 然后在 IDE 中刷新 Maven 项目
```

### Eclipse

1. 右键点击项目
2. 选择 "Maven" → "Update Project..."
3. 勾选 "Force Update of Snapshots/Releases"
4. 点击 "OK"

### VS Code

1. 按 `Cmd+Shift+P` (Mac) 或 `Ctrl+Shift+P` (Windows/Linux)
2. 输入 "Java: Clean Java Language Server Workspace"
3. 重新加载窗口

## ✅ 验证是否成功

执行以下步骤验证：

1. 打开 `GrpcClientConfig.java`
2. 检查 `import com.aps.grpc.proto.ApsServiceGrpc;` 是否有红色波浪线
3. 如果没有红色波浪线，说明配置成功

## 📁 生成的代码位置

```
target/generated-sources/protobuf/
├── java/                           # Protobuf 消息类
│   └── com/aps/grpc/proto/
│       ├── SolveRequest.java
│       ├── SolveResponse.java
│       ├── Job.java
│       ├── KpiSummary.java
│       └── ... (其他消息类)
└── grpc-java/                      # gRPC 服务类
    └── com/aps/grpc/proto/
        └── ApsServiceGrpc.java     # gRPC 服务存根
```

## 🚀 启动应用前的检查清单

- [ ] Maven 编译成功 (`mvn clean compile`)
- [ ] IDE 已识别生成的代码（无红色波浪线）
- [ ] Rust 排产引擎已启动（监听 localhost:50051）
- [ ] 数据库连接正常
- [ ] Redis 连接正常

## 💡 常见问题

### Q1: 为什么每次 clean 后都需要重新标记？

A: 因为 `target` 目录在 clean 时会被删除，需要重新生成。建议使用 "Reload Maven Project" 而不是手动标记。

### Q2: 编译成功但 IDE 还是报错？

A: 尝试以下步骤：
1. File → Invalidate Caches / Restart
2. 重新导入 Maven 项目
3. 确认 JDK 版本为 17

### Q3: 如何确认 protobuf 插件是否正常工作？

A: 检查以下内容：
```bash
# 查看生成的文件
ls -la target/generated-sources/protobuf/java/com/aps/grpc/proto/
ls -la target/generated-sources/protobuf/grpc-java/com/aps/grpc/proto/

# 应该看到多个 .java 文件
```

---

**提示**: 如果以上方法都不行，请尝试删除 `.idea` 目录（IntelliJ）或 `.project` 文件（Eclipse），然后重新导入项目。

