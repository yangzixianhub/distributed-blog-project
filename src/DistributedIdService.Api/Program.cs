using DistributedIdService.Api.Middleware;
using DistributedIdService.Api.Services;
using Microsoft.AspNetCore.Mvc;

var builder = WebApplication.CreateBuilder(args);

// 配置 Kestrel，指定端口 5000
builder.WebHost.UseUrls("http://0.0.0.0:5000");

// Add services to the container.
builder.Services
    .AddControllers()
    .AddJsonOptions(options =>
    {
        // 使用驼峰命名 (camelCase)
        options.JsonSerializerOptions.PropertyNamingPolicy = System.Text.Json.JsonNamingPolicy.CamelCase;
        options.JsonSerializerOptions.WriteIndented = false;
    });

// 配置 API 行为
builder.Services.Configure<ApiBehaviorOptions>(options =>
{
    options.SuppressModelStateInvalidFilter = true;
});

// 注册配置
var configuration = builder.Configuration;

// 注册雪花ID生成器
builder.Services.AddSingleton<ISnowflakeIdGenerator>(sp =>
{
    var logger = sp.GetRequiredService<ILogger<SnowflakeIdGenerator>>();
    var workerIdProvider = sp.GetRequiredService<WorkerIdProvider>();

    // 从配置读取参数
    var workerId = workerIdProvider.GetWorkerId();

    // Snowflake 起始时间戳（自定义，例如：2024-01-01 00:00:00 UTC）
    // 这个值是 2024-01-01T00:00:00Z 的 Unix 时间戳（毫秒）
    const long epoch = 1704067200000L;

    logger.LogInformation("Creating SnowflakeIdGenerator with WorkerId: {WorkerId}, Epoch: {Epoch}",
        workerId, epoch);

    var config = sp.GetRequiredService<IConfiguration>();
    var useCircuitBreaker = config.GetValue("UseCircuitBreaker", false);
    var breakerLogger = sp.GetRequiredService<ILogger<CircuitBreakerSnowflakeIdGenerator>>();

    ISnowflakeIdGenerator generator = new SnowflakeIdGenerator(workerId, epoch);

    if (useCircuitBreaker)
    {
        var failureThreshold = config.GetValue("CircuitBreaker:FailureThreshold", 5);
        var openStateDurationSeconds = config.GetValue("CircuitBreaker:OpenStateDurationSeconds", 30);
        var halfOpenMaxCalls = config.GetValue("CircuitBreaker:HalfOpenMaxCalls", 3);

        var breaker = new CircuitBreaker(
            "SnowflakeIdGenerator",
            failureThreshold,
            TimeSpan.FromSeconds(openStateDurationSeconds),
            halfOpenMaxCalls);

        generator = new CircuitBreakerSnowflakeIdGenerator(generator, breaker, breakerLogger);
        logger.LogInformation("Circuit breaker enabled for SnowflakeIdGenerator");
    }

    return generator;
});

// 注册 WorkerId 提供者
builder.Services.AddSingleton<WorkerIdProvider>();
builder.Services.AddSingleton<IWorkerIdProvider>(sp => sp.GetRequiredService<WorkerIdProvider>());

// 注册 API Key 认证配置
var apiKeysSection = configuration.GetSection(ApiKeyAuthenticationOptions.SectionName);
var apiKeysOptions = new ApiKeyAuthenticationOptions();

// 手动绑定配置（.NET 配置绑定对 List 类型的 bug）
for (int i = 0; i < 10; i++)
{
    var key = configuration[$"{ApiKeyAuthenticationOptions.SectionName}:{i}:Key"];
    if (string.IsNullOrEmpty(key)) break;
    var name = configuration[$"{ApiKeyAuthenticationOptions.SectionName}:{i}:Name"] ?? "";
    var enabled = configuration[$"{ApiKeyAuthenticationOptions.SectionName}:{i}:Enabled"] ?? "true";
    var config = new ApiKeyConfig { Key = key, Name = name, Enabled = bool.Parse(enabled) };
    apiKeysOptions.Keys.Add(config);
    apiKeysOptions.KeysDictionary[key] = config;
}
builder.Services.AddSingleton(apiKeysOptions);

// 记录配置信息
builder.Logging.AddConsole();
builder.Logging.SetMinimumLevel(LogLevel.Information);

var app = builder.Build();

// 配置 HTTP 请求管道

// 1. 异常处理中间件（最先添加）
app.UseMiddleware<ExceptionHandlingMiddleware>();

// 2. API Key 认证中间件
app.UseApiKeyAuthentication();

// 3. 限流中间件
app.UseMiddleware<RateLimitingMiddleware>();

// 4. 性能统计中间件
app.UsePerformanceMetrics();

// 3. HTTPS 重定向（如果有 HTTPS 配置）
if (!app.Environment.IsDevelopment())
{
    app.UseHttpsRedirection();
}

// 4. 路由
app.UseRouting();

// 5. 授权（如需要，目前无）
app.UseAuthorization();

// 6. 映射控制器
app.MapControllers();

// 8. 性能指标端点
PerformanceMetricsMiddleware.MapMetricsEndpoints(app);

// 7. 健康检查端点
app.MapGet("/", () => new
{
    name = "DistributedIdService",
    version = "1.0.0",
    status = "running",
    timestamp = DateTime.UtcNow
});

app.MapGet("/ping", () => "pong");

// 记录启动信息
var logger = app.Services.GetRequiredService<ILogger<Program>>();
logger.LogInformation("DistributedIdService started successfully");
logger.LogInformation("Service endpoints:");
logger.LogInformation("  GET  /                 - Service info");
logger.LogInformation("  GET  /ping            - Health check (simple)");
logger.LogInformation("  GET  /health          - Health check (detailed)");
logger.LogInformation("  GET  /api/id          - Generate single ID");
logger.LogInformation("  POST /api/id/batch    - Generate batch IDs");
logger.LogInformation("  GET  /api/id/info/id - Parse ID info");
logger.LogInformation("  GET  /api/id/status   - Generator status");

app.Run();
