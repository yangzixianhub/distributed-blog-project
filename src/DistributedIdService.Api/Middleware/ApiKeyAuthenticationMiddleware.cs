using System.Text.Json;

namespace DistributedIdService.Api.Middleware;

public class ApiKeyAuthenticationMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ApiKeyAuthenticationOptions _options;
    private readonly ILogger<ApiKeyAuthenticationMiddleware> _logger;
    private readonly HashSet<string> _excludedPaths;

    public ApiKeyAuthenticationMiddleware(
        RequestDelegate next,
        ApiKeyAuthenticationOptions options,
        ILogger<ApiKeyAuthenticationMiddleware> logger)
    {
        _next = next;
        _options = options;
        _logger = logger;
        _excludedPaths = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
        {
            "/",
            "/ping",
            "/health",
            "/metrics"
        };

        _logger.LogInformation("ApiKeyAuthenticationMiddleware initialized with {KeyCount} keys", _options.Keys.Count);
    }

    public async Task InvokeAsync(HttpContext context)
    {
        var path = context.Request.Path.Value ?? string.Empty;

        // 排除不需要认证的路径
        if (IsExcludedPath(path))
        {
            await _next(context);
            return;
        }

        // 获取 API Key
        if (!context.Request.Headers.TryGetValue(ApiKeyAuthenticationOptions.HeaderName, out var extractedKey))
        {
            _logger.LogWarning("API request without API key from {RemoteIp}", context.Connection.RemoteIpAddress);
            await WriteUnauthorizedResponse(context, "Missing API key");
            return;
        }

        var key = extractedKey.ToString();

        // O(1) 查找
        if (!_options.KeysDictionary.TryGetValue(key, out var keyConfig))
        {
            _logger.LogWarning("API request with invalid API key from {RemoteIp}", context.Connection.RemoteIpAddress);
            await WriteUnauthorizedResponse(context, "Invalid API key");
            return;
        }

        if (!keyConfig.Enabled)
        {
            _logger.LogWarning("API request with disabled API key: {KeyName}", keyConfig.Name);
            await WriteUnauthorizedResponse(context, "API key is disabled");
            return;
        }

        // 将调用方信息记录到日志
        context.Items["ApiKeyName"] = keyConfig.Name;
        _logger.LogDebug("API request authenticated for service: {ServiceName}", keyConfig.Name);

        await _next(context);
    }

    private bool IsExcludedPath(string path)
    {
        // 精确匹配
        if (_excludedPaths.Contains(path))
            return true;

        // /metrics 可能有子路径如 /metrics/reset
        if (path.StartsWith("/metrics", StringComparison.OrdinalIgnoreCase))
            return true;

        return false;
    }

    private static async Task WriteUnauthorizedResponse(HttpContext context, string message)
    {
        context.Response.StatusCode = StatusCodes.Status401Unauthorized;
        context.Response.ContentType = "application/json";

        var response = new
        {
            success = false,
            error = new
            {
                code = "UNAUTHORIZED",
                message = message
            }
        };

        await context.Response.WriteAsync(JsonSerializer.Serialize(response, new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase
        }));
    }
}

public static class ApiKeyAuthenticationMiddlewareExtensions
{
    public static IApplicationBuilder UseApiKeyAuthentication(this IApplicationBuilder app)
    {
        return app.UseMiddleware<ApiKeyAuthenticationMiddleware>();
    }
}