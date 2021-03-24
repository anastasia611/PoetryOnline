import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class SubsParser {

    public static WordEntry[] parseSubs(String text) {
        List<WordEntry> wordDelays = getWordEntries(text);

        Map<Integer, Stats> dist = getDelayStats(wordDelays);
        normalizeDelays(wordDelays, dist);

        printWords(wordDelays);
        System.out.println(wordDelays.stream()
                .collect(
                        Collectors.groupingBy(WordEntry::getLength,
                                Collectors.averagingLong(WordEntry::getTimeDelay))
                )
        );

        List<List<WordEntry>> seqs = getWordsSequences(wordDelays, 2.5);
        for (List<WordEntry> seq : seqs) {
            for (WordEntry word : seq) {
                System.out.print(word.getWord() + " ");
            }
            System.out.println();
            System.out.println();
        }

        return wordDelays
                .stream()
                .toArray(WordEntry[]::new);
                //.map(WordEntry::getWord)
                //.toArray(String[]::new);
    }

    private static List<WordEntry> getWordEntries(String text) {

        List<WordEntry> words = new ArrayList<>();

        String TIME_REGEX = "[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}";
        Pattern TIME_CODE_PATTERN = Pattern.compile("<" + TIME_REGEX + ">");
        Pattern TIME_PATTERN = Pattern.compile(TIME_REGEX);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS");

        Date time1;
        Date time2 = null;

        int endIndex = 0;
        int startIndex = 0;

        int index = 0;

        String[] subLines = text.split("-->");

        for (int i = 0; i < subLines.length; i++) {
            try {
                int prevIndex = 0;

                Matcher startTimeMatcher = TIME_PATTERN.matcher(subLines[i]);
                if (startTimeMatcher.find()) {
                    time2 = sdf.parse(startTimeMatcher.group());
                }

                String subLine = subLines[++i];

                String content = subLine.split("\n")[2];
                Matcher timeTagMatcher = TIME_CODE_PATTERN.matcher(content);

                boolean found = false;

                while (timeTagMatcher.find()) {
                    WordEntry entry = new WordEntry(index++);
                    words.add(entry);

                    found = true;

                    String timeText = timeTagMatcher.group();
                    timeText = timeText.substring(1, timeText.length() - 1);

                    time1 = time2;
                    time2 = sdf.parse(timeText);
                    entry.setTimeCode(time1);
                    if (time1 != null) {
                        entry.setTimeDelay(time2.getTime() - time1.getTime());
                    } else {
                        entry.setTimeDelay(0L);
                    }

                    startIndex = timeTagMatcher.start();
                    endIndex = timeTagMatcher.end();

                    entry.setWord(filterWord(content.substring(prevIndex, startIndex)));
                    prevIndex = endIndex;
                }

                if (!found) {
                    continue;
                }

                WordEntry entry = new WordEntry(index++);
                words.add(entry);

                Matcher timeMatcher = TIME_PATTERN.matcher(subLine);
                String endLineTime = null;
                if (timeMatcher.find()) {
                    endLineTime = timeMatcher.group();
                }

                time1 = time2;
                time2 = sdf.parse(endLineTime);
                entry.setTimeCode(time1);
                entry.setTimeDelay(time2.getTime() - time1.getTime());

                entry.setWord(filterWord(content.substring(endIndex)));

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return words;
    }

    public static Map<Integer, Stats> getDelayStats(List<WordEntry> entries) {
        Map<Integer, Long> delayByLength = new HashMap<>();
        Map<Integer, Long> meanByLength = new HashMap<>();
        Map<Integer, Long> sumSquadByLength = new HashMap<>();
        Map<Integer, Integer> amountByLength = new HashMap<>();

        Map<Integer, Stats> statsByLength = new HashMap<>();

        for (WordEntry wordEntry : entries) {
            int length = wordEntry.getLength();

            if (delayByLength.containsKey(length)) {
                Long sum = delayByLength.get(length);
                delayByLength.put(length, sum + wordEntry.getTimeDelay());

                Integer num = amountByLength.get(length);
                amountByLength.put(length, num + 1);
            } else {
                delayByLength.put(length, wordEntry.getTimeDelay());
                amountByLength.put(length, 1);
            }
        }

        for (Integer length : delayByLength.keySet()) {
            Long sum = delayByLength.get(length);
            Integer num = amountByLength.get(length);

            meanByLength.put(length, sum / num);
        }

        for (WordEntry wordEntry : entries) {
            int length = wordEntry.getLength();
            Long mean = meanByLength.get(length);
            Long diff = wordEntry.getTimeDelay() - mean;

            if (sumSquadByLength.containsKey(length)) {
                Long sumSquad = sumSquadByLength.get(length);
                sumSquadByLength.put(length, sumSquad + diff * diff);
            } else {
                sumSquadByLength.put(length, diff * diff);
            }
        }

        for (Integer length : sumSquadByLength.keySet()) {
            Long sumSquad = sumSquadByLength.get(length);
            Integer num = amountByLength.get(length);

            statsByLength.put(length, new Stats(meanByLength.get(length), Math.sqrt(sumSquad / (double)((num - 1 > 0) ? num - 1 : num))));
        }

        return statsByLength;
    }

    public static void normalizeDelays(List<WordEntry> wordEntries, Map<Integer, Stats> meanDist) {
        for (WordEntry wordEntry : wordEntries) {
            long meanForLength = meanDist.get(wordEntry.getLength()).getMean();
            double stdDevForLength = meanDist.get(wordEntry.getLength()).getStdDev();

            if (stdDevForLength == 0 || wordEntry.getWord().matches("\\d+")) {
                wordEntry.setNormalizedDelay(0);
            } else {
                double diff = wordEntry.getTimeDelay() - meanForLength;
                wordEntry.setNormalizedDelay(diff / stdDevForLength);
            }
        }
    }

    public static List<List<WordEntry>> getWordsSequences(List<WordEntry> entries, double threshold) {
        List<List<WordEntry>> seqs = new ArrayList<>();
        List<WordEntry> currentSeq = new ArrayList<>();

        for (WordEntry wordEntry : entries) {
            if (wordEntry.getLength() > threshold && wordEntry.getNormalizedDelay() > 2) {
                seqs.add(currentSeq);
                currentSeq = new ArrayList<>();
            }
            currentSeq.add(wordEntry);
        }

        return seqs;
    }

    public static void printWords(List<WordEntry> words) {
        for (int i = 0; i < words.size(); i++) {
            System.out.println(words.get(i));
        }
    }

    public static String filterWord(String text) {
        return text
                .replaceAll("</c>", "")
                .replaceAll("<c>", "")
                .replaceAll(" ", "");
    }
}

class Stats {
    private long mean;
    private double stdDev;

    public long getMean() {
        return mean;
    }

    public void setMean(long mean) {
        this.mean = mean;
    }

    public double getStdDev() {
        return stdDev;
    }

    public void setStdDev(long stdDev) {
        this.stdDev = stdDev;
    }

    public Stats(long mean, double stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
    }
}