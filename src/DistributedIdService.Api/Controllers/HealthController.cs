using DistributedIdService.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace DistributedIdService.Api.Controllers;

[ApiController]
[Route("health")]
public class HealthController : ControllerBase
{
    private readonly ISnowflakeIdGenerator _idGenerator;
    private readonly ILogger<HealthController> _logger;

    public HealthController(ISnowflakeIdGenerator idGenerator, ILogger<HealthController> logger)
    {
        _idGenerator = idGenerator;
        _logger = logger;
    }

    /// <summary>
    /// 健康检查端点
    /// </summary>
    [HttpGet]
    public ActionResult<object> Get()
    {
        try
        {
            // 尝试生成ID以检查服务是否正常运行
            var testId = _idGenerator.NextId();
            var canGenerate = testId > 0;

            var health = new
            {
                status = canGenerate ? "Healthy" : "Unhealthy",
                timestamp = DateTime.UtcNow,
                workerId = _idGenerator.WorkerId,
                isGeneratorHealthy = _idGenerator.IsHealthy,
                // 通过生成测试ID验证功能
                testId = testId,
                testGenerationSuccessful = canGenerate
            };

            return canGenerate
                ? Ok(health)
                : StatusCode(503, health);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Health check failed");

            var errorHealth = new
            {
                status = "Unhealthy",
                timestamp = DateTime.UtcNow,
                error = ex.Message
            };

            return StatusCode(503, errorHealth);
        }
    }

    /// <summary>
    /// 详细健康检查，包含所有子组件状态
    /// </summary>
    [HttpGet("detailed")]
    public ActionResult<object> GetDetailed()
    {
        try
        {
            var id = _idGenerator.NextId();
            var info = _idGenerator.GetIdInfo(id);

            var health = new
            {
                overallStatus = "Healthy",
                timestamp = DateTime.UtcNow,
                components = new
                {
                    idGenerator = new
                    {
                        status = "Healthy",
                        workerId = _idGenerator.WorkerId,
                        isHealthy = _idGenerator.IsHealthy
                    }
                },
                test = new
                {
                    generatedId = id,
                    generationTime = info.GeneratedAt,
                    sequence = info.Sequence
                }
            };

            return Ok(health);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Detailed health check failed");

            var errorHealth = new
            {
                overallStatus = "Unhealthy",
                timestamp = DateTime.UtcNow,
                error = ex.Message,
                stackTrace = ex.StackTrace
            };

            return StatusCode(503, errorHealth);
        }
    }
}
