package rhymes;

public class RhymesResult {
    private ErrorCodes error;
    private String[] data;

    public RhymesResult(String[] data) {
        this.data = data;
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

    public void setData(String[] data) {
        this.data = data;
    }
}

