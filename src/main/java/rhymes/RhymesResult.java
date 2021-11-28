package rhymes;

public class RhymesResult {
    private ErrorCodes error;
    private String[] data;
    private String word;

    public RhymesResult(String[] data, String word) {
        this.data = data;
        this.word = word;
    }

    public RhymesResult(ErrorCodes error) {
        this.error = error;
    }

    public ErrorCodes getError() {
        return error;
    }

    public void setError(ErrorCodes error) {
        this.error = error;
    }

    public String[] getData() {
        return data;
    }

    public String getWord() {
        return word;
    }

    public void setData(String[] data) {
        this.data = data;
    }
}

