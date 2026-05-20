using System.Collections.Concurrent;
using System.Diagnostics;

namespace DistributedIdService.Api.Middleware;

public class PerformanceMetricsMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<PerformanceMetricsMiddleware> _logger;

    private static readonly ConcurrentDictionary<string, LatencyRecorder> _recorders = new();
    private static readonly ConcurrentQueue<RequestMetric> _metricsBuffer = new();
    private static readonly CancellationTokenSource _metricsTimerCts = new();

    public PerformanceMetricsMiddleware(RequestDelegate next, ILogger<PerformanceMetricsMiddleware> logger)
    {
        _next = next;
        _logger = logger;
        StartMetricsReporter();
    }

    public async Task InvokeAsync(HttpContext context)
    {
        var path = context.Request.Path.Value ?? "/";
        var method = context.Request.Method;

        if (!_recorders.TryGetValue(path, out var recorder))
        {
            recorder = new LatencyRecorder(path);
            _recorders.TryAdd(path, recorder);
        }

        var sw = Stopwatch.StartNew();
        try
        {
            await _next(context);
        }
        finally
        {
            sw.Stop();
            var latency = sw.Elapsed.TotalMicroseconds / 1000.0;
            recorder.Record(latency);

            if (!context.Response.HasStarted)
            {
                context.Response.Headers["X-Response-Time-Ms"] = latency.ToString("F2");
            }

            // 缓存指标
            _metricsBuffer.Enqueue(new RequestMetric
            {
                Path = path,
                Method = method,
                LatencyMs = latency,
                Timestamp = DateTime.UtcNow,
                StatusCode = context.Response.StatusCode
            });
        }
    }

    private void StartMetricsReporter()
    {
        Task.Run(async () =>
        {
            while (!_metricsTimerCts.Token.IsCancellationRequested)
            {
                try
                {
                    await Task.Delay(TimeSpan.FromSeconds(10), _metricsTimerCts.Token);
                    ReportMetrics();
                }
                catch (OperationCanceledException)
                {
                    break;
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error reporting metrics");
                }
            }
        });
    }

    private void ReportMetrics()
    {
        if (_recorders.IsEmpty) return;

        _logger.LogInformation("=== Performance Metrics ===");
        foreach (var kvp in _recorders)
        {
            var recorder = kvp.Value;
            var stats = recorder.GetStatistics();
            if (stats.Count > 0)
            {
                _logger.LogInformation(
                    "Path: {Path} | Count: {Count} | Avg: {Avg:F2}ms | Min: {Min:F2}ms | Max: {Max:F2}ms | P50: {P50:F2}ms | P90: {P90:F2}ms | P95: {P95:F2}ms | P99: {P99:F2}ms | QPS: {QPS:F2}",
                    kvp.Key, stats.Count, stats.Avg, stats.Min, stats.Max,
                    stats.P50, stats.P90, stats.P95, stats.P99, stats.Qps);
            }
        }
    }

    // 添加一个端点查看实时指标
    public static IEndpointRouteBuilder MapMetricsEndpoints(IEndpointRouteBuilder app)
    {
        app.MapGet("/metrics", () =>
        {
            var result = _recorders.Select(kvp =>
            {
                var stats = kvp.Value.GetStatistics();
                return new
                {
                    Path = kvp.Key,
                    Count = stats.Count,
                    AvgMs = Math.Round(stats.Avg, 2),
                    MinMs = Math.Round(stats.Min, 2),
                    MaxMs = Math.Round(stats.Max, 2),
                    P50 = Math.Round(stats.P50, 2),
                    P90 = Math.Round(stats.P90, 2),
                    P95 = Math.Round(stats.P95, 2),
                    P99 = Math.Round(stats.P99, 2),
                    QPS = Math.Round(stats.Qps, 2)
                };
            }).ToList();

            return Results.Ok(new { endpoints = result, timestamp = DateTime.UtcNow });
        });

        return app;
    }
}

public class RequestMetric
{
    public string Path { get; set; } = "";
    public string Method { get; set; } = "";
    public double LatencyMs { get; set; }
    public DateTime Timestamp { get; set; }
    public int StatusCode { get; set; }
}

public class LatencyRecorder
{
    private readonly string _path;
    private readonly MovingStatistics _stats = new();
    private readonly object _lock = new();
    private DateTime _windowStart = DateTime.UtcNow;

    public LatencyRecorder(string path) => _path = path;

    public void Record(double latencyMs)
    {
        lock (_lock)
        {
            _stats.Add(latencyMs);
        }
    }

    public LatencyStatistics GetStatistics()
    {
        lock (_lock)
        {
            return _stats.GetStatistics();
        }
    }
}

public class LatencyStatistics
{
    public int Count { get; set; }
    public double Avg { get; set; }
    public double Min { get; set; }
    public double Max { get; set; }
    public double P50 { get; set; }
    public double P90 { get; set; }
    public double P95 { get; set; }
    public double P99 { get; set; }
    public double Qps { get; set; }
}

public class MovingStatistics
{
    private readonly List<double> _values = new();
    private DateTime _startTime = DateTime.UtcNow;

    public void Add(double value)
    {
        _values.Add(value);
    }

    public LatencyStatistics GetStatistics()
    {
        if (_values.Count == 0)
            return new LatencyStatistics();

        var sorted = _values.OrderBy(x => x).ToList();
        var count = sorted.Count;
        var elapsed = (DateTime.UtcNow - _startTime).TotalSeconds;

        return new LatencyStatistics
        {
            Count = count,
            Avg = sorted.Average(),
            Min = sorted.First(),
            Max = sorted.Last(),
            P50 = GetPercentile(sorted, 0.50),
            P90 = GetPercentile(sorted, 0.90),
            P95 = GetPercentile(sorted, 0.95),
            P99 = GetPercentile(sorted, 0.99),
            Qps = elapsed > 0 ? count / elapsed : 0
        };
    }

    private static double GetPercentile(List<double> sorted, double percentile)
    {
        if (sorted.Count == 0) return 0;
        var index = (int)Math.Ceiling(sorted.Count * percentile) - 1;
        return sorted[Math.Min(index, sorted.Count - 1)];
    }
}

public static class PerformanceMetricsMiddlewareExtensions
{
    public static IApplicationBuilder UsePerformanceMetrics(this IApplicationBuilder app)
    {
        return app.UseMiddleware<PerformanceMetricsMiddleware>();
    }
}