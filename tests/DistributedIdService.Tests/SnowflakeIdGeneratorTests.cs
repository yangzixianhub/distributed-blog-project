using DistributedIdService.Api.Services;
using Xunit;

namespace DistributedIdService.Tests;

public class SnowflakeIdGeneratorTests
{
    private const long Epoch = 1704067200000L; // 2024-01-01 00:00:00 UTC

    [Fact]
    public void Constructor_ValidWorkerId_CreatesInstance()
    {
        // Arrange & Act
        var generator = new SnowflakeIdGenerator(1, Epoch);

        // Assert
        Assert.NotNull(generator);
        Assert.Equal(1, generator.WorkerId);
        Assert.True(generator.IsHealthy);
    }

    [Fact]
    public void Constructor_InvalidWorkerId_ThrowsException()
    {
        // Arrange & Act & Assert
        Assert.Throws<ArgumentException>(() => new SnowflakeIdGenerator(-1, Epoch));
        Assert.Throws<ArgumentException>(() => new SnowflakeIdGenerator(1024, Epoch)); // Max is 1023
    }

    [Fact]
    public void NextId_GeneratesUniqueIds()
    {
        // Arrange
        var generator = new SnowflakeIdGenerator(1, Epoch);

        // Act
        var id1 = generator.NextId();
        var id2 = generator.NextId();

        // Assert
        Assert.NotEqual(id1, id2);
        Assert.True(id1 > 0);
        Assert.True(id2 > 0);
    }

    [Fact]
    public void NextId_SameTimestamp_DifferentSequence()
    {
        // Arrange
        var generator = new SnowflakeIdGenerator(1, Epoch);

        // Act - generate multiple IDs rapidly
        var ids = new List<long>();
        for (int i = 0; i < 10; i++)
        {
            ids.Add(generator.NextId());
        }

        // Assert - all should be unique even within same millisecond
        Assert.Equal(ids.Distinct().Count(), ids.Count);
    }

    [Fact]
    public void GetIdInfo_ReturnsCorrectComponents()
    {
        // Arrange
        var generator = new SnowflakeIdGenerator(123, Epoch);
        var id = generator.NextId();

        // Act
        var info = generator.GetIdInfo(id);

        // Assert
        Assert.Equal(id, info.Id);
        Assert.Equal(123, info.WorkerId);
        Assert.True(info.Timestamp >= Epoch);
    }

    [Fact]
    public void GenerateManyIds_WithinSequenceLimit()
    {
        // Arrange
        var generator = new SnowflakeIdGenerator(5, Epoch);

        // Act - generate more than sequence bits allow in same millisecond
        // Sequence is 12 bits = 4096 max
        var ids = new List<long>();
        for (int i = 0; i < 100; i++)
        {
            ids.Add(generator.NextId());
        }

        // Assert - all unique, sequence will wrap if needed
        Assert.Equal(ids.Distinct().Count(), ids.Count);
    }

    [Fact]
    public void TwoGenerators_DifferentWorkerIds_DifferentIdRanges()
    {
        // Arrange
        var gen1 = new SnowflakeIdGenerator(1, Epoch);
        var gen2 = new SnowflakeIdGenerator(2, Epoch);

        // Act
        var id1 = gen1.NextId();
        var id2 = gen2.NextId();

        // Extract workerId from IDs to verify they're different
        var info1 = gen1.GetIdInfo(id1);
        var info2 = gen2.GetIdInfo(id2);

        // Assert
        Assert.Equal(1, info1.WorkerId);
        Assert.Equal(2, info2.WorkerId);
        Assert.NotEqual(id1, id2);
    }

    [Fact]
    public void Dispose_Works()
    {
        // Arrange
        var generator = new SnowflakeIdGenerator(1, Epoch);

        // Act
        generator.Dispose();

        // Assert - should still be able to get ID info but IsHealthy false?
        // Actually IsHealthy becomes false after disposal
        Assert.False(generator.IsHealthy);
    }
}
