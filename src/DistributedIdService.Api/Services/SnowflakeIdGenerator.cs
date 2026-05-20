using DistributedIdService.Api.Models;

namespace DistributedIdService.Api.Services;

public class SnowflakeIdGenerator : ISnowflakeIdGenerator, IDisposable
{
    // Snowflake 参数配置
    private const int TimestampBits = 41;      // 时间戳位数
    private const int WorkerIdBits = 10;      // 工作机器ID位数
    private const int SequenceBits = 12;      // 序列号位数

    private const long MaxWorkerId = -1L ^ (-1L << WorkerIdBits);
    private const long MaxSequence = -1L ^ (-1L << SequenceBits);

    private readonly int _workerId;
    private readonly long _epoch; // 自定义起始时间戳

    private long _sequence = 0L;
    private long _lastTimestamp = -1L;
    private readonly object _lock = new();

    public int WorkerId => _workerId;
    public bool IsHealthy => _disposed == false;

    private bool _disposed = false;

    public SnowflakeIdGenerator(int workerId, long epoch)
    {
        if (workerId < 0 || workerId > MaxWorkerId)
            throw new ArgumentException($"WorkerId must be between 0 and {MaxWorkerId}");

        _workerId = workerId;
        _epoch = epoch;
    }

    public long NextId()
    {
        lock (_lock)
        {
            var timestamp = WaitNextMillis();

            if (timestamp < _lastTimestamp)
            {
                throw new InvalidOperationException(
                    $"Clock moved backwards. Refusing to generate id for {_lastTimestamp - timestamp} milliseconds");
            }

            if (timestamp == _lastTimestamp)
            {
                _sequence = (_sequence + 1) & MaxSequence;
                if (_sequence == 0)
                {
                    timestamp = WaitNextMillis();
                }
            }
            else
            {
                _sequence = 0L;
            }

            _lastTimestamp = timestamp;

            var id = ((timestamp - _epoch) << (WorkerIdBits + SequenceBits))
                    | (_workerId << SequenceBits)
                    | _sequence;

            return id;
        }
    }

    public SnowflakeIdInfo GetIdInfo(long id)
    {
        var sequenceMask = -1L ^ (-1L << SequenceBits);
        var workerIdMask = -1L ^ (-1L << WorkerIdBits);

        var sequence = id & sequenceMask;
        var workerId = (id >> SequenceBits) & workerIdMask;
        var timestamp = (id >> (WorkerIdBits + SequenceBits)) + _epoch;

        return new SnowflakeIdInfo
        {
            Id = id,
            Timestamp = timestamp,
            WorkerId = (int)workerId,
            Sequence = (int)sequence,
            GeneratedAt = DateTimeOffset.FromUnixTimeMilliseconds(timestamp).DateTime
        };
    }

    private long WaitNextMillis()
    {
        var timestamp = GetCurrentTimestamp();
        while (timestamp <= _lastTimestamp)
        {
            Thread.SpinWait(10);
            timestamp = GetCurrentTimestamp();
        }
        return timestamp;
    }

    private long GetCurrentTimestamp()
    {
        return DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
    }

    public void Dispose()
    {
        _disposed = true;
        GC.SuppressFinalize(this);
    }
}
