# DistributedIdService

一个基于 .NET 9 的分布式雪花 ID 生成服务，提供唯一 ID 生成和熔断功能。

## 功能特性

- **雪花算法 ID 生成**: 生成 64 位唯一 ID，包含时间戳、工作机器 ID、序列号
- **熔断器模式**: 当错误达到阈值时自动熔断，防止雪崩效应
- **健康检查**: 提供健康状态检查端点
- **批量生成**: 支持批量生成 ID
- **ID 解析**: 解析 ID 的详细信息（时间戳、工作机器 ID、序列号）
- **限流**: 基于 IP 的请求速率限制
- **异常处理**: 统一的异常处理和错误响应

## 项目结构

```
DistributedIdService/
├── src/
│   └── DistributedIdService.Api/    # Web API 项目
│       ├── Controllers/             # API 控制器
│       ├── Middleware/              # 中间件（异常处理、限流、性能统计）
│       ├── Services/                # 核心服务（雪花生成器、熔断器、worker id 提供者）
│       ├── Models/                  # 数据模型
│       └── Program.cs               # 应用入口
├── tests/
│   ├── DistributedIdService.Tests/  # 单元测试
│   └── DistributedIdService.LoadTest/ # 压测工具
└── docs/                            # 文档和示例
```

## 快速开始

### 环境要求

- .NET 9 SDK 或更高版本
- 端口 5000（默认）

### 运行服务

```bash
# 还原依赖
dotnet restore

# 构建
dotnet build

# 运行
dotnet run --project src/DistributedIdService.Api
```

服务默认启动在 `http://localhost:5000`

### 配置

通过 `appsettings.json` 进行配置：

```json
{
  "WorkerId": "", // 手动指定 workerId，留空则自动生成
  "UseMachineId": true, // 使用机器标识生成 workerId
  "UseCircuitBreaker": true, // 是否启用熔断器
  "CircuitBreaker": {
    "FailureThreshold": 5, // 失败阈值
    "OpenStateDurationSeconds": 30, // 熔断持续时间
    "HalfOpenMaxCalls": 3 // 半开状态最大调用次数
  }
}
```

## API 端点

| 端点 | 方法 | 描述 |
|------|------|------|
| `/` | GET | 服务信息 |
| `/ping` | GET | 简单健康检查 |
| `/health` | GET | 详细健康检查 |
| `/api/id` | GET | 生成单个雪花 ID |
| `/api/id/batch` | POST | 批量生成 ID（Body: `{"count": N}`） |
| `/api/id/info/{id}` | GET | 解析 ID 详细信息 |
| `/api/id/status` | GET | 获取生成器状态 |
| `/metrics` | GET | 性能指标（P50/P90/P95/P99/QPS） |

### 示例

```bash
# 生成单个 ID
curl http://localhost:5000/api/id

# 批量生成 5 个 ID
curl -X POST http://localhost:5000/api/id/batch \
  -H "Content-Type: application/json" \
  -d '{"count":5}'

# 解析 ID
curl http://localhost:5000/api/id/info/311700624614801408

# 健康检查
curl http://localhost:5000/health
```

## 测试

运行单元测试：

```bash
dotnet test tests/DistributedIdService.Tests/DistributedIdService.Tests.csproj
```

测试覆盖：
- 雪花ID生成器
- 熔断器状态转换
- Worker ID 提供者

### 性能压测

项目包含压测工具，可测试 P50/P90/P95/P99 等延迟指标：

```bash
# 参数: [并发数] [持续秒数]，默认 50并发 30秒
dotnet run --project tests/DistributedIdService.LoadTest

# 示例：100并发，持续60秒
dotnet run --project tests/DistributedIdService.LoadTest 100 60
```

**输出示例：**
```
=== 压测结果 ===
总请求数: 150000 (成功: 150000, 失败: 0)

--- 延迟分布 ---
  最小值:  0.15ms
  平均值:  2.35ms
  最大值:  45.23ms
  P50:     1.89ms
  P90:     3.21ms
  P95:     4.15ms
  P99:     8.67ms
  P99.9:   15.32ms
```

服务端也会每10秒输出一次指标到控制台，或直接访问 `/metrics` 端点查看实时数据。

## 核心组件

### SnowflakeIdGenerator

实现 Twitter 雪花算法：
- 时间戳（41 位）- 毫秒级，从自定义 epoch 开始
- 工作机器 ID（10 位）- 支持最多 1024 个实例
- 序列号（12 位）- 同一毫秒内的序列，最多 4096

### CircuitBreaker

熔断器实现三种状态：
- Closed: 正常请求通过
- Open: 快速失败，不调用实际服务
- HalfOpen: 允许部分请求试探服务是否恢复

支持回退机制，熔断时返回默认值。

### WorkerIdProvider

自动生成和持久化 worker ID：
- 支持从配置文件指定固定 ID
- 支持基于机器名和进程 ID 自动生成
- 自动保存到本地文件，重启后保持相同 ID

## 注意事项

- WorkerId 必须在 0-1023 范围内
- 确保所有实例使用不同的 WorkerId
- 系统时钟回拨会抛出异常，建议使用 NTP 同步时间
- 熔断器默认配置：5 次失败后打开，持续 30 秒

## 技术栈

- .NET 9
- ASP.NET Core Web API
- xUnit（测试）
- 无外部依赖

## Java 客户端示例

对于使用 Java 的队友，可以使用以下示例调用服务：

### 完整示例代码

```java
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class JavaClientExample {
    private static final String BASE_URL = "http://localhost:5000";
    private static final HttpClient httpClient;

    static {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // 1. 生成单个ID
        String id = generateSingleId();
        System.out.println("生成的ID: " + id);

        // 2. 批量生成5个ID
        String batch = generateBatchIds(5);
        System.out.println("批量结果: " + batch);

        // 3. 解析ID详情
        String info = getIdInfo(id);
        System.out.println("ID详情: " + info);

        // 4. 健康检查
        String health = getHealth();
        System.out.println("健康状态: " + health);
    }

    public static String generateSingleId() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/id"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String generateBatchIds(int count) throws IOException, InterruptedException {
        String jsonBody = "{\"count\":" + count + "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/id/batch"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String getIdInfo(String id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/id/info/" + id))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String getHealth() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/health"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
```

### 快速使用

```bash
# 1. 确保 C# 服务已启动
dotnet run --project src/DistributedIdService.Api

# 2. 编译 Java 代码
javac docs/JavaClientExample.java

# 3. 运行（需要 Java 11+）
java -cp docs JavaClientExample
```

### 响应示例

**生成ID：**
```json
{
  "success": true,
  "message": "ID generated successfully",
  "data": 311704606653464576,
  "timestamp": "2026-05-10T03:22:49.4180262Z"
}
```

**批量生成：**

```json
{
  "success": true,
  "message": "Generated 5 IDs",
  "data": [311700651613536256, 311700651617730560, ...],
  "timestamp": "2026-05-10T03:07:06.4661471Z"
}
```

**ID详情：**

```json
{
  "id": 311700624614801408,
  "timestamp": 1778382420025,
  "workerId": 748,
  "sequence": 0,
  "generatedAt": "2026-05-10T03:07:00.025"
}
```

在博客项目中，可以直接使用 `HttpClient` 封装成工具类，根据业务需求调用相应接口。
