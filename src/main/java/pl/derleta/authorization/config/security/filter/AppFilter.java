package pl.derleta.authorization.config.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * A filter that processes incoming HTTP requests to validate the requesting
 * application's access permissions based on predefined allowed applications.
 * <p>
 * This filter checks for the presence of the "X-Requesting-App" header in the
 * HTTP request and compares its value against a list of allowed applications
 * defined in the application configuration. Requests originating from
 * unauthorized applications will be denied access.
 * <p>
 * The filter executes as the first filter in the chain due to its ordering
 * being set to 1. If the requesting application is allowed, it passes the
 * request through to the next filter in the chain; otherwise, it returns a
 * 403 response with an "Access denied" message.
 */
@Component
@Order(1)
public class AppFilter extends HttpFilter {

    @Value("${allowed.applications}")
    private String allowedApplications;

    /**
     * Filters incoming HTTP requests to validate if the requesting application has access permissions.
     * Checks for the presence of the "X-Requesting-App" header in the HTTP request, and processes or blocks
     * the request based on whether the application is allowed.
     * <p>
     * If the application is authorized, the filter passes the request to the next filter in the chain.
     * If the application is unauthorized, the filter returns a 403 response with an error message.
     *
     * @param request  the HTTP request containing the "X-Requesting-App" header
     * @param response the HTTP response to be sent to the client
     * @param chain    the filter chain used to pass the request and response to the next filter
     * @throws IOException      if an I/O error occurs during the processing of the request or response
     * @throws ServletException if an error occurs while processing the servlet
     */
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestingApp = request.getHeader("X-Requesting-App");
        if (isApplicationAllowed(requestingApp)) {
            chain.doFilter(request, response);
        } else {
            response.getWriter().write("Access denied for the application");
            response.setContentType("application/json");
            response.setStatus(403);
        }
    }

    /**
     * Determines if the provided application is allowed to access the system.
     * The method checks the provided application name against a list of allowed applications
     * configured in the application settings.
     *
     * @param requestingApp the name of the application requesting access
     * @return true if the requesting application is in the list of allowed applications; false otherwise
     */
    private boolean isApplicationAllowed(String requestingApp) {
        if (allowedApplications != null && requestingApp != null) {
            String[] allowedAppsArray = allowedApplications.split(",");
            for (String allowedApp : allowedAppsArray) {
                if (requestingApp.trim().equals(allowedApp.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

}
