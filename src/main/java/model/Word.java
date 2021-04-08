package model;

public class Word {
    private String word;
    private String properties;
    private int emphasis;

    public Word(String word, String properties, int emphasis) {
        this.word = word;
        this.properties = properties;
        this.emphasis = emphasis;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public int getEmphasis() {
        return emphasis;
    }

    public void setEmphasis(int emphasis) {
        this.emphasis = emphasis;
    }
}
