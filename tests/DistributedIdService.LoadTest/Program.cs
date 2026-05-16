using System.Diagnostics;
using System.Net.Http.Json;
using System.Text.Json;

const string baseUrl = "http://localhost:5000";
const int defaultConcurrency = 50;
const int defaultDurationSeconds = 30;
const int warmupSeconds = 3;

// 解析命令行参数
var concurrency = args.Length > 0 ? int.Parse(args[0]) : defaultConcurrency;
var duration = args.Length > 1 ? int.Parse(args[1]) : defaultDurationSeconds;

Console.WriteLine($"=== 分布式ID服务压测工具 ===");
Console.WriteLine($"目标地址: {baseUrl}");
Console.WriteLine($"并发数: {concurrency}");
Console.WriteLine($"持续时间: {duration}秒");
Console.WriteLine($"预热时间: {warmupSeconds}秒");
Console.WriteLine();

var latencies = new List<double>();
var successCount = 0;
var failureCount = 0;
var lockObj = new object();
var cts = new CancellationTokenSource();

var httpClient = new HttpClient
{
    BaseAddress = new Uri(baseUrl),
    Timeout = TimeSpan.FromSeconds(30)
};

// 预热
Console.WriteLine("预热中...");
await Warmup(httpClient);
Console.WriteLine("预热完成，开始压测...");
Console.WriteLine();

// 记录开始时间
var startTime = DateTime.UtcNow;
var timer = new Timer(_ => PrintProgress(startTime, successCount, failureCount), null, TimeSpan.Zero, TimeSpan.FromSeconds(1));

// 并发压测
var tasks = new List<Task>();
for (var i = 0; i < concurrency; i++)
{
    var task = Task.Run(async () =>
    {
        var sw = new Stopwatch();
        while (!cts.Token.IsCancellationRequested)
        {
            sw.Restart();
            try
            {
                var response = await httpClient.GetAsync("/api/id");
                sw.Stop();

                if (response.IsSuccessStatusCode)
                {
                    var latency = sw.Elapsed.TotalMilliseconds;
                    lock (lockObj)
                    {
                        latencies.Add(latency);
                        successCount++;
                    }
                }
                else
                {
                    lock (lockObj) { failureCount++; }
                }
            }
            catch
            {
                lock (lockObj) { failureCount++; }
            }
        }
    });
    tasks.Add(task);
}

// 持续压测
await Task.Delay(TimeSpan.FromSeconds(duration));
cts.Cancel();

// 等待所有任务结束
await Task.WhenAll(tasks);
timer.Dispose();

Console.WriteLine();
Console.WriteLine("=== 压测结果 ===");
PrintStatistics(latencies, successCount, failureCount);

// 额外获取服务端指标
Console.WriteLine();
Console.WriteLine("=== 服务端指标 ===");
try
{
    var metricsResponse = await httpClient.GetAsync("/metrics");
    if (metricsResponse.IsSuccessStatusCode)
    {
        var json = await metricsResponse.Content.ReadAsStringAsync();
        Console.WriteLine(json);
    }
}
catch (Exception ex)
{
    Console.WriteLine($"无法获取服务端指标: {ex.Message}");
}

static async Task Warmup(HttpClient client)
{
    for (var i = 0; i < 10; i++)
    {
        await client.GetAsync("/api/id");
    }
}

static void PrintProgress(DateTime startTime, int success, int failure)
{
    var elapsed = (DateTime.UtcNow - startTime).TotalSeconds;
    var qps = success / elapsed;
    Console.Write($"\r已运行: {elapsed:F0}s | 成功: {success} | 失败: {failure} | QPS: {qps:F0}    ");
}

static void PrintStatistics(List<double> latencies, int success, int failure)
{
    if (latencies.Count == 0)
    {
        Console.WriteLine("没有成功请求");
        return;
    }

    var sorted = latencies.OrderBy(x => x).ToList();
    var count = sorted.Count;
    var totalTime = sorted.Sum();
    var elapsed = 1.0; // 避免除零

    Console.WriteLine($"总请求数: {success + failure} (成功: {success}, 失败: {failure})");
    Console.WriteLine($"总耗时: {(sorted.Max() - sorted.Min()):F2}ms");
    Console.WriteLine();
    Console.WriteLine("--- 延迟分布 ---");
    Console.WriteLine($"  最小值:  {sorted.First():F2}ms");
    Console.WriteLine($"  平均值:  {sorted.Average():F2}ms");
    Console.WriteLine($"  最大值:  {sorted.Last():F2}ms");
    Console.WriteLine($"  P50:     {GetPercentile(sorted, 0.50):F2}ms");
    Console.WriteLine($"  P90:     {GetPercentile(sorted, 0.90):F2}ms");
    Console.WriteLine($"  P95:     {GetPercentile(sorted, 0.95):F2}ms");
    Console.WriteLine($"  P99:     {GetPercentile(sorted, 0.99):F2}ms");
    Console.WriteLine($"  P99.9:   {GetPercentile(sorted, 0.999):F2}ms");
}

static double GetPercentile(List<double> sorted, double percentile)
{
    if (sorted.Count == 0) return 0;
    var index = (int)Math.Ceiling(sorted.Count * percentile) - 1;
    return sorted[Math.Max(0, Math.Min(index, sorted.Count - 1))];
}