namespace DistributedIdService.Api.Models;

public class SnowflakeIdInfo
{
    public long Id { get; set; }
    public long Timestamp { get; set; }
    public int WorkerId { get; set; }
    public int Sequence { get; set; }
    public DateTime GeneratedAt { get; set; }
}
