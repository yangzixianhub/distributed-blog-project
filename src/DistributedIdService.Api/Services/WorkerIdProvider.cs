using Microsoft.Extensions.Logging;
using System.Text;

namespace DistributedIdService.Api.Services;

public interface IWorkerIdProvider
{
    int GetWorkerId();
    Task<int> GetWorkerIdAsync();
}

public class WorkerIdProvider : IWorkerIdProvider, IDisposable
{
    private readonly ILogger<WorkerIdProvider> _logger;
    private readonly string _workerIdFilePath;
    private readonly int? _configuredWorkerId;
    private readonly bool _useMachineId;
    private int? _cachedWorkerId;
    private bool _disposed = false;

    // WorkerId 有效范围：0 - 1023 (10位)
    public const int MaxWorkerId = 1023;

    public WorkerIdProvider(
        ILogger<WorkerIdProvider> logger,
        IConfiguration configuration)
    {
        _logger = logger;

        var workerIdConfig = configuration["WorkerId"];
        var machineIdEnabled = configuration.GetValue("UseMachineId", false);

        if (int.TryParse(workerIdConfig, out int configuredId))
        {
            _configuredWorkerId = configuredId;
            _logger.LogInformation("Using configured worker ID: {WorkerId}", configuredId);
        }

        _useMachineId = machineIdEnabled || string.IsNullOrEmpty(workerIdConfig);
        _workerIdFilePath = Path.Combine(AppContext.BaseDirectory, "worker.id");

        if (_useMachineId)
        {
            _logger.LogInformation("Using machine-based worker ID with file persistence");
        }
    }

    public int GetWorkerId()
    {
        if (_cachedWorkerId.HasValue)
        {
            return _cachedWorkerId.Value;
        }

        return GetOrCreateWorkerId();
    }

    public Task<int> GetWorkerIdAsync()
    {
        return Task.FromResult(GetWorkerId());
    }

    private int GetOrCreateWorkerId()
    {
        lock (this)
        {
            if (_cachedWorkerId.HasValue)
            {
                return _cachedWorkerId.Value;
            }

            // 1. 使用配置的 workerId
            if (_configuredWorkerId.HasValue)
            {
                return SetAndCacheWorkerId(_configuredWorkerId.Value);
            }

            // 2. 尝试从文件读取
            if (File.Exists(_workerIdFilePath))
            {
                try
                {
                    var content = File.ReadAllText(_workerIdFilePath).Trim();
                    if (int.TryParse(content, out int fileWorkerId))
                    {
                        if (IsValidWorkerId(fileWorkerId))
                        {
                            _logger.LogInformation("Loaded worker ID from file: {WorkerId}", fileWorkerId);
                            return SetAndCacheWorkerId(fileWorkerId);
                        }
                    }
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Failed to read worker ID from file");
                }
            }

            // 3. 生成基于机器标识的 workerId
            int generatedId = GenerateMachineBasedWorkerId();

            // 4. 持久化到文件
            try
            {
                File.WriteAllText(_workerIdFilePath, generatedId.ToString());
                _logger.LogInformation("Generated and saved new worker ID: {WorkerId}", generatedId);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to save worker ID to file");
            }

            return SetAndCacheWorkerId(generatedId);
        }
    }

    private int GenerateMachineBasedWorkerId()
    {
        // 使用机器名和进程ID生成一个相对稳定的ID
        var machineName = Environment.MachineName;
        var processId = Environment.ProcessId;
        var combined = $"{machineName}-{processId}";

        // 使用简单的哈希算法生成0-1023之间的数
        var hash = combined.GetHashCode();
        var workerId = Math.Abs(hash % (MaxWorkerId + 1));

        return workerId;
    }

    private int SetAndCacheWorkerId(int workerId)
    {
        if (!IsValidWorkerId(workerId))
        {
            throw new InvalidOperationException($"Invalid worker ID: {workerId}. Must be between 0 and {MaxWorkerId}");
        }

        _cachedWorkerId = workerId;
        _logger.LogDebug("Worker ID set to: {WorkerId}", workerId);
        return workerId;
    }

    private bool IsValidWorkerId(int workerId)
    {
        return workerId >= 0 && workerId <= MaxWorkerId;
    }

    public void Dispose()
    {
        _disposed = true;
        GC.SuppressFinalize(this);
    }
}
