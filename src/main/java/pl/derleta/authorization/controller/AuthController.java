package pl.derleta.authorization.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.security.api.AuthApiService;
import pl.derleta.authorization.config.security.api.AuthLoginRequest;
import pl.derleta.authorization.config.security.api.AuthResponse;
import pl.derleta.authorization.config.security.jwt.JwtTokenUtil;

import java.sql.Date;
import java.util.Objects;


/**
 * The AuthController class is a REST controller responsible for managing
 * authentication-related operations, such as user login and token management.
 * This controller handles requests and provides responses for authentication actions,
 * including adding JWT tokens to response cookies for authenticated users.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String COOKIE_ACCESS_NAME = "accessToken";
    private static final String COOKIE_REFRESH_NAME = "refreshToken";

    private final AuthenticationManager authManager;
    private final JwtTokenUtil jwtUtil;
    private final AuthApiService authApiService;

    @Autowired
    public AuthController(AuthenticationManager authManager, JwtTokenUtil jwtUtil, AuthApiService authApiService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.authApiService = authApiService;
    }

    /**
     * Authenticates a user using the provided login credentials and returns an appropriate response.
     * On successful authentication, builds a success response.
     * On failure, returns an unauthorized response.
     *
     * @param request  the authentication request containing user login and password
     * @param response the HTTP response used to add additional information (e.g., cookies)
     * @return a ResponseEntity containing authentication success details or an error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthLoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
            return buildSuccessResponse(authentication, response);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login credentials");
        }
    }

    /**
     * Endpoint for refreshing the access token using the provided refresh token from cookies.
     * This method validates the refresh token, generates a new access token, and adds the
     * access token and refresh token to the response cookies.
     * <p>
     * If the refresh token is valid, a new access token is generated and returned in the cookies;
     * otherwise, an "Unauthorized" response is returned.
     *
     * @param refreshToken the refresh token retrieved from the request cookies
     * @param response     the HTTP response to which the new tokens will be added as cookies
     * @return a ResponseEntity with a status of 200 (OK) if the refresh token is valid and the
     * access token is refreshed, or a 401 (UNAUTHORIZED) status with an error message
     * if the refresh token is invalid.
     */
    @PostMapping("/refresh-access")
    public ResponseEntity<?> refreshAccess(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        if (jwtUtil.validateJWTToken(refreshToken)) {
            Long userId = jwtUtil.getUserId(refreshToken);
            var accessToken = authApiService.updateAccessToken(userId);
            addCookiesToResponse(response, accessToken, refreshToken);
            return ResponseEntity.ok("Access token refreshed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }

    /**
     * Builds a success response for a successful authentication by generating
     * JWT tokens (access token and refresh token), storing them, and adding relevant
     * cookies to the HTTP response. The successful response contains user details.
     * If the user is null, returns an unauthorized error response.
     *
     * @param authentication      the authentication object containing user details
     * @param httpServletResponse the HTTP response to which cookies will be added
     * @return a ResponseEntity containing the user details on successful authentication
     * or an unauthorized response if the authentication fails
     */
    @NotNull
    private ResponseEntity<?> buildSuccessResponse(Authentication authentication, HttpServletResponse httpServletResponse) {
        UserSecurity user = (UserSecurity) authentication.getPrincipal();
        if (Objects.isNull(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login credentials");
        }
        final String accessToken = jwtUtil.generateAccessToken(user);
        final String refreshToken = jwtUtil.generateRefreshToken(user);
        addCookiesToResponse(httpServletResponse, accessToken, refreshToken);
        var accessResult = this.authApiService.saveAccessToken(user.getId(), accessToken, jwtUtil.getTokenExpiration(accessToken));
        var refreshResult = this.authApiService.saveRefreshToken(user.getId(), refreshToken, jwtUtil.getTokenExpiration(refreshToken));
        AuthResponse response = new AuthResponse(user.getUsername(), user.getEmail());
        if (accessResult && refreshResult) return ResponseEntity.ok(response);
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }

    /**
     * Adds cookies containing the access token and refresh token to the HTTP response.
     * These cookies are configured with properties such as HttpOnly, Secure, Path, and MaxAge.
     *
     * @param response     the HTTP response to which the cookies will be added
     * @param accessToken  the JWT access token to be included in the "accessToken" cookie
     * @param refreshToken the JWT refresh token to be included in the "refreshToken" cookie
     */
    private void addCookiesToResponse(HttpServletResponse response, final String accessToken, final String refreshToken) {
        Cookie accessTokenCookie = getAccessTokenCookie(accessToken);
        response.addCookie(accessTokenCookie);
        Cookie refreshTokenCookie = getRefresTokenCookie(refreshToken);
        response.addCookie(refreshTokenCookie);
    }

    /**
     * Creates a cookie containing the access token.
     * Sets attributes such as HttpOnly, Secure, Path, and Max-Age based on the token's expiration time.
     *
     * @param token the JWT access token
     * @return the configured access token cookie
     */
    private Cookie getAccessTokenCookie(String token) {
        Cookie accessTokenCookie = new Cookie(COOKIE_ACCESS_NAME, token);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        int maxAgeSeconds = (int) getMaxAgeSecondsForAccessToken(token);
        accessTokenCookie.setMaxAge(maxAgeSeconds);
        return accessTokenCookie;
    }

    /**
     * Creates a cookie containing the refresh token.
     * Sets attributes such as HttpOnly, Secure, Path, and Max-Age based on the token's expiration time.
     *
     * @param token the JWT refresh token
     * @return the configured refresh token cookie
     */
    private Cookie getRefresTokenCookie(String token) {
        Cookie refreshTokenCookie = new Cookie(COOKIE_REFRESH_NAME, token);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        int maxAgeSeconds = (int) getMaxAgeSecondsForRefreshToken(token);
        refreshTokenCookie.setMaxAge(maxAgeSeconds);
        return refreshTokenCookie;
    }

    /**
     * Calculates the maximum age in seconds for the access token cookie,
     * based on the token's expiration time and a predefined upper limit (JWT_ACCESS_EXPIRATION).
     *
     * @param token the JWT access token
     * @return the number of seconds until expiration, capped by JWT_ACCESS_EXPIRATION
     */
    private long getMaxAgeSecondsForAccessToken(String token) {
        long maxAgeSeconds = getMaxAgeSeconds(token);
        int seconds = (int) Math.min(maxAgeSeconds, Integer.MAX_VALUE);
        return Math.min(seconds, jwtUtil.JWT_ACCESS_EXPIRATION);
    }

    /**
     * Calculates the maximum age in seconds for the refresh token cookie,
     * based on the token's expiration time and a predefined upper limit (JWT_REFRESH_EXPIRATION).
     *
     * @param token the JWT refresh token
     * @return the number of seconds until expiration, capped by JWT_REFRESH_EXPIRATION
     */
    private long getMaxAgeSecondsForRefreshToken(String token) {
        long maxAgeSeconds = getMaxAgeSeconds(token);
        int seconds = (int) Math.min(maxAgeSeconds, Integer.MAX_VALUE);
        return Math.min(seconds, jwtUtil.JWT_REFRESH_EXPIRATION);
    }

    /**
     * Computes the number of milliseconds between the current time and the token's expiration time.
     * This is used to determine how long the cookie should be valid.
     *
     * @param token the JWT token (either access or refresh)
     * @return the remaining time in milliseconds until token expiration
     */
    private long getMaxAgeSeconds(String token) {
        Date expirationDate = jwtUtil.getTokenExpiration(token);
        if(expirationDate == null || expirationDate.getTime() < 0) return 0;
        Date now = new Date(System.currentTimeMillis());
        long expirationTime = expirationDate.getTime();
        long nowTime = now.getTime();
        return expirationTime - nowTime;
    }

}
