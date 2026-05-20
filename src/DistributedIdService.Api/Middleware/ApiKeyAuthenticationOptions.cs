namespace DistributedIdService.Api.Middleware;

public class ApiKeyAuthenticationOptions
{
    public const string SectionName = "ApiKeys";
    public const string HeaderName = "X-API-Key";

    public List<ApiKeyConfig> Keys { get; set; } = new();
    public Dictionary<string, ApiKeyConfig> KeysDictionary { get; set; } = new();
}

public class ApiKeyConfig
{
    public string Key { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public bool Enabled { get; set; } = true;
}