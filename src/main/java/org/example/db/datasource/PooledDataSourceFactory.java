package org.example.db.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class PooledDataSourceFactory {
    private PooledDataSourceFactory() {}

    public static DataSource createHikari(
            String url,
            String username,
            String password,
            int maxPoolSize
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(Math.min(2, maxPoolSize));
        config.setConnectionTimeout(5000);
        config.setPoolName("RouteSubsystemPool");

        return new HikariDataSource(config);
    }
}
