namespace DistributedIdService.Api.Services;

public enum CircuitState
{
    Closed,    // 关闭：请求正常通过
    Open,      // 打开：快速失败，不调用实际服务
    HalfOpen   // 半开：允许部分请求通过以探测服务是否恢复
}

public class CircuitBreakerException : Exception
{
    public CircuitBreakerException(string message) : base(message) { }
}

/// <summary>
/// 熔断器实现
/// </summary>
public class CircuitBreaker : IDisposable
{
    private readonly string _name;
    private readonly int _failureThreshold;
    private readonly TimeSpan _openStateDuration;
    private readonly int _halfOpenMaxCalls;

    private CircuitState _state;
    private int _failureCount;
    private int _successCount;
    private DateTime _stateChangedAt;
    private readonly object _lock = new();

    public CircuitState State
    {
        get
        {
            CheckStateTransition();
            return _state;
        }
    }

    public int FailureCount => _failureCount;
    public int SuccessCount => _successCount;
    public DateTime StateChangedAt => _stateChangedAt;

    public CircuitBreaker(string name, int failureThreshold = 5, TimeSpan? openStateDuration = null, int halfOpenMaxCalls = 3)
    {
        _name = name ?? throw new ArgumentNullException(nameof(name));
        _failureThreshold = failureThreshold > 0 ? failureThreshold : throw new ArgumentException("Failure threshold must be > 0");
        _openStateDuration = openStateDuration ?? TimeSpan.FromSeconds(30);
        _halfOpenMaxCalls = halfOpenMaxCalls > 0 ? halfOpenMaxCalls : throw new ArgumentException("Half-open max calls must be > 0");

        _state = CircuitState.Closed;
        _stateChangedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// 执行受熔断器保护的操作
    /// </summary>
    public T Execute<T>(Func<T> action)
    {
        return Execute(action, () => default!);
    }

    /// <summary>
    /// 异步执行受熔断器保护的操作
    /// </summary>
    public async Task<T> ExecuteAsync<T>(Func<Task<T>> action)
    {
        return await ExecuteAsync(action, () => Task.FromResult<T>(default!));
    }

    /// <summary>
    /// 执行受熔断器保护的操作，失败时提供回退
    /// </summary>
    public T Execute<T>(Func<T> action, Func<T> fallback)
    {
        try
        {
            return ExecuteCore(action);
        }
        catch (CircuitBreakerException)
        {
            return fallback();
        }
    }

    /// <summary>
    /// 异步执行受熔断器保护的操作，失败时提供回退
    /// </summary>
    public async Task<T> ExecuteAsync<T>(Func<Task<T>> action, Func<Task<T>> fallback)
    {
        try
        {
            return await ExecuteCoreAsync(action);
        }
        catch (CircuitBreakerException)
        {
            return await fallback();
        }
    }

    private T ExecuteCore<T>(Func<T> action)
    {
        CheckStateTransition();

        if (_state == CircuitState.Open)
        {
            throw new CircuitBreakerException($"Circuit breaker '{_name}' is open. Request rejected.");
        }

        try
        {
            var result = action();

            if (_state == CircuitState.HalfOpen)
            {
                RecordSuccess();
            }

            return result;
        }
        catch (Exception ex) when (ex is not CircuitBreakerException)
        {
            RecordFailure();
            throw;
        }
    }

    private async Task<T> ExecuteCoreAsync<T>(Func<Task<T>> action)
    {
        CheckStateTransition();

        if (_state == CircuitState.Open)
        {
            throw new CircuitBreakerException($"Circuit breaker '{_name}' is open. Request rejected.");
        }

        try
        {
            var result = await action();

            if (_state == CircuitState.HalfOpen)
            {
                RecordSuccess();
            }

            return result;
        }
        catch (Exception ex) when (ex is not CircuitBreakerException)
        {
            RecordFailure();
            throw;
        }
    }

    private void CheckStateTransition()
    {
        lock (_lock)
        {
            switch (_state)
            {
                case CircuitState.Open:
                    if (DateTime.UtcNow - _stateChangedAt > _openStateDuration)
                    {
                        TransitionTo(CircuitState.HalfOpen);
                    }
                    break;

                case CircuitState.HalfOpen:
                    // 在半开状态下，只允许有限数量的调用
                    // 实际限制在ExecuteCore中处理
                    break;
            }
        }
    }

    private void RecordSuccess()
    {
        lock (_lock)
        {
            if (_state == CircuitState.HalfOpen)
            {
                _successCount++;

                if (_successCount >= _halfOpenMaxCalls)
                {
                    // 半开状态成功达到阈值，关闭熔断器
                    TransitionTo(CircuitState.Closed);
                }
            }
            else if (_state == CircuitState.Closed)
            {
                // 在关闭状态成功，重置失败计数
                if (_failureCount > 0)
                {
                    _failureCount = Math.Max(0, _failureCount / 2); // 衰减机制
                }
            }
        }
    }

    private void RecordFailure()
    {
        lock (_lock)
        {
            _failureCount++;

            if (_state == CircuitState.HalfOpen)
            {
                // 半开状态下的任何失败都应该重新打开熔断器
                TransitionTo(CircuitState.Open);
            }
            else if (_state == CircuitState.Closed && _failureCount >= _failureThreshold)
            {
                // 关闭状态下失败达到阈值，打开熔断器
                TransitionTo(CircuitState.Open);
            }
        }
    }

    private void TransitionTo(CircuitState newState)
    {
        _state = newState;
        _stateChangedAt = DateTime.UtcNow;

        if (newState == CircuitState.Closed)
        {
            _failureCount = 0;
            _successCount = 0;
        }
        else if (newState == CircuitState.HalfOpen)
        {
            _successCount = 0;
        }
    }

    /// <summary>
    /// 手动重置熔断器
    /// </summary>
    public void Reset()
    {
        lock (_lock)
        {
            TransitionTo(CircuitState.Closed);
        }
    }

    /// <summary>
    /// 强制打开熔断器
    /// </summary>
    public void ForceOpen()
    {
        lock (_lock)
        {
            TransitionTo(CircuitState.Open);
        }
    }

    public void Dispose()
    {
        // 清理资源
        GC.SuppressFinalize(this);
    }
}
