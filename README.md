
我明白了，让我们确保提供的 Java 代码可以直接运行。以下是一个完整的、可以直接运行的 Java 项目，包含所有必要的依赖、导入和代码。

1. 设置 Maven 项目
首先，确保你有一个 Maven 项目，并在 pom.xml 中添加 OkHttp 和 Jackson 依赖。

pom.xml：
xml
复制代码
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>github-search</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.9.3</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.12.3</version>
        </dependency>
    </dependencies>
</project>
2. 创建 Java 文件
创建一个新的 Java 文件，例如 GitHubSearch.java，并将以下代码粘贴进去。

GitHubSearch.java：
java
复制代码
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class GitHubSearch {
    private static final String GITHUB_API_URL = "https://api.github.com/search/code";
    private static final String TOKEN = "your_github_token"; // 替换为你的 GitHub 访问令牌
    private static final String KEYWORD = "your_keyword"; // 替换为你的关键字
    private static final String EXCLUDED_SUFFIX = ".txt"; // 替换为你想排除的后缀
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        searchCode(KEYWORD, EXCLUDED_SUFFIX);
    }

    private static void searchCode(String keyword, String excludedSuffix) throws IOException {
        int page = 1;
        boolean hasResults = true;

        while (hasResults) {
            String url = String.format("%s?q=%s+in:file&page=%d", GITHUB_API_URL, keyword, page);
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "token " + TOKEN)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("Request failed: " + response);
                    return;
                }

                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    JsonNode jsonNode = mapper.readTree(responseBody.string());
                    JsonNode items = jsonNode.get("items");

                    if (items == null || !items.isArray() || items.size() == 0) {
                        hasResults = false;
                    } else {
                        for (JsonNode item : items) {
                            String filePath = item.get("path").asText();
                            if (!filePath.endsWith(excludedSuffix)) {
                                String repoFullName = item.get("repository").get("full_name").asText();
                                String htmlUrl = item.get("html_url").asText();
                                System.out.printf("Repository: %s, File: %s, URL: %s%n", repoFullName, filePath, htmlUrl);
                            }
                        }
                    }
                }
            }

            page++;
        }
    }
}
3. 替换变量并运行程序
将 TOKEN 替换为您的 GitHub 访问令牌。
将 KEYWORD 替换为您要搜索的关键字。
将 EXCLUDED_SUFFIX 替换为您要排除的文件后缀，例如 .txt。
4. 运行程序
确保您的 Maven 项目已正确配置并且所有依赖项都已下载。然后运行 GitHubSearch.java。

该程序会通过 GitHub API 搜索包含指定关键字但不包含特定后缀文件的代码，并输出结果。

如果有任何问题或需要进一步的帮助，请告诉我！
