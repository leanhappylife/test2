i java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.


@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
private <T, ID> void exportDataToCsv(JpaRepository<T, ID> repository, String fileName, String[] headers, Function<T, List<Object>> dataMapper) {
    int pageSize = 300;
    int pageNumber = 0;
    final int flushThreshold = 1000;
    final int[] recordCount = {0};

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false), 1024 * 1024);
         CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {

        while (true) {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            Page<T> page = repository.findAll(pageable);

            List<T> entities = new ArrayList<>(page.getContent());
            if (entities.isEmpty()) {
                break;
            }

            for (int i = 0; i < entities.size(); i++) {
                T entity = entities.get(i);
                csvPrinter.printRecord(dataMapper.apply(entity));
                recordCount[0]++;

                if (recordCount[0] % flushThreshold == 0) {
                    csvPrinter.flush();
                }

                // Detach the entity to release memory
                entityManager.detach(entity);
                entities.set(i, null); // Clear the local reference to help with garbage collection
            }

            // Clear the persistence context
            entityManager.clear();

            pageNumber++;
        }

        csvPrinter.flush();
        logger.info("CSV file created successfully: {}", fileName);

    } catch (IOException e) {
        logger.error("IOException occurred while creating CSV file: {}", fileName, e);
    } catch (Exception e) {
        logger.error("An unexpected error occurred while creating CSV file: {}", fileName, e);
    }
}

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

public class GitHubCodeSearch {

    private static final List<String> TOKENS = Arrays.asList(
        "YOUR_GITHUB_PERSONAL_ACCESS_TOKEN_1"
        // 你可以在这里添加更多令牌
    );

    private static final List<String> EXCLUDED_EXTENSIONS = Arrays.asList(".md", ".txt");
    private static final List<String> EXCLUDED_REPO_KEYWORDS = Arrays.asList("test", "example");

    public static void main(String[] args) {
        String searchKeyword = "abc";
        String organization = "openai";
        String query = generateQuery(searchKeyword, organization);
        int page = 1;
        boolean hasMoreResults = true;
        int tokenIndex = 0;
        int retryCount = 0;
        final int maxRetries = 2;

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("GitHub Search Results");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("File Name");
        headerRow.createCell(1).setCellValue("Repository Name");
        headerRow.createCell(2).setCellValue("Code Snippet");
        headerRow.createCell(3).setCellValue("Search Keyword");
        headerRow.createCell(4).setCellValue("File Type");
        headerRow.createCell(5).setCellValue("Visibility");
        headerRow.createCell(6).setCellValue("File URL");

        int rowNum = 1;

        try {
            while (hasMoreResults || retryCount < maxRetries) {
                try {
                    boolean success = false;
                    while (!success && tokenIndex < TOKENS.size()) {
                        String token = TOKENS.get(tokenIndex);
                        String url = String.format("https://api.github.com/search/code?q=%s&page=%d", query, page);
                        System.out.println("Request URL: " + url);  // 打印请求URL
                        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Authorization", "token " + token);
                        connection.setRequestProperty("Accept", "application/vnd.github.v3.text-match+json");

                        int responseCode = connection.getResponseCode();
                        if (responseCode == 403) {
                            // Rate limit hit, switch token
                            tokenIndex = (tokenIndex + 1) % TOKENS.size();
                            if (tokenIndex == 0) {
                                // All tokens exhausted, wait before retrying
                                System.out.println("Rate limit hit for all tokens. Waiting before retrying...");
                                Thread.sleep(60000); // Wait for 1 minute
                            }
                        } else if (responseCode == 200) {
                            success = true;
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String inputLine;
                            StringBuilder content = new StringBuilder();
                            while ((inputLine = in.readLine()) != null) {
                                content.append(inputLine);
                            }

                            in.close();
                            connection.disconnect();

                            JSONObject jsonResponse = new JSONObject(content.toString());
                            JSONArray items = jsonResponse.getJSONArray("items");

                            if (items.length() == 0) {
                                if (retryCount < maxRetries) {
                                    retryCount++;
                                    System.out.printf("No results found, retrying %d/%d%n", retryCount, maxRetries);
                                } else {
                                    hasMoreResults = false;
                                }
                            } else {
                                retryCount = 0; // reset retry count if results are found
                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject item = items.getJSONObject(i);
                                    String fileName = item.getString("name");
                                    String repoName = item.getJSONObject("repository").getString("full_name");
                                    boolean isPrivate = item.getJSONObject("repository").getBoolean("private");
                                    String visibility = isPrivate ? "Private" : "Public";
                                    String fileType = getFileExtension(fileName);
                                    String fileUrl = item.getString("html_url");

                                    if (isValidFile(fileName) && !containsExcludedRepoKeyword(repoName)) {
                                        Row row = sheet.createRow(rowNum++);
                                        row.createCell(0).setCellValue(fileName);
                                        row.createCell(1).setCellValue(repoName);

                                        JSONArray textMatches = item.getJSONArray("text_matches");
                                        StringBuilder snippets = new StringBuilder();
                                        for (int j = 0; j < textMatches.length(); j++) {
                                            JSONObject textMatch = textMatches.getJSONObject(j);
                                            String fragment = textMatch.getString("fragment");
                                            snippets.append(fragment).append("\n");
                                        }
                                        row.createCell(2).setCellValue(snippets.toString());
                                        row.createCell(3).setCellValue(searchKeyword);
                                        row.createCell(4).setCellValue(fileType);
                                        row.createCell(5).setCellValue(visibility);
                                        row.createCell(6).setCellValue(fileUrl);
                                    }
                                }
                                page++;
                            }
                        } else {
                            hasMoreResults = false;
                            System.out.printf("Unexpected response code: %d%n", responseCode);
                        }
                        
                        // 延迟每个请求，减少请求频率
                        Thread.sleep(2000); // 等待2秒
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (++retryCount >= maxRetries) {
                        hasMoreResults = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream("GitHubSearchResults.xlsx")) {
                workbook.write(fileOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String generateQuery(String keyword, String organization) {
        return String.format("%s+org:%s+is:public+archived:false", keyword, organization);
    }

    private static boolean isValidFile(String fileName) {
        for (String extension : EXCLUDED_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsExcludedRepoKeyword(String repoName) {
        for (String keyword : EXCLUDED_REPO_KEYWORDS) {
            if (repoName.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
}
