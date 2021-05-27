import model.WordEntry;
import rhymes.Rhymes;
import youtube.Youtube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    static final int quality = 0;

    public static void main(String[] args) {
        String[] wordRhymes = Rhymes.getRhymes("скрывайся", quality).getData();

        String videoId = /*"voL5_TYPgCU";*/ "P9LG1kTH3nE";

        Map<String, List<WordEntry>> wordsPos = new HashMap();

        try {
//            WordEntry[] subtitles = Youtube.getSubtitlesFromLib(video);
//            String[] subtitles = Youtube.getSubtitles(video);
//
//            int index = 0;
//            for (String sub : subtitles) {
//                String[] subWords = sub.split(" ");
//
//                for (String word : subWords) {
//                    word = word.replace("\n", ""); //costyl
//                    if (!wordsPos.containsKey(word)) {
//                        wordsPos.put(word, new ArrayList<>());
//                    }
//                    List<Integer> indexes = wordsPos.get(word);
//                    indexes.add(index);
//
//                    index++;
//                }
//                words.addAll(Arrays.asList(subWords));
//            }
//            Map<String, String> rhymes = getRhymesFromFile("rhymes.txt");

            WordEntry[] words = Youtube.getSubtitlesFromLib(
                    videoId,
                    Resource.getFromResource("subs/" + videoId + ".ru.vtt")
            );

            for (WordEntry wordEntry : words) {
                String word = wordEntry.getWord();

                if (!wordsPos.containsKey(word)) {
                    wordsPos.put(word, new ArrayList<>());
                }

                List<WordEntry> indexes = wordsPos.get(word);
                indexes.add(wordEntry);
            }
            Map<String, String> rhymes = getRhymesForSubs(wordsPos.keySet());

            String phraseStr = getWordsRhymesString(rhymes, wordsPos, words);
            System.out.println(phraseStr);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static String getWordsRhymesString(Map<String, String> rhymes,
                                               Map<String, List<WordEntry>> wordsPos,
                                               WordEntry[] words) {

        StringBuilder phrase = new StringBuilder();
        int contextWords = 3;

        for (String rhyme : rhymes.keySet()) {
            if (wordsPos.containsKey(rhymes.get(rhyme))) {
                phrase.append(getContextWordsString(contextWords, wordsPos.get(rhyme), words));
                phrase.append(" - ");

                phrase.append(getContextWordsString(contextWords, wordsPos.get(rhymes.get(rhyme)), words));
                phrase.append("\n");
            }
        }

        return phrase.toString();
    }

    private static String getContextWordsString(int wordsNum, Collection<WordEntry> entries, WordEntry[] words) {
        StringBuilder phrase = new StringBuilder();

        for (WordEntry entry : entries) {
            for (int i = wordsNum; i >= 0; i--) {
                if (entry.getIndex() - i >= 0) {
                    WordEntry ctxEntry = words[entry.getIndex() - i];
                    phrase.append(ctxEntry.getWord());
                    phrase.append(" ");
                }
            }
            phrase.append("(");
            phrase.append(entry.getTimeCode());
            phrase.append(") , ");
        }

        return phrase.toString();
    }

    private static Map<String, String> getRhymesForSubs(Collection<String> words) {
        Map<String, String> rhymes = new HashMap<>();

        for (String word : words) {
            if (!rhymes.containsKey(word)) {
                String[] wordRhymes = Rhymes.getRhymes(word, quality).getData();

                for (String rhyme : wordRhymes) {
                    if (words.contains(rhyme)) {
                        rhymes.put(word, rhyme);
                        rhymes.put(rhyme, word);
                    }
                }
            }
        }

        return rhymes;
    }

    private static Map<String, String> getRhymesFromFile(String fileName) throws IOException {
        String file = Resource.getFromResource(fileName);
        Map<String, String> rhymes = new HashMap<>();

        for (String rhyme : file.split(", ")) {
            String[] pair = rhyme.split(" - ");
            rhymes.put(pair[0], pair[1]);
        }

        return rhymes;
    }

    public static String getFromResource(String fileName) throws IOException {
        ClassLoader classLoader = Main.class.getClassLoader();
        InputStream fileIn = classLoader.getResourceAsStream(fileName);

        try (InputStreamReader streamReader = new InputStreamReader(fileIn, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {
            StringBuffer response = new StringBuffer();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line + "\n");
            }
            return response.toString();
        }
    }
}
