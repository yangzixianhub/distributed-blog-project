using System.Text.Json;

namespace DistributedIdService.Api.Middleware;

public class RateLimitingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly int _maxRequestsPerMinute;
    private readonly Dictionary<string, List<DateTime>> _requests;
    private readonly object _lock = new();

    public RateLimitingMiddleware(RequestDelegate next)
    {
        _next = next;
        _maxRequestsPerMinute = 60; // 默认每分钟60次
        _requests = new Dictionary<string, List<DateTime>>();
    }

    public async Task InvokeAsync(HttpContext context)
    {
        var clientIp = GetClientIp(context);
        var now = DateTime.UtcNow;

        lock (_lock)
        {
            if (!_requests.ContainsKey(clientIp))
            {
                _requests[clientIp] = new List<DateTime>();
            }

            // 清理过期的请求记录
            var cutoff = now.AddMinutes(-1);
            _requests[clientIp].RemoveAll(t => t < cutoff);

            // 检查请求是否超过限制
            if (_requests[clientIp].Count >= _maxRequestsPerMinute)
            {
                context.Response.StatusCode = StatusCodes.Status429TooManyRequests;
                var response = new
                {
                    success = false,
                    message = "Rate limit exceeded. Please try again later.",
                    timestamp = now
                };
                return;
            }

            _requests[clientIp].Add(now);
        }

        await _next(context);
    }

    private string GetClientIp(HttpContext context)
    {
        // 尝试从各种头中获取真实IP
        if (context.Request.Headers.TryGetValue("X-Forwarded-For", out var forwarded))
        {
            return forwarded.ToString().Split(',')[0].Trim();
        }

        if (context.Request.Headers.TryGetValue("X-Real-IP", out var realIp))
        {
            return realIp.ToString();
        }

        return context.Connection.RemoteIpAddress?.ToString() ?? "unknown";
    }
}
