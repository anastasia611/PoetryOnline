import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Youtube {

    public static WordEntry[] getSubtitlesFromLib(String videoId) throws YoutubeDLException, URISyntaxException {
        String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

        // Destination directory
        URL url = Youtube.class.getClassLoader().getResource("subs");
        File file = Paths.get(url.toURI()).toFile();
        String directory = file.getAbsolutePath();

        // Build request
        YoutubeDLRequest request = new YoutubeDLRequest(videoUrl, directory);
        request.setOption("ignore-errors");
        request.setOption("output", "%(id)s");
        request.setOption("retries", 10);
        request.setOption("write-auto-sub");
        request.setOption("sub-lang", "ru");
        request.setOption("skip-download");
        request.setOption("convert-subs", "srt");

//        YoutubeDLResponse response = YoutubeDL.execute(request);
//        String stdOut = response.getOut(); // Executable output
//
//        System.out.println(stdOut);

        try {
            String subs = Resource.getFromResource("subs/" + videoId + ".ru.vtt");
            return SubsParser.parseSubs(subs);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return new WordEntry[]{};
    }

    public static String[] getSubtitles(String videoId) {
        String[] resultWords = new String[]{};
        String json = "";
        String host = "https://savesubs.com";

        try {
            json = Resource.getFromResource("youtube-response.html");

//            json = getSubsLinkFromSite(videoId);
//
//            JSONObject obj = new JSONObject(json);
//            JSONArray arr = obj.getJSONArray("formats");
//            String saveUrl = arr.getJSONObject(0).getString("url");
//            String title = obj.getString("title");
//
//            String url = host + saveUrl + "?fileName=" + title + " - {lang_name}&stripAngle=false&stripParentheses=false&stripCurly=false&stripSquare=false&stripMusicCues=false&ext=txt";
//            List<String> subs = getSubsFileFromSite(url);

            String file = Resource.getFromResource("subs.txt");
            resultWords = file.split("[\\r\\n]+");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return resultWords;
    }

    public static String getSubsLinkFromSite(String videoId) throws IOException {
        String html = "";
        String urlParam = URLEncoder.encode("https://www.youtube.com/watch?v=" + videoId, StandardCharsets.UTF_8);
        String jsonInputString = "{\"url\":\"" + urlParam + "\"}";
        jsonInputString = "{\"url\":\"https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DvoL5_TYPgCU\"}";
        byte[] postData = jsonInputString.getBytes(StandardCharsets.UTF_8);

        int postDataLength = postData.length;
        String request = "https://savesubs.com/action/get";
        URL url = new URL(request);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));

        conn.setRequestProperty("authority", "savesubs.com");
        conn.setRequestProperty("pragma", "no-cache");
        conn.setRequestProperty("cache-control", "no-cache");
        conn.setRequestProperty("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        conn.setRequestProperty("x-requested-with", "xmlhttprequest");
        conn.setRequestProperty("x-token", "4f453036786837786566");
        conn.setRequestProperty("cookie", "__cfduid=d688b22764a58fc5c9b248b4ba57aa3401614290959; PHPSESSID=2tpqmcgaj14qfroqoqdqfq4lii; user_locale=ru; isMessageShown=1; userSettings=a%3A5%3A%7Bs%3A10%3A%22stripAngle%22%3Bb%3A0%3Bs%3A16%3A%22stripParentheses%22%3Bb%3A0%3Bs%3A10%3A%22stripCurly%22%3Bb%3A0%3Bs%3A11%3A%22stripSquare%22%3Bb%3A0%3Bs%3A14%3A%22stripMusicCues%22%3Bb%3A0%3B%7D; __cf_bm=b3f7e40b601b2f3b3c1c71564c532d60fdacc29c-1614461714-1800-AXZ8v030tc2Sb2Q3+y4+8PfSzviactVQp7MGTgfvNAI5eznlLq1yyztZFr/691BftVtc34S2NJ2SehBtxKrUEyIeePQ047laxnKEvnA2oBq0mbGR4h1RVU2HESC7pCFrNQ==");

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData, 0, postDataLength);
        }

        try (DataInputStream r = new DataInputStream(conn.getInputStream())) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(r));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            html = result.toString();
        }

        return html;
    }

    private static List<String> getSubsFileFromSite(String href) throws IOException {
        List<String> res = new ArrayList<>();

        URL url = new URL(href);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            res.add(inputLine);
        }
        in.close();

        return res;
    }

    public static String getVideoPage(String videoId) {
        try {
            String html = "";

            String request = "https://youtube.com?v=" + videoId;
            URL url = new URL(request);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            html = response.toString();
            return html;

        } catch (Exception e) {
            return "";
        }
    }
}