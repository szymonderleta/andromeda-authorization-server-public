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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.security.api.AuthApiService;
import pl.derleta.authorization.config.security.api.AuthLoginRequest;
import pl.derleta.authorization.config.security.api.AuthResponse;
import pl.derleta.authorization.config.security.jwt.JwtTokenUtil;

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
        Cookie accessTokenCookie = new Cookie(COOKIE_ACCESS_NAME, accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(jwtUtil.JWT_ACCESS_EXPIRATION);
        response.addCookie(accessTokenCookie);

        int maxAgeSeconds = (int) Math.min((jwtUtil.JWT_REFRESH_EXPIRATION), Integer.MAX_VALUE);
        Cookie refreshTokenCookie = new Cookie(COOKIE_REFRESH_NAME, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(maxAgeSeconds);
        response.addCookie(refreshTokenCookie);
    }

}
