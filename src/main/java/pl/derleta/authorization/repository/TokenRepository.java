package pl.derleta.authorization.repository;

import org.springframework.jdbc.core.RowMapper;
import pl.derleta.authorization.domain.builder.TokenFactory;
import pl.derleta.authorization.domain.entity.token.TokenEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.types.TokenType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Set;

public abstract class TokenRepository implements RepositoryClass {

    protected static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("u.user_id", "u.username", "u.email", "t.created_at", "t.expiration_date", "t.token_id");
    protected static final Set<String> ALLOWED_SORT_ORDERS = Set.of("ASC", "DESC");

    /**
     * Constructs a SQL "LIKE" parameter with wildcard characters (%) appended
     * to both the beginning and the end of the provided string parameter.
     * If the input parameter is null, it returns a string with only
     * wildcard characters ("%%").
     *
     * @param param the input string to be wrapped with wildcards;
     *              can be null, in which case an empty string is substituted
     * @return a string formatted as "%<input>%" for SQL "LIKE" queries
     */
    protected String getSqlLikeParam(String param) {
        final String effectiveParam = (param != null) ? param : "";
        return "%" + effectiveParam + "%";
    }

    /**
     * A {@link RowMapper} implementation responsible for mapping rows of a {@link ResultSet}
     * to {@link TokenEntity} objects. This class extracts user data and token data from
     * the result set and constructs the corresponding objects, linking them as required.
     * <p>
     * The mapping process includes reading user-related columns (e.g., user_id, username, email, password)
     * and token-related columns (e.g., token_id, token, expiration_date, type). A {@link UserEntity} is
     * created to represent the user, and a specific implementation of {@link TokenEntity} is created
     * based on the token type using the {@link TokenFactory#createToken} method.
     * <p>
     * Responsibilities:
     * - Extract user details and construct a {@link UserEntity}.
     * - Extract token details and determine the token type based on the database column value.
     * - Utilize {@link TokenFactory} to create the appropriate token entity.
     * <p>
     * Exception Handling:
     * - Throws {@link SQLException} if there are errors accessing the {@link ResultSet}.
     * <p>
     * This class is typically used in conjunction with JDBC templates for database-to-entity mapping.
     */
    public static class TokenMapper implements RowMapper<TokenEntity> {
        @Override
        public TokenEntity mapRow(ResultSet resultSet, int i) throws SQLException {

            long userId = resultSet.getLong("u.user_id");
            String username = resultSet.getString("u.username");
            String email = resultSet.getString("u.email");
            String password = resultSet.getString("u.password");
            UserEntity userEntity = new UserEntity(userId, username, email, password);

            long tokenId = resultSet.getLong("token_id");
            String token = resultSet.getString("t.token");
            Timestamp expirationDate = resultSet.getTimestamp("t.expiration_date");

            TokenType tokenType = TokenType.valueOf(resultSet.getString("t.type"));
            return TokenFactory.createToken(tokenType, tokenId, userEntity, token, expirationDate);
        }
    }

}
