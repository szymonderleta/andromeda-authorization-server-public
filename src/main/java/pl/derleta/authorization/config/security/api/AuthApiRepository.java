package pl.derleta.authorization.config.security.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Repository class for managing operations on JWT tokens in the database.
 * This class interacts with the database using JdbcTemplate to perform CRUD operations
 * related to JWT token data.
 */
@Repository
public class AuthApiRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuthApiRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }


    /**
     * Retrieves the next unique identifier for an access token by querying the database.
     * This method calculates the next ID by finding the maximum token ID in the
     * `andromeda.access_tokens` table, incrementing it by 1. If no records exist, it defaults to 1.
     *
     * @return the next available access token ID as a {@code Long}
     */
    public Long getAccessTokenNextId() {
        String idSql = "SELECT COALESCE(MAX(token_id) + 1, 1) FROM access_tokens";
        return jdbcTemplate.queryForObject(idSql, Long.class);
    }

    /**
     * Generates the next unique identifier for a refresh token by querying the database.
     * This method retrieves the maximum token ID from the `andromeda.refresh_tokens` table
     * and increments it by 1. If no records exist, it defaults to 1.
     *
     * @return the next available refresh token ID as a {@code Long}
     */
    public Long getRefreshTokenNextId() {
        String idSql = "SELECT COALESCE(MAX(token_id) + 1, 1) FROM refresh_tokens";
        return jdbcTemplate.queryForObject(idSql, Long.class);
    }

    /**
     * Retrieves an access token from the database by its unique identifier.
     * This method queries the `access_tokens` table to find the token
     * associated with the provided token ID. If no matching token is found,
     * an empty {@code Optional} is returned.
     *
     * @param tokenId the unique identifier of the access token to retrieve
     * @return an {@code Optional} containing the access token if it exists; otherwise, an empty {@code Optional}
     */
    public Optional<String> findAccessTokenById(final long tokenId) {
        String sql = """
                SELECT jt.token
                FROM access_tokens jt
                WHERE token_id = ?;
                """;
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, new JwtTokenMapper(), tokenId)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves a refresh token from the database by its unique identifier.
     * This method queries the `refresh_tokens` table to find the token
     * associated with the provided token ID. If no matching token is found,
     * an empty {@code Optional} is returned.
     *
     * @param tokenId the unique identifier of the refresh token to retrieve
     * @return an {@code Optional} containing the refresh token if it exists; otherwise, an empty {@code Optional}
     */
    public Optional<String> findRefreshTokenById(final long tokenId) {
        String sql = """
                SELECT jt.token
                FROM refresh_tokens jt
                WHERE token_id = ?;
                """;
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, new JwtTokenMapper(), tokenId)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Saves an access token into the database for a specific user with an associated token ID and expiration date.
     * This method stores the token information into the `andromeda.access_tokens` table.
     *
     * @param tokenId the unique identifier for the access token to be stored
     * @param userId the unique identifier of the user associated with the access token
     * @param token the access token string to be saved in the database
     * @param expirationDate the expiration date of the access token
     */
    public void saveAccessToken(final long tokenId, final long userId, final String token, final Date expirationDate) {
        String sql = """ 
                INSERT INTO access_tokens (token_id, user_id, token, expiration_date)
                VALUES (?, ?, ?, ?);
                """;
        jdbcTemplate.update(sql,
                tokenId,
                userId,
                token,
                expirationDate);
    }

    /**
     * Saves a refresh token into the database for a specific user with an associated token ID.
     * The token is stored along with its expiration date.
     *
     * @param tokenId the unique identifier for the refresh token to be stored
     * @param userId the unique identifier of the user associated with the refresh token
     * @param token the refresh token string to be saved in the database
     * @param expirationDate the expiration date of the refresh token
     */
    public void saveRefreshToken(final long tokenId, final long userId, final String token, final Date expirationDate) {
        String sql = """ 
                INSERT INTO refresh_tokens (token_id, user_id, token, expiration_date)
                VALUES (?, ?, ?, ?);
                """;
        jdbcTemplate.update(sql,
                tokenId,
                userId,
                token,
                expirationDate);
    }

    /**
     * Implementation of the {@link RowMapper} interface to map a single column value
     * from a database result set to a {@code String}.
     * <p>
     * This class is specifically used to extract the "token" column from the result set
     * of a query on the `andromeda.jwt_tokens` table. It is designed to be a reusable
     * mapper for mapping the "jt.token" column to a string value.
     * <p>
     * Usage typically occurs within the context of DAO or repository methods, where
     * the result set of a query needs to be mapped to a single string value.
     */
    private static class JwtTokenMapper implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getString("jt.token");
        }
    }

}
