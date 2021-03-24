import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Resource {
    public static String getFromResource(String fileName) throws IOException {
        ClassLoader classLoader = Ryfma.class.getClassLoader();
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
