using System.Text.Json;

namespace DistributedIdService.Api.Middleware;

public class RateLimitingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly int _maxRequestsPerMinute;
    private readonly Dictionary<string, List<DateTime>> _requests;
    private readonly object _lock = new();

    public RateLimitingMiddleware(RequestDelegate next, IConfiguration config)
    {
        _next = next;
        _maxRequestsPerMinute = 1000000; // 非常高的限制，支持压测
        _requests = new Dictionary<string, List<DateTime>>();
    }

    public async Task InvokeAsync(HttpContext context)
    {
        var clientIp = GetClientIp(context);
        var now = DateTime.UtcNow;

        var rateLimited = false;
        lock (_lock)
        {
            if (!_requests.ContainsKey(clientIp))
            {
                _requests[clientIp] = new List<DateTime>();
            }

            var cutoff = now.AddMinutes(-1);
            _requests[clientIp].RemoveAll(t => t < cutoff);

            if (_requests[clientIp].Count >= _maxRequestsPerMinute)
            {
                rateLimited = true;
            }
            else
            {
                _requests[clientIp].Add(now);
            }
        }

        if (rateLimited)
        {
            context.Response.StatusCode = StatusCodes.Status429TooManyRequests;
            context.Response.ContentType = "application/json";
            var response = new
            {
                success = false,
                message = "Rate limit exceeded. Please try again later.",
                timestamp = now
            };
            await context.Response.WriteAsync(JsonSerializer.Serialize(response));
            return;
        }

        await _next(context);
    }

    private string GetClientIp(HttpContext context)
    {
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