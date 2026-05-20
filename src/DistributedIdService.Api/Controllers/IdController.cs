using DistributedIdService.Api.Models;
using DistributedIdService.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace DistributedIdService.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class IdController : ControllerBase
{
    private readonly ISnowflakeIdGenerator _idGenerator;
    private readonly ILogger<IdController> _logger;

    public IdController(ISnowflakeIdGenerator idGenerator, ILogger<IdController> logger)
    {
        _idGenerator = idGenerator;
        _logger = logger;
    }

    /// <summary>
    /// 生成单个雪花ID
    /// </summary>
    [HttpGet]
    public ActionResult<ApiResponse<long>> Get()
    {
        try
        {
            var id = _idGenerator.NextId();
            return Ok(ApiResponse<long>.Ok(id, "ID generated successfully"));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to generate ID");
            return StatusCode(500, ApiResponse<long>.Fail("Failed to generate ID"));
        }
    }

    /// <summary>
    /// 批量生成雪花ID
    /// </summary>
    [HttpPost("batch")]
    public ActionResult<ApiResponse<List<long>>> Batch([FromBody] BatchRequest request)
    {
        try
        {
            if (request.Count <= 0 || request.Count > 1000)
            {
                return BadRequest(ApiResponse<List<long>>.Fail("Count must be between 1 and 1000"));
            }

            var ids = new List<long>(request.Count);
            for (var i = 0; i < request.Count; i++)
            {
                ids.Add(_idGenerator.NextId());
            }

            return Ok(ApiResponse<List<long>>.Ok(ids, $"Generated {ids.Count} IDs"));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to generate batch IDs");
            return StatusCode(500, ApiResponse<List<long>>.Fail("Failed to generate batch IDs"));
        }
    }

    /// <summary>
    /// 解析雪花ID的详细信息
    /// </summary>
    [HttpGet("info/{id:long}")]
    public ActionResult<ApiResponse<SnowflakeIdInfo>> GetInfo(long id)
    {
        try
        {
            var info = _idGenerator.GetIdInfo(id);
            return Ok(ApiResponse<SnowflakeIdInfo>.Ok(info, "ID info retrieved"));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to get ID info for {Id}", id);
            return StatusCode(500, ApiResponse<SnowflakeIdInfo>.Fail("Failed to get ID info"));
        }
    }

    /// <summary>
    /// 获取生成器配置和状态
    /// </summary>
    [HttpGet("status")]
    public ActionResult<ApiResponse<object>> GetStatus()
    {
        var status = new
        {
            workerId = _idGenerator.WorkerId,
            isHealthy = _idGenerator.IsHealthy,
            timestamp = DateTime.UtcNow
        };

        return Ok(ApiResponse<object>.Ok(status, "Status retrieved"));
    }
}

public class BatchRequest
{
    public int Count { get; set; } = 1;
}
