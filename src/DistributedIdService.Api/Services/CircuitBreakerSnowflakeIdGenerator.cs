using Microsoft.Extensions.Logging;
using DistributedIdService.Api.Models;
using DistributedIdService.Api.Services;

namespace DistributedIdService.Api.Services;

/// <summary>
/// 带熔断器的雪花ID生成器装饰器
/// </summary>
public class CircuitBreakerSnowflakeIdGenerator : ISnowflakeIdGenerator, IDisposable
{
    private readonly ISnowflakeIdGenerator _inner;
    private readonly CircuitBreaker _breaker;
    private readonly ILogger<CircuitBreakerSnowflakeIdGenerator> _logger;

    public int WorkerId => _inner.WorkerId;
    public bool IsHealthy => _inner.IsHealthy && _breaker.State != CircuitState.Open;

    public CircuitBreakerSnowflakeIdGenerator(
        ISnowflakeIdGenerator inner,
        CircuitBreaker breaker,
        ILogger<CircuitBreakerSnowflakeIdGenerator> logger)
    {
        _inner = inner ?? throw new ArgumentNullException(nameof(inner));
        _breaker = breaker ?? throw new ArgumentNullException(nameof(breaker));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    public long NextId()
    {
        return _breaker.Execute(
            () => _inner.NextId(),
            () =>
            {
                _logger.LogWarning("Circuit breaker is open. Returning fallback value 0.");
                return 0L;
            });
    }

    public SnowflakeIdInfo GetIdInfo(long id)
    {
        return _breaker.Execute(
            () => _inner.GetIdInfo(id),
            () =>
            {
                _logger.LogWarning("Circuit breaker is open. Returning default SnowflakeIdInfo.");
                return new SnowflakeIdInfo();
            });
    }

    public void Dispose()
    {
        if (_inner is IDisposable disposableInner)
        {
            disposableInner.Dispose();
        }
        _breaker.Dispose();
    }
}
