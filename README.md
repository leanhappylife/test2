package com.example;

import java.io.IOException;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GitHubSearchAnalyzer {

    private static final String BASE_URL = "https://github.com/search?q=abc&type=repositories&p=";

    public static void main(String[] args) throws IOException {
        CookieStore cookieStore = new BasicCookieStore();
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).setDefaultRequestConfig(globalConfig).build();

        // 模拟登录
        boolean loginSuccess = simulateLogin(client, cookieStore);
        if (!loginSuccess) {
            System.out.println("Login failed");
            return;
        }

        int page = 1;
        boolean hasMorePages = true;

        while (hasMorePages) {
            String url = BASE_URL + page;
            System.out.println("Fetching page: " + page);

            // 发送HTTP请求并获取响应
            HttpGet request = new HttpGet(url);

            try (CloseableHttpResponse response = client.execute(request)) {
                String html = EntityUtils.toString(response.getEntity());
                // 解析HTML并检查是否有更多页面
                hasMorePages = parseAndPrintResults(html);
                page++;
            } catch (IOException e) {
                e.printStackTrace();
                hasMorePages = false; // 如果出现异常，则停止抓取
            }
        }

        System.out.println("Finished fetching all pages.");
        client.close();
    }

    private static boolean simulateLogin(CloseableHttpClient client, CookieStore cookieStore) throws IOException {
        // 创建并配置登录请求
        HttpPost loginPost = new HttpPost("https://github.com/session");
        loginPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        // 设置登录所需的用户名和密码 (确保替换为您的GitHub用户名和密码)
        List<NameValuePair> credentials = new ArrayList<>();
        credentials.add(new BasicNameValuePair("login", "your_username"));
        credentials.add(new BasicNameValuePair("password", "your_password"));
        loginPost.setEntity(new UrlEncodedFormEntity(credentials));

        // 发送登录请求
        try (CloseableHttpResponse loginResponse = client.execute(loginPost)) {
            int statusCode = loginResponse.getStatusLine().getStatusCode();
            if (statusCode == 200 || statusCode == 302) {
                return true; // 登录成功
            }
        }

        return false; // 登录失败
    }

    private static boolean parseAndPrintResults(String html) {
        Document doc = Jsoup.parse(html);

        // 选择包含仓库信息的HTML元素
        Elements repoElements = doc.select("div.f4.text-normal");

        // 如果没有找到仓库元素，返回false表示没有更多页面
        if (repoElements.isEmpty()) {
            return false;
        }

        // 迭代仓库元素并提取信息
        for (Element repoElement : repoElements) {
            Element linkElement = repoElement.selectFirst("a");
            if (linkElement != null) {
                String repoName = linkElement.text();
                String repoUrl = linkElement.attr("href");
                System.out.println("Repository: " + repoName);
                System.out.println("URL: " + "https://github.com" + repoUrl);
                System.out.println();
            }
        }

        // 检查是否有“下一页”按钮
        Elements nextPageElements = doc.select("a.next_page");
        return !nextPageElements.isEmpty();
    }
}
