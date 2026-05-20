using DistributedIdService.Api.Services;
using Xunit;

namespace DistributedIdService.Tests;

public class CircuitBreakerTests
{
    [Fact]
    public void Constructor_DefaultParams_Valid()
    {
        // Arrange & Act
        var breaker = new CircuitBreaker("test");

        // Assert
        Assert.Equal(CircuitState.Closed, breaker.State);
    }

    [Fact]
    public void Execute_Success_NoChange()
    {
        // Arrange
        var breaker = new CircuitBreaker("test", failureThreshold: 3);
        var callCount = 0;

        // Act
        var result = breaker.Execute<string>(() =>
        {
            callCount++;
            return "success";
        });

        // Assert
        Assert.Equal("success", result);
        Assert.Equal(CircuitState.Closed, breaker.State);
        Assert.Equal(0, breaker.FailureCount);
    }

    [Fact]
    public void Execute_Failure_RecordsFailure()
    {
        // Arrange
        var breaker = new CircuitBreaker("test", failureThreshold: 3);

        // Act
        for (int i = 0; i < 2; i++)
        {
            try
            {
                breaker.Execute<object>(() => throw new Exception("fail"));
            }
            catch { }
        }

        // Assert
        Assert.Equal(2, breaker.FailureCount);
        Assert.Equal(CircuitState.Closed, breaker.State);
    }

    [Fact]
    public void Execute_MultipleFailures_OpensCircuit()
    {
        // Arrange
        var breaker = new CircuitBreaker("test", failureThreshold: 3);

        // Act - cause failures to trigger open
        for (int i = 0; i < 3; i++)
        {
            try
            {
                breaker.Execute<object>(() => throw new Exception("fail"));
            }
            catch { }
        }

        // Assert
        Assert.Equal(CircuitState.Open, breaker.State);

        // After open, single-arg Execute should return default(T) without throwing
        var result = breaker.Execute<object>(() => { throw new Exception("shouldn't run"); });
        Assert.Null(result);
    }

    [Fact]
    public void Execute_Fallback_ReturnsFallbackOnCircuitOpen()
    {
        // Arrange
        var breaker = new CircuitBreaker("test", failureThreshold: 1);

        // Act - trigger circuit open
        try
        {
            breaker.Execute<object>(() => throw new Exception("fail"));
        }
        catch { }

        var result = breaker.Execute(
            () => throw new Exception("main"),
            () => "fallback");

        // Assert
        Assert.Equal("fallback", result);
    }

    [Fact]
    public void Reset_ClosesCircuit()
    {
        // Arrange
        var breaker = new CircuitBreaker("test", failureThreshold: 1);

        // Trigger circuit open
        try
        {
            breaker.Execute<object>(() => throw new Exception("fail"));
        }
        catch { }

        // Act
        breaker.Reset();

        // Assert
        Assert.Equal(CircuitState.Closed, breaker.State);
        Assert.Equal(0, breaker.FailureCount);
    }
}
