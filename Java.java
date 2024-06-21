import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class GitHubCodeSearch {

    private static final String TOKEN = "YOUR_GITHUB_PERSONAL_ACCESS_TOKEN";
    private static final String QUERY = "abc+org:openai+is:public+archived:false";
    private static final int PAGE = 1;

    public static void main(String[] args) {
        try {
            String url = String.format("https://api.github.com/search/code?q=%s&page=%d", QUERY, PAGE);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "token " + TOKEN);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

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

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String fileName = item.getString("name");
                String repoName = item.getJSONObject("repository").getString("full_name");
                System.out.printf("File: %s, Repository: %s%n", fileName, repoName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
