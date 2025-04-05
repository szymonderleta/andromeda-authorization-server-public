package pl.derleta.authorization.config.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration class for database-related beans.
 * Provides configuration for setting up a JdbcTemplate bean using the provided DataSource.
 */
@Configuration
public class DbConf {

    private final DataSource dataSource;

    /**
     * Constructs a DbConf instance with the specified DataSource.
     *
     * @param dataSource the DataSource to be used for database configurations
     */
    @Autowired
    public DbConf(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates and configures a JdbcTemplate bean using the provided DataSource.
     * The JdbcTemplate provides a convenient template for executing SQL queries,
     * updates, and other database operations while abstracting common boilerplate code.
     *
     * @return an instance of JdbcTemplate configured with the application's DataSource
     */
    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

}
