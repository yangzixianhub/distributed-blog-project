import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Java 客户端调用分布式雪花ID生成服务示例
 *
 * 功能：
 * 1. 生成单个ID
 * 2. 批量生成ID
 * 3. 解析ID详细信息
 * 4. 健康检查
 */
public class JavaClientExample {

    private static final String BASE_URL = "http://localhost:5000";
    private static final HttpClient httpClient;

    static {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("=== 分布式雪花ID生成服务 Java 客户端示例 ===\n");

        // 1. 健康检查
        System.out.println("1. 健康检查：");
        String health = getHealth();
        System.out.println(health);
        System.out.println();

        // 2. 生成单个ID
        System.out.println("2. 生成单个ID：");
        String singleId = generateSingleId();
        System.out.println("生成的ID: " + singleId);
        System.out.println();

        // 3. 批量生成ID
        System.out.println("3. 批量生成5个ID：");
        String batchIds = generateBatchIds(5);
        System.out.println(batchIds);
        System.out.println();

        // 4. 解析ID详情（使用上面生成的第一个ID）
        System.out.println("4. 解析ID详情：");
        if (singleId != null && !singleId.isEmpty()) {
            String idInfo = getIdInfo(singleId);
            System.out.println(idInfo);
        }
        System.out.println();

        // 5. 获取生成器状态
        System.out.println("5. 生成器状态：");
        String status = getStatus();
        System.out.println(status);
    }

    /**
     * 健康检查
     */
    public static String getHealth() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/health"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * 生成单个雪花ID
     */
    public static String generateSingleId() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/id"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * 批量生成雪花ID
     *
     * @param count 需要生成的数量（建议1-1000）
     */
    public static String generateBatchIds(int count) throws IOException, InterruptedException {
        String jsonBody = "{\"count\":" + count + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/id/batch"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * 解析ID详细信息
     *
     * @param id 雪花ID（长整型）
     */
    public static String getIdInfo(String id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/id/info/" + id))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * 获取生成器状态
     */
    public static String getStatus() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/id/status"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
