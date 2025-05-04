package pl.derleta.authorization.config.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.repository.TokensGeneratorRepository;
import pl.derleta.authorization.config.security.jwt.JwtTokenFilter;

import java.util.Optional;

/**
 * SecurityConfig is a configuration class that defines the security setup
 * for the application, including authentication and authorization mechanisms.
 * It provides beans for authentication management, password encoding, and
 * user details retrieval, as well as configuring the security filter chain.
 * <p>
 * This class integrates custom JWT token filtering and exception handling
 * for unauthorized access attempts. It uses a custom {@link TokensGeneratorRepository}
 * for retrieving user information and a {@link JwtTokenFilter} for processing
 * JWT-based authentication.
 */
@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    private TokensGeneratorRepository userRepo;
    private JwtTokenFilter jwtTokenFilter;

    @Autowired
    public void setUserRepo(TokensGeneratorRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Autowired
    public void setJwtTokenFilter(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    /**
     * Configures and provides a {@link DaoAuthenticationProvider} bean for the application's
     * authentication system. This provider is responsible for handling user authentication by
     * utilizing a {@link UserDetailsService} and a {@link PasswordEncoder}.
     * <p>
     * It delegates the process of loading user details to the custom UserDetailsService
     * implementation and encodes passwords using the configured PasswordEncoder.
     *
     * @return a configured instance of {@link DaoAuthenticationProvider} for authentication purposes
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Provides a UserDetailsService implementation that retrieves user details based on the provided username.
     * The method searches for a user by their email and, if not found, falls back to searching by their login identifier.
     * If no user is found using either method, a UsernameNotFoundException is thrown.
     *
     * @return a UserDetailsService instance that resolves user details by username or email for authentication purposes
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Optional<UserSecurity> user = userRepo.findByEmail(username);
            if (user.isPresent() && user.get().getEmail().equals(username)) return user.get();
            return userRepo.findByLogin(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
        };
    }

    /**
     * Provides a bean of the PasswordEncoder used for encoding and verifying passwords
     * in the application's authentication system.
     *
     * @return an instance of BCryptPasswordEncoder for secure password hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates and provides an instance of AuthenticationManager for managing
     * authentication processes in the application.
     *
     * @param authConfig the AuthenticationConfiguration object that provides
     *                   the configuration for authentication management
     * @return an instance of AuthenticationManager used to handle authentication
     * @throws Exception if an error occurs while retrieving the AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configures and returns the SecurityFilterChain that defines the security settings
     * and filters for the application.
     * <p>
     * The SecurityFilterChain is configured with the following:
     * - CSRF is disabled.
     * - Public endpoint patterns are permitted.
     * - Any other requests require authentication.
     * - Stateless session management is enforced.
     * - A custom JWT token filter is added before the UsernamePasswordAuthenticationFilter.
     * - Custom exception handling for authentication entry points.
     * - HTTP Basic Authentication configuration is applied.
     *
     * @param http the HttpSecurity object that allows configuring web-based security for specific HTTP requests
     * @return the configured SecurityFilterChain for web security settings
     * @throws Exception if an error occurs while building the SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
//                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/public/**", "/api/v2/public/**",
                                "/api/v3/public/**", "/api/v3/auth/login",
                                "/api/v1/auth/login", "/auth/login", "/api/v1/auth/login**",
                                "/api/v1/auth/email", "/auth/email", "/api/v1/auth/email**",
                                "/api/v1/auth/refresh-access").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(
                        (request, response, ex) -> response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage())
                ))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

}
