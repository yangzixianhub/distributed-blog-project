using DistributedIdService.Api.Models;

namespace DistributedIdService.Api.Services;

public interface ISnowflakeIdGenerator
{
    long NextId();
    SnowflakeIdInfo GetIdInfo(long id);
    int WorkerId { get; }
    bool IsHealthy { get; }
}
