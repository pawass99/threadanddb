package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Provides a singleton {@link DataSource} configured through {@code db.properties}.
 */
public final class DataSourceFactory {
    private static DataSource dataSource;

    private DataSourceFactory() {
    }

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = createDataSource(DatabaseConfig.load());
        }
        return dataSource;
    }

    private static DataSource createDataSource(Properties props) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(props.getProperty("jdbc.url"));
        hikariConfig.setUsername(props.getProperty("jdbc.user"));
        hikariConfig.setPassword(props.getProperty("jdbc.pass"));

        String poolSize = props.getProperty("pool.size", "8");
        hikariConfig.setMaximumPoolSize(Integer.parseInt(poolSize));

        // Reasonable defaults for demo usage.
        hikariConfig.setPoolName("ThreadDemoPool");
        hikariConfig.setInitializationFailTimeout(-1);

        return new HikariDataSource(hikariConfig);
    }
}
