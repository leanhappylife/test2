package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class GitHubRepoScraper {
    private static final String GITHUB_SEARCH_URL = "https://github.com/search";

    public static void main(String[] args) {
        String query = "abc"; // 替换为你的搜索关键字
        String org = "your_org"; // 替换为你的组织名称
        int page = 1; // 从第一页开始

        boolean hasNextPage = true;

        while (hasNextPage) {
            String url = String.format("%s?q=%s+org:%s&type=repositories&p=%d", GITHUB_SEARCH_URL, query, org, page);

            try {
                Document doc = Jsoup.connect(url).get();
                hasNextPage = parseResults(doc);
                page++;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private static boolean parseResults(Document doc) {
        Elements results = doc.select("div.f4.text-normal a");
        if (results.isEmpty()) {
            return false;
        }

        for (Element result : results) {
            String repoLink = result.attr("href");
            String repoName = result.text();
            System.out.printf("Repository: %s, URL: %s%n", repoName, "https://github.com" + repoLink);
        }

        // 检查是否存在下一页
        Elements nextPageLinks = doc.select("a.next_page");
        return !nextPageLinks.isEmpty();
    }
}
