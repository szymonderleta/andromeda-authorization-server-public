package pl.derleta.authorization.config.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.derleta.authorization.config.model.RoleSecurity;
import pl.derleta.authorization.config.model.UserSecurity;

import java.io.IOException;


/**
 * JwtTokenFilter is a filter that intercepts HTTP requests to enable JWT-based authentication.
 * It extends OncePerRequestFilter and processes each request to extract and validate JWT tokens.
 * If a valid token is found, it sets the authentication context in the SecurityContextHolder.
 */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private JwtTokenUtil jwtUtil;

    @Autowired
    public void setJwtUtil(JwtTokenUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    /**
     * Processes the HTTP request and applies the JWT authentication filter logic.
     * This method attempts to extract a JWT token from the request, validates it,
     * and sets the authentication context if the token is valid. If no token is provided
     * or the token is invalid, the method allows the request to proceed without setting
     * the authentication context.
     *
     * @param request     the HttpServletRequest object containing client request information.
     * @param response    the HttpServletResponse object for sending the response back to the client.
     * @param filterChain the FilterChain object for passing the request and response to the next filter.
     * @throws ServletException if an error occurs during the filtering process.
     * @throws IOException      if an I/O error occurs during the filtering process.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        if (token == null || !jwtUtil.validateJWTToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        setAuthenticationContext(token, request);
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the token from an HTTP request.
     * The method first checks the "Authorization" header for a token in the format "Bearer <token>"
     * and retrieves it if available. If not found, it then looks for a "jwtToken" cookie and extracts its value.
     *
     * @param request the HttpServletRequest object containing the request information.
     * @return the extracted token as a String if found in the "Authorization" header or "token" cookie;
     * returns null if no token is present in either location.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!ObjectUtils.isEmpty(header) && header.startsWith("Bearer ")) {
            return header.split("\\s", 2)[1].trim();
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Sets the authentication context in the security context using the provided JWT token.
     * This method uses the token to retrieve user details, builds an authentication object,
     * and sets it in the SecurityContext for the current request.
     *
     * @param token   the JWT token used to authenticate the user.
     * @param request the HTTP request containing additional details about the user context.
     */
    private void setAuthenticationContext(String token, HttpServletRequest request) {
        UserDetails userDetails = getUserDetails(token);
        UsernamePasswordAuthenticationToken
                authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Extracts and constructs user details from the provided JWT token.
     * The method decodes the token to retrieve user information and assigns
     * corresponding roles to the user instance.
     *
     * @param token the JWT token containing user details and roles.
     * @return an instance of UserDetails populated with user ID, email, and roles.
     */
    private UserDetails getUserDetails(String token) {
        UserSecurity userDetails = new UserSecurity();
        Claims claims = jwtUtil.parseClaims(token);
        String subject = (String) claims.get(Claims.SUBJECT);
        String roles = claims.get("roles").toString();
        roles = roles.replace("[", "").replace("]", "");
        String[] roleNames = roles.split("},");
        for (String item : roleNames) {
            int startIndex = item.indexOf("name=") + 5;
            String roleName = item.substring(startIndex).trim().replace("}", "");
            userDetails.addRole(new RoleSecurity(roleName));
        }
        String[] jwtSubject = subject.split(",");
        userDetails.setId(Long.parseLong(jwtSubject[0]));
        userDetails.setEmail(jwtSubject[1]);
        return userDetails;
    }

}