package model;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WordEntry {
    private String word;
    private int index;
    private Date timeCode;
    private long timeDelay;
    private double normalizedDelay;

    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS");

    public WordEntry(int index) {
        this.index = index;
    }

    public WordEntry(String word, Date timeCode, long timeDelay) {
        this.timeDelay = timeDelay;
        this.timeCode = timeCode;
        this.word = word;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTimeCode() {
        return sdf.format(timeCode);
    }

    public void setTimeCode(String timeCode) throws ParseException {
        this.timeCode = sdf.parse(timeCode);
    }

    public void setTimeCode(Date timeCode) {
        this.timeCode = timeCode;
    }

    public double getNormalizedDelay() {
        return normalizedDelay;
    }

    public void setNormalizedDelay(double normalizedDelay) {
        this.normalizedDelay = normalizedDelay;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public long getTimeDelay() {
        return timeDelay;
    }

    public void setTimeDelay(long timeDelay) {
        this.timeDelay = timeDelay;
    }

    public int getLength() {
        int signs = StringUtils.countMatches(word, "ь") + StringUtils.countMatches(word, "ъ");
        return word.length() - signs;
    }

    public String toString() {
        return word + ": " + normalizedDelay + ", " + timeDelay;
    }
}
