package rhymes;

import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class Rhymes {
    public static RhymesResult getRhymes(String word, int quality) {
        String[] resultWords;

        try {
            String html = getRhymesResponse(word, quality);
            String wordWithStress = "";

            Document doc = Jsoup.parse(html);
            Element result = doc.select("#results").get(0);
            Elements results = result.children();

            Elements wordElements = doc.select("#results .caps");
            if (wordElements.size() > 0) {
                Element wordElement = wordElements.get(0);
                for (Element child : wordElement.children()) {
                    if (child.tag().getName().equals("u")) {
                        String letter = child.text();
                        child.replaceWith(new TextNode(letter.toUpperCase()));
                    }
                }
                wordWithStress = wordElement.text();
            }

            Elements errors = doc.select(".error");
            for (Element error : errors) {
                if (error.getElementsContainingText("ударение").size() > 0) {
                    return new RhymesResult(ErrorCodes.STRESS_NEEDED);
                }
            }

            int i = 0;
            while (i < results.size() && !results.get(i).classNames().contains("pos")) {
                results.get(i++).remove();
            }

            for (Element child : results) {
                if (child.classNames().contains("pos")) {
                    child.remove();
                }
                if (child.tag().getName().equals("u")) {
                    String letter = child.text();
                    child.replaceWith(new TextNode(letter.toUpperCase()));
                }
            }

            StringBuilder txt = new StringBuilder(result.text().replaceAll(",", ""));

            int begInd = 0;
            int endInd;

            for (int j = 0; j < txt.length(); j++) {
                char ch = txt.charAt(j);

                if (ch == '(' || ch == '[') {
                    begInd = j;
                } else if (ch == ')' || ch == ']') {
                    boolean necessary = ch == ']';
                    String optsStr = txt.substring(begInd + 1, j);
                    String[] prefOpts = optsStr.split("[\\s+\\[(]");
                    if (!necessary) {
                        prefOpts = ArrayUtils.addAll(prefOpts, "");
                    }

                    endInd = j;
                    while (j < txt.length() && txt.charAt(j) != ' ' && txt.charAt(j) != '[') j++;

                    String varWord = txt.substring(endInd + 1, j);

                    String[] suffOpts;
                    if (j < txt.length() && (txt.charAt(j) == '[' || txt.charAt(j) == '(')) {
                        necessary = txt.charAt(j) == '[';
                        endInd = j;
                        while (j < txt.length() && txt.charAt(j) != ']' && txt.charAt(j) != ')') j++;

                        optsStr = txt.substring(endInd + 1, j);
                        suffOpts = optsStr.split("[\\s+\\[(]");
                        if (!necessary) {
                            suffOpts = ArrayUtils.addAll(suffOpts, "");
                        }
                    } else {
                        suffOpts = new String[]{""};
                    }

                    StringBuilder replStr = new StringBuilder();
                    for (String prefOpt : prefOpts) {
                        for (String suffOpt : suffOpts) {
                            replStr.append(prefOpt);
                            replStr.append(varWord);
                            replStr.append(suffOpt);
                            replStr.append(" ");
                        }
                    }

                    txt.replace(begInd, j, replStr.toString());
                    j = replStr.length() + begInd;
                }
            }

            String cleared = txt.toString().replaceAll("\\p{P}", "").trim();
            resultWords = cleared.split("\\s+", -1);
            resultWords = Arrays.stream(resultWords).filter(item -> item != null && !"".equals(item)).toArray(String[]::new);

            return new RhymesResult(resultWords, wordWithStress);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new RhymesResult(ErrorCodes.SERVER_ERROR);
        }
    }

    private static String getRhymesResponse(String word, int quality) {
        try {
            word = URLEncoder.encode(word, Charset.forName("windows-1251"));

            String urlParameters = "cDict=r&t52Rc=" + word +
                    "&cQuality=" + quality +
                    "&cPos=11&cFreq=*&cSylls=0&bPrefixedWords=ON&bInLine=ON&help=&lang=ru&wordCount=&updateStat=&showPopularWords=&detailedStat=&cAcc=3";

            String html = "";

            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            String request = "https://rifmovnik.ru/find";
            URL url = new URL(request);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);
            }

            try (DataInputStream r = new DataInputStream(conn.getInputStream())) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(r, "windows-1251"));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                html = result.toString();
            }

            return html;

        } catch (Exception e) {
            return "";
        }
    }
}
