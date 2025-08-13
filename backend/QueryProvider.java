import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class QueryProvider {

    private static final Properties queries = new Properties();

    static {
        try (InputStream is = QueryProvider.class.getClassLoader()
                .getResourceAsStream("report-queries.properties")) {
            if (is == null) {
                throw new IllegalStateException("report-queries.properties not found in classpath");
            }
            queries.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load report queries", e);
        }
    }

    public static String getQuery(String key) {
        String query = queries.getProperty(key);
        if (query == null) {
            throw new IllegalArgumentException("No query found for key: " + key);
        }
        return query;
    }
}
