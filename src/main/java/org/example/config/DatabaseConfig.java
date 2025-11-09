package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads database properties from the classpath so the rest of the app does not hardcode credentials.
 */
public final class DatabaseConfig {
    private static final String RESOURCE = "/db.properties";

    private DatabaseConfig() {
    }

    public static Properties load() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Could not find " + RESOURCE + " on classpath");
            }
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load database configuration", e);
        }
    }
}
