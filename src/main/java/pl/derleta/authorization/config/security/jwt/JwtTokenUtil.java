package pl.derleta.authorization.config.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.derleta.authorization.config.model.UserSecurity;

import javax.crypto.SecretKey;
import java.sql.Date;

/**
 * Utility class for handling JSON Web Tokens (JWT) in the application.
 * This class provides methods for generating, validating, and parsing JWTs.
 * It is designed to support token-based authentication in a secure manner.
 */
@Component
public class JwtTokenUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${app.jwt.secret}")
    private String SECRET_KEY;

    @Value("${app.jwt.expiration.access}")
    public Integer JWT_ACCESS_EXPIRATION;

    @Value("${app.jwt.expiration.refresh}")
    public Long JWT_REFRESH_EXPIRATION;

    /**
     * Generates an access token for the given user based on their security attributes.
     * The token is constructed with the user's ID and email, includes their roles as a claim,
     * and is signed using a secret key.
     *
     * @param user the user for whom the access token is generated. This object contains
     *             the user's ID, email, and roles required to populate the token.
     * @return a signed JWT as a string that can be used for authentication and authorization purposes.
     */
    public String generateAccessToken(UserSecurity user) {
        return Jwts.builder()
                .subject(String.format("%s,%s", user.getId(), user.getEmail()))
                .issuer("DbConnectionApp")
                .claim("roles", user.getRoles())
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + JWT_ACCESS_EXPIRATION))
                .signWith(getPublicSigningKey())
                .compact();
    }

    /**
     * Generates a refresh token for the specified user. The token is constructed
     * with the user's ID and email as the subject, their roles as a claim, and
     * includes the issuer, issue date, expiration date, and signature.
     *
     * @param user the user for whom the refresh token is being generated. This
     *             object provides the user's ID, email, and roles necessary for
     *             creating the token.
     * @return a signed refresh token as a string that can be used to obtain a
     * new access token or continue a session.
     */
    public String generateRefreshToken(UserSecurity user) {
        return Jwts.builder()
                .subject(String.format("%s,%s", user.getId(), user.getEmail()))
                .issuer("DbConnectionApp")
                .claim("roles", user.getRoles())
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + JWT_REFRESH_EXPIRATION))
                .signWith(getPublicSigningKey())
                .compact();
    }

    /**
     * Validates the given JSON Web Token (JWT) to ensure it is properly formed, unexpired,
     * and signed with a valid key. This method checks the token's signature, format, and
     * expiration status and logs an error message for any detected issues.
     *
     * @param token the JWT as a string to be validated
     * @return true if the token is valid, false if the token is invalid, expired, null, or improperly formatted
     */
    public boolean validateJWTToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getPublicSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException ex) {
            LOGGER.error("JWT expired : {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Token is null, empty or only whitespace : {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            LOGGER.error("JWT is invalid", ex);
        } catch (UnsupportedJwtException ex) {
            LOGGER.error("JWT is not supported", ex);
        }
        return false;
    }

    /**
     * Retrieves the secret key used for signing JWT tokens.
     * The key is derived from a base64-encoded secret string.
     *
     * @return the secret key used for HMAC signing of JWT tokens.
     */
    private SecretKey getPublicSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    /**
     * Parses the given JSON Web Token (JWT) to extract the claims it contains.
     * The method uses a public signing key to verify the token's integrity and authenticity
     * before decoding and returning its claims.
     *
     * @param token the JWT as a string to be parsed. This token must be properly signed
     *              and encoded to allow successful verification and extraction of claims.
     * @return the claims extracted from the provided token. These claims may include properties
     * such as the token's subject, expiration, roles, and custom-defined attributes.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getPublicSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Retrieves the expiration date of a provided JWT token.
     * The method parses the token to extract its claims and returns
     * the expiration date contained within the claims.
     *
     * @param token the JWT as a string. This token must be properly formatted
     *              and signed to allow successful extraction of the expiration date.
     * @return the expiration date of the token as a Date object.
     */
    public Date getTokenExpiration(String token) {
        Claims claims = parseClaims(token);
        return new Date(claims.getExpiration().getTime());
    }

    /**
     * Extracts the user ID from the provided JWT token.
     * The user ID is assumed to be the first element in the token's subject, separated by a comma.
     *
     * @param token the JWT token from which to extract the user ID
     * @return the extracted user ID as a {@code Long}
     */
    public Long getUserId(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return Long.valueOf(claims.getSubject().split(",")[0]);
    }

    /**
     * Extracts all claims from a given JWT token.
     *
     * @param token the JWT token from which to extract the claims
     * @return a {@code Claims} object containing all claims extracted from the token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(getPublicSigningKey()).build().parseSignedClaims(token).getPayload();
    }

}
